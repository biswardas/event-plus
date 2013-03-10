package com.biswa.ep.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.biswa.ep.entities.aggregate.Aggregator;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.spec.SortSpec.SortOrder;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.PivotedSubstance;
import com.biswa.ep.entities.substance.Substance;

/**
 * Schema to provide pivoting of the container based on defined set of
 * attribute.
 * 
 * @author biswa
 * 
 */
public class PivotContainer extends ConcreteContainer {
	/**
	 * Default substance in case pivoting is requested on a null value.
	 */
	static final private Substance DEFAULT_SUBSTANCE = new ObjectSubstance("");
	/**
	 * Virtual root of all pivot / leaf entry
	 */
	private PivotEntry root;
	/**
	 * Attributes pivoted on
	 */
	private List<Attribute> pivotedAttributes = new ArrayList<Attribute>();
	
	private Map<Attribute, Aggregator> aggrMap=new HashMap<Attribute, Aggregator>();

	/**
	 * Create a pivot container with cascade schema and supplied pivot attribute
	 * array.
	 * 
	 * @param name String Name of the Container
	 */
	public PivotContainer(String name,Properties props) {
		super(name,props);
	}

	@Override
	final public void connected(ConnectionEvent ce) {
		super.connected(ce);
		root = new PivotEntry();
	}

	@Override
	final public void entryUpdated(ContainerEvent ce) {
		if (ce.getSource() == this.getName()) {
			simpleEntryUpdate(ce);
		} else {
			if (pivotedAttributes.contains(ce.getAttribute())) {
				pivotEntryUpdate(ce);
			} else {
				simpleEntryUpdate(ce);
			}
		}
	}

	/**
	 * When pivoted substance is updated then the pivot is basically invalidated. <br>
	 * 1. Fetch the container entry based on the incoming event & reconstruct the
	 * qualifier.<br> 
	 * 2. Remove the entry for which the pivoted column is being updated. <br>
	 * 3. Reconstruct an event to request the entry to be re added and
	 * pivoted again.<br> 
	 * Keep in mind this is a very expensive operation.
	 * 
	 * @param ce ContainerEvent
	 */
	private void pivotEntryUpdate(ContainerEvent ce) {
		// Fetch the concrete ContainerEntry based on the incoming event
		ContainerEntry containerEntry = getConcreteEntry(ce.getIdentitySequence());
		// If a pivot entry is being updated then rules of the game is very
		// different merge the container entry
		TransportEntry transportEntry = containerEntry.cloneConcrete();
		transportEntry.getEntryQualifier().put(ce.getAttribute(), ce.getSubstance());
		// Remove the container entry
		entryRemoved(ce);
		// re add the container entry
		ContainerEvent adjEvent = new ContainerInsertEvent(ce.getSource(),
				transportEntry,getCurrentTransactionID());
		entryAdded(adjEvent);
	}

	/**
	 * Update the container entry and dispatch the entry updated event.
	 * 
	 * @param ce  ContainerEvent
	 */
	private void simpleEntryUpdate(ContainerEvent ce) {
		// Fetch the concrete ContainerEntry based on the incoming event
		ContainerEntry containerEntry = getConcreteEntry(ce.getIdentitySequence());
		// fetch the pivot holding this physical
		PivotEntry pivotEntry = containerEntry == null ? null
				: getLeafPivotEntry(containerEntry);
		// Update the physical container
		super.entryUpdated(ce);
		// Update the aggregates
		if (pivotEntry != null)
			pivotEntry.aggregateAndPropagate(ce.getAttribute().getRegisteredAttribute());
	}

	@Override
	final public void entryRemoved(ContainerEvent ce) {
		// Fetch the concrete ContainerEntry based on the incoming event
		ContainerEntry containerEntry = getConcreteEntry(ce.getIdentitySequence());
		PivotEntry pivotEntry = getLeafPivotEntry(containerEntry);
		// Remove the concrete entry
		super.entryRemoved(ce);
		// Remove the registered container entry
		pivotEntry.unregister(containerEntry);
	}

	// TODO optimize the way to access Pivot directly
	private PivotEntry getLeafPivotEntry(ContainerEntry containerEntry) {
		PivotEntry pivotEntry = root;
		// Remove the pivot entries if required, search for the leaf pivot and
		// remove if required.
		for (Attribute pivotedAttribute:pivotedAttributes) {
			Substance substanceAtDepth = containerEntry.getSubstance(pivotedAttribute);
			if (substanceAtDepth == null) {
				substanceAtDepth = DEFAULT_SUBSTANCE;
			}
			// Obtain the pivot based on substance at depth
			pivotEntry = pivotEntry.childPivotEntries.get(substanceAtDepth);
		}
		return pivotEntry;
	}
	boolean externallyAdded = false;
	@Override
	final public void entryAdded(ContainerEvent ce) {
		externallyAdded = true;
		// Add the physical entry
		super.entryAdded(ce);
	}

	@Override
	public void dispatchEntryAdded(ContainerEntry containerEntry) {
		super.dispatchEntryAdded(containerEntry);
		if(externallyAdded){
			externallyAdded =false;
			applyPivot(containerEntry);
		}
	}

	@Override
	public void attributeAdded(ContainerEvent ce) {
		super.attributeAdded(ce);
	}

	@Override
	public void attributeRemoved(ContainerEvent ce) {
		super.attributeRemoved(ce);
		aggrMap.remove(ce.getAttribute());
		if (pivotedAttributes.contains(ce.getAttribute())) {
			pivotedAttributes.remove(ce.getAttribute());
			pivot(pivotedAttributes.toArray(new Attribute[0]));
		}
	}

	private void applyPivot(ContainerEntry containerEntry) {
		PivotEntry pivotEntry = root;

		// Create the qualifier entries for the pivot entries
		final Map<Attribute, Substance> entryQualifier = new HashMap<Attribute, Substance>();

		// Create the pivot entries if required, in case exists navigate till
		// the leaf pivot
		
		for (int depth=0;depth<pivotedAttributes.size();depth++) {
			Attribute pivotedAttribute = pivotedAttributes.get(depth);
			Substance substanceAtDepth = containerEntry.getSubstance(pivotedAttribute);
			substanceAtDepth = new PivotedSubstance(substanceAtDepth != null ? substanceAtDepth: DEFAULT_SUBSTANCE);

			entryQualifier.put(pivotedAttribute, substanceAtDepth);
			PivotEntry tempPivotEntry = pivotEntry.childPivotEntries
					.get(substanceAtDepth);
			if (tempPivotEntry == null) {
				tempPivotEntry = pivotEntry.create(depth, substanceAtDepth);
				tempPivotEntry.letTheWorldKnow(entryQualifier);
			}
			pivotEntry = tempPivotEntry;
		}
		// Register the physical entry reference
		pivotEntry.register(containerEntry);
	}
	
	private Aggregator getAggregator(Attribute attribute) {
		Aggregator aggregator = aggrMap.get(attribute);
		return aggregator!=null?aggregator:Aggregators.NONE.AGGR;
	}

	/**
	 * Pivoted entry are the virtual entries based the incoming physicals.
	 * 
	 * @author biswa
	 * 
	 */
	private final class PivotEntry {
		/**
		 * Substance on which this pivot has been created
		 */
		private final Substance substance;
		/**
		 * Parent pivot of this current pivot
		 */
		private final PivotEntry parent;
		/**
		 * Unique identity of this pivot (virtual container entry) in the
		 * current infrastructure.
		 */
		private final int identity = generateIdentity();
		/**
		 * Child pivots this pivot contains.
		 */
		private final Map<Substance, PivotEntry> childPivotEntries;
		private final Collection<Integer> registeredEntries;

		/**
		 * Constructor to create the Pivot entry. This is the ROOT constructor
		 */
		private PivotEntry() {
			this.parent = null;
			this.substance = DEFAULT_SUBSTANCE;
			this.registeredEntries = new ArrayList<Integer>();
			this.childPivotEntries = new HashMap<Substance, PivotEntry>();

			// Additional pivoted Entry to be created
			// Create the qualifier entries for the pivot entries
			final Map<Attribute, Substance> entryQualifier = new HashMap<Attribute, Substance>();
			// Create the pivot entries if required, in case exists navigate till
			// the leaf pivot
			for (Attribute pivotedAttribute:pivotedAttributes) {
				entryQualifier.put(pivotedAttribute, DEFAULT_SUBSTANCE);
			}
			letTheWorldKnow(entryQualifier);
		}

		private void letTheWorldKnow(
				final Map<Attribute, Substance> entryQualifier) {
			TransportEntry newContainerEntry = new TransportEntry(identity,
					entryQualifier);
			ContainerEvent adjEvent = new ContainerInsertEvent(
					PivotContainer.this.getName(), newContainerEntry,getCurrentTransactionID());
			PivotContainer.super.entryAdded(adjEvent);
		}

		/**
		 * Constructor to create the Pivot entry to contain child pivots
		 * 
		 * @param depth
		 *            int
		 * @param substanceAtDepth
		 *            Substance
		 * @param parent
		 *            PivotEntry
		 */
		private PivotEntry(int depth, Substance substanceAtDepth,
				PivotEntry parent) {
			this.substance = substanceAtDepth;
			this.parent = parent;
			// Initialize the leaf container only for the leaf pivot
			if (depth == pivotedAttributes.size() - 1) {
				this.childPivotEntries = null;
				registeredEntries = new ArrayList<Integer>();
			} else {
				this.registeredEntries = null;
				childPivotEntries = new HashMap<Substance, PivotEntry>();
			}
		}

		/**
		 * Unregister the physical entry from this pivot. If this pivot is
		 * containing only this container entry then remove the pivot and
		 * propagate the changes to the parents.
		 * 
		 * @param containerEntry
		 */
		private void unregister(ContainerEntry containerEntry) {
			// Remove the physical data
			registeredEntries.remove(containerEntry.getIdentitySequence());
			for (Attribute attribute : aggrMap.keySet()) {
				aggregateAndPropagate(attribute);
			}
			if (registeredEntries.size() == 0) {
				parent.removePivot(this.substance);
			}
		}

		/**
		 * Remove pivot based on this substance on the current pivot
		 * 
		 * @param substance
		 */
		private void removePivot(Substance substance) {
			PivotEntry toBeDeletedEntry = childPivotEntries.remove(substance);
			ContainerEvent adjEvent = new ContainerDeleteEvent(
					PivotContainer.this.getName(), toBeDeletedEntry.identity,getCurrentTransactionID());
			PivotContainer.super.entryRemoved(adjEvent);

			if (parent != null && childPivotEntries.isEmpty()) {
				parent.removePivot(this.substance);
			}
		}

		/**
		 * Registers the physical with the current pivot
		 * 
		 * @param containerEntry
		 */
		private void register(ContainerEntry containerEntry) {
			registeredEntries.add(containerEntry.getIdentitySequence());			
			for (Attribute attribute : aggrMap.keySet()) {
				aggregateAndPropagate(attribute);
			}
		}

		/**
		 * factory method to create the pivot and propagate the required
		 * notifications
		 * 
		 * @param depth
		 *            create the pivot at this depth
		 * @param substanceAtDepth
		 *            Substance at the current depth
		 * @param entryQualifier
		 *            create the pivot with this qualifier entry
		 * @return PivotEntry
		 */
		private PivotEntry create(final int depth,
				final Substance substanceAtDepth) {
			PivotEntry pivEntry = new PivotEntry(depth, substanceAtDepth, this);
			childPivotEntries.put(substanceAtDepth, pivEntry);
			return pivEntry;
		}		
			
		private void aggregateAndPropagate(Attribute attribute) {
			aggregate(attribute);
			if (parent != null)
				parent.aggregateAndPropagate(attribute);
		}

		private void aggregate(Attribute attribute) {
			Aggregator aggregator = getAggregator(attribute);
			if(aggregator!=Aggregators.NONE.AGGR){
				ContainerEntry[] containerEntries = entriesToAggregate();
				Collection<Substance> inputSubstances = new ArrayList<Substance>();
				for (ContainerEntry containerEntry : containerEntries) {
					inputSubstances.add(containerEntry.getSubstance(attribute));
				}
				ContainerEvent adjEvent = new ContainerUpdateEvent(
						PivotContainer.this.getName(), identity,
						attribute, aggregator.failSafeaggregate(inputSubstances
								.toArray(new Substance[0])),getCurrentTransactionID());
				PivotContainer.super.entryUpdated(adjEvent);
			}
		}

		private ContainerEntry[] entriesToAggregate() {
			ContainerEntry[] containerEntries = null;
			if (childPivotEntries != null && pivotedAttributes.size()>0) {
				// Parent pivots aggregate only on the underlying pivots
				PivotEntry[] pivotEntries = childPivotEntries.values().toArray(
						new PivotEntry[0]);

				containerEntries = new ContainerEntry[pivotEntries.length];
				for (int i = 0; i < pivotEntries.length; i++) {
					containerEntries[i] = getConcreteEntry(pivotEntries[i].identity);
				}

			} else {// Leaf level Pivot
				Integer[] containerEntryIdentities = registeredEntries
						.toArray(new Integer[0]);

				containerEntries = new ContainerEntry[containerEntryIdentities.length];
				for (int i = 0; i < containerEntryIdentities.length; i++) {
					containerEntries[i] = getConcreteEntry(containerEntryIdentities[i]);
				}
			}
			return containerEntries;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((substance == null) ? 0 : substance.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PivotEntry other = (PivotEntry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (substance == null) {
				if (other.substance != null)
					return false;
			} else if (!substance.equals(other.substance))
				return false;
			return true;
		}

		private PivotContainer getOuterType() {
			return PivotContainer.this;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (parent != null) {
				sb.append(parent.toString()).append(":").append(substance);
			}
			return sb.toString();
		}

		public void clear() {
			if (childPivotEntries != null) {
				HashSet<PivotEntry> set = new HashSet<PivotEntry>(childPivotEntries.values());
				for (PivotEntry pivotEntry : set) {
					pivotEntry.clear();
				}
			}
			if (registeredEntries != null) {
				registeredEntries.clear();
				if (parent != null) {
					parent.removePivot(this.substance);
				}
			}
		}
	}

	public void pivot(Attribute[] pivotArray) {
		pivotedAttributes.clear();
		for(Attribute pivot:pivotArray){
			Attribute registered = pivot.getRegisteredAttribute();
			if(registered!=null){
				//Allow pivoting only on the preadded attributes
				pivotedAttributes.add(registered);	
			}
		}
		//Can not aggregate on the pivoted columns so remove them from the aggregation
		//as soon a pivot is applied
		for(Attribute attribute:pivotArray){
			aggrMap.remove(attribute);
		}
		if (root != null) {
			root.clear();
			// Re pivot everything based on new specification			
			int rootIdentity = root.identity; // TODO check does this ever change?
			for (ContainerEntry containerEntry : getContainerDataEntries()) {
				if(containerEntry.getIdentitySequence()==rootIdentity) continue;
				applyPivot(containerEntry);
			}
		}
	}
	
	@Override
	public void applySort(final SortOrder ... sortorder){
	}
	
	public void aggregate(final Map<Attribute, Aggregator> changeMap) {
		for(Attribute attribute:pivotedAttributes){
			changeMap.remove(attribute);
		}
		for(Entry<Attribute, Aggregator> entry:changeMap.entrySet()){
			this.aggrMap.put(entry.getKey().getRegisteredAttribute(),entry.getValue());
		}
		if(root!=null){
			Runnable aggrRunnable = new Runnable(){
				public void run(){
					aggregateUniverse(root);
				}
				private void aggregateUniverse(PivotEntry pivotEntry){		
					if (pivotEntry.childPivotEntries != null && pivotedAttributes.size()>0) {
						for(PivotEntry innerPivot:pivotEntry.childPivotEntries.values()){
							aggregateUniverse(innerPivot);				
						}
					}
					for(Attribute attribute:aggrMap.keySet()){
						pivotEntry.aggregate(attribute);
					}
				}
			};
			//Oh I know it has to run in same thread
			aggrRunnable.run();
		}
	}
}