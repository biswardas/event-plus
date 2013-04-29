package com.biswa.ep.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import com.biswa.ep.entities.aggregate.Aggregator;
import com.biswa.ep.entities.store.ConcreteContainerEntry;
import com.biswa.ep.entities.transaction.Agent;

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
	static final private Object DEFAULT_SUBSTANCE = "";
	
	public class PivotAgent extends FilterAgent{
		/**
		 * Attributes pivoted on
		 */
		private final Map<Attribute, Boolean> pivotedAttributes = new LinkedHashMap<Attribute, Boolean>();

		private final Map<Attribute, Aggregator> aggrMap = new LinkedHashMap<Attribute, Aggregator>();

		private final Map<Attribute, Boolean> sortOrder = new LinkedHashMap<Attribute, Boolean>();

		private final Map<Integer, PivotEntry> directPivotAccess = new HashMap<Integer, PivotEntry>();
		/**
		 * Virtual root of all pivot / leaf entry
		 */
		private final PivotEntry root = new PivotEntry();

		private ConcreteContainerEntry[] indexedEntries = null;
		private PivotAgent(String sink, Agent agent) {
			super(sink, agent);
		}
		/**
		 * Pivoted entry are the virtual entries based the incoming physicals.
		 * 
		 * @author biswa
		 * 
		 */
		public final class PivotEntry extends ConcreteContainerEntry{
			/**
			 * 
			 */
			private static final long serialVersionUID = -5874874959467249733L;
			private final Comparator<Object> objectComparator = new Comparator<Object>(){

				@Override
				public int compare(Object o1Substance, Object o2Substance) {

					int compareValue = 0;
					if (o1Substance != null && o2Substance != null) {
						if (o1Substance.getClass().isAssignableFrom(
								Number.class)
								&& o2Substance.getClass().isAssignableFrom(
										Number.class)) {
							compareValue = compare((Number) o1Substance,
									(Number) o2Substance);
						}else{
							compareValue = o1Substance.toString().compareTo(o2Substance.toString());
						}
					} else if (o1Substance == null && o2Substance != null) {
						compareValue = -1;
					} else if (o1Substance == null && o2Substance != null) {
						compareValue = 1;
					}
					return compareValue;
				}
				
			};
			private final Comparator<ContainerEntry> recordComparator = new Comparator<ContainerEntry>() {
				@Override
				public int compare(ContainerEntry o1, ContainerEntry o2) {
					for (Entry<Attribute, Boolean> oneEntry : sortOrder.entrySet()) {
						if (pivotedAttributes.containsKey(oneEntry.getKey())) {
							// This is already pivoted on this, So skip it during
							// sorting
							continue;
						}
						Object o1Substance = o1.getSubstance(oneEntry.getKey());
						Object o2Substance = o2.getSubstance(oneEntry.getKey());
						int compareValue = compareObject(o1Substance, o2Substance);
						if (compareValue == 0) {
							continue;
						} else {
							return compareValue * (oneEntry.getValue() ? 1 : -1);
						}
					}
					return o1.getIdentitySequence() - o2.getIdentitySequence();
				}

				private int compareObject(Object o1Substance, Object o2Substance) {
					return objectComparator.compare(o1Substance,o2Substance);
				}

				@SuppressWarnings("all")
				public int compare(Number number1, Number number2) {
					if (((Object) number2).getClass().equals(
							((Object) number1).getClass())) {
						// both numbers are instances of the same type!
						if (number1 instanceof Comparable) {
							// and they implement the Comparable interface
							return ((Comparable) number1).compareTo(number2);
						}
					}
					// for all different Number types, let's check there double
					// values
					if (number1.doubleValue() < number2.doubleValue())
						return -1;
					if (number1.doubleValue() > number2.doubleValue())
						return 1;
					return 0;
				}
			};
			/**
			 * Substance on which this pivot has been created
			 */
			private final Object substance;
			/**
			 * Parent pivot of this current pivot
			 */
			private final PivotEntry parent;
			/**
			 * Depth of this pivot
			 */
			private final int pivotDepth;
			/**
			 * Child pivots this pivot contains.
			 */
			private final TreeMap<Object, PivotEntry> childPivotEntries;
			private final TreeSet<ContainerEntry> registeredEntries;
			
			/**
			 * State of this pivot entry
			 */
			private boolean collapsed = false;
			/**
			 * Is this node dirty?
			 */
			private boolean dirty = false;

			/**
			 * Constructor to create the Pivot entry. This is the ROOT constructor
			 */
			private PivotEntry() {
				super(generateIdentity());
				this.parent = null;
				this.pivotDepth = 0;
				this.substance = DEFAULT_SUBSTANCE;
				this.registeredEntries = new TreeSet<ContainerEntry>(
						recordComparator);
				this.childPivotEntries = new TreeMap<Object, PivotEntry>(objectComparator);
				letTheWorldKnow();
			}

			/**
			 * Constructor to create the Pivot entry to contain child pivots
			 * 
			 * @param substanceAtDepth
			 *            Substance
			 * @param parent
			 *            PivotEntry
			 */
			private PivotEntry(Object substanceAtDepth, PivotEntry parent) {
				super(generateIdentity());
				this.substance = substanceAtDepth;
				this.parent = parent;
				this.pivotDepth = parent.pivotDepth + 1;
				// Initialize the leaf container only for the leaf pivot
				if (pivotDepth == pivotedAttributes.size()) {
					this.childPivotEntries = null;
					registeredEntries = new TreeSet<ContainerEntry>(
							recordComparator);
				} else {
					this.registeredEntries = null;
					childPivotEntries = new TreeMap<Object, PivotEntry>(objectComparator);
				}
				this.parent.childPivotEntries.put(substanceAtDepth, this);
				letTheWorldKnow();
			}

			/**
			 * Method broadcasts the world that a pivot entry has been created.
			 */
			private void letTheWorldKnow() {
				directPivotAccess.put(getIdentitySequence(), this);

				// Additional pivoted Entry to be created
				// Create the qualifier entries for the pivot entries
				Stack<Object> substanceStack = new Stack<Object>();
				// Create the pivot entries if required, in case exists navigate
				// till
				// the leaf pivot
				PivotEntry pivotEntry = this;
				while (pivotEntry.pivotDepth != 0) {
					substanceStack.push(pivotEntry.substance);
					pivotEntry = pivotEntry.parent;
				}
				for (Attribute oneAttribute : pivotedAttributes.keySet()) {
					if (!substanceStack.isEmpty()) {
						this.silentUpdate(oneAttribute,
								substanceStack.pop());
					} else {
						break;
					}
				}
			}

			public void reallocate(int physicalSize) {
				if (childPivotEntries != null) {
					for (PivotEntry innerPivot : childPivotEntries.values()) {
						innerPivot.reallocate(physicalSize);
					}
				}
				super.reallocate(physicalSize);
			}

			/**
			 * Fetched the child based the substance provided, Returns null in case
			 * no such child present.
			 * 
			 * @param substanceAtDepth
			 *            Substance
			 * @return PivotEntry
			 * 
			 * @throws NullPointerException
			 *             if substanceAtDepth is null
			 */
			public PivotEntry getChild(Object substanceAtDepth) {
				return childPivotEntries.get(substanceAtDepth);
			}

			/**
			 * Unregister the physical entry from this pivot. If this pivot is
			 * containing only this container entry then remove the pivot and
			 * propagate the changes to the parents.
			 * 
			 * @param containerEntry
			 */
			private void unregister(ContainerEntry containerEntry) {
				dirty = true;
				// Remove the physical data
				registeredEntries.remove(containerEntry);
				directPivotAccess.remove(containerEntry.getIdentitySequence());
				for (Attribute attribute : aggrMap.keySet()) {
					aggregateAndPropagate(attribute);
				}
				if (registeredEntries.size() == 0) {
					if (parent != null) {// NOT GROUPED
						parent.removePivot(this.substance);
					}
				}
			}

			/**
			 * Remove pivot based on this substance on the current pivot
			 * 
			 * @param substance
			 *            Substance
			 */
			private void removePivot(Object substance) {
				PivotEntry deletedEntry = childPivotEntries.remove(substance);
				directPivotAccess.remove(deletedEntry.getIdentitySequence());
				if (parent != null && childPivotEntries.isEmpty()) {
					parent.removePivot(this.substance);
				}
			}

			/**
			 * Registers the physical with the current pivot
			 * 
			 * @param containerEntry
			 *            ContainerEntry
			 */
			private void register(ContainerEntry containerEntry) {
				dirty = true;
				directPivotAccess.put(containerEntry.getIdentitySequence(), this);
				registeredEntries.add(containerEntry);
				for (Attribute attribute : aggrMap.keySet()) {
					aggregateAndPropagate(attribute);
				}
			}

			/**
			 * Aggregates and propagates the attribute vertically outward.
			 * 
			 * @param attribute
			 *            Attribute
			 */
			public void aggregateAndPropagate(Attribute attribute) {
				aggregate(attribute);
				if (parent != null)
					parent.aggregateAndPropagate(attribute);
			}

			private void aggregate(Attribute attribute) {
				if (!pivotedAttributes.containsKey(attribute)) {
					Aggregator aggregator = aggrMap.get(attribute);
					this.silentUpdate(aggregator.getTargetAttr(), aggregator
							.failSafeaggregate(this));
					for(Aggregator oneAggregator:aggregator.getChainedAggregators()){
						if (!pivotedAttributes.containsKey(oneAggregator.getTargetAttr())) {
							this.silentUpdate(oneAggregator.getTargetAttr(), oneAggregator
									.failSafeaggregate(this));
						}
					}
				}
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

			private void refreshPageView() {
				root.dirty = true;
				indexedEntries = root.getContainerEntries();
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
					for (PivotEntry pivotEntry : childPivotEntries.values()
							.toArray(new PivotEntry[0])) {
						pivotEntry.clear();
					}
				}
				if (registeredEntries != null) {
					for (ContainerEntry containerEntry : registeredEntries.toArray(new ContainerEntry[0])) {
						unregister(containerEntry);
					}
				}
			}

			public ConcreteContainerEntry[] getContainerEntries() {
				if (dirty) {
					dirty = false;
					return computeContainerEntries(
							pivotedAttributes.keySet().toArray(new Attribute[0]))
							.toArray(new ConcreteContainerEntry[0]);
				} else {
					return indexedEntries;
				}
			}

			/**
			 * Method responsible to generate view for the given collapsed
			 * structure. This list is strictly for viewing. Dont execute any
			 * operation on this list.
			 * 
			 * @param attributes
			 *            pivotedAttributed relative to this node.
			 * @return ArrayList<ContainerEntry>
			 */
			private ArrayList<ContainerEntry> computeContainerEntries(
					Attribute[] attributes) {
				ArrayList<ContainerEntry> containerEntries = new ArrayList<ContainerEntry>();
				// Root is always included.
				// Include all non terminals if they contain at least one terminal
				// Include all non terminals if they contain at least two non
				// terminal
				if (this == root || attributes.length == 0
						|| childPivotEntries.size() > 1) {
					containerEntries.add(this);
				}
				if (!collapsed) {
					if (attributes.length == 0) {
						containerEntries.addAll(registeredEntries);
					} else {
						Boolean order = sortOrder.get(attributes[0]);
						// If order not specified it is natural order.
						order = (order == null) ? true : order;
						Attribute[] innerAttributes = Arrays.copyOfRange(
								attributes, 1, attributes.length);
						for (PivotEntry pivotEntry : order ? childPivotEntries
								.values() : childPivotEntries.descendingMap()
								.values()) {
							containerEntries.addAll(pivotEntry
									.computeContainerEntries(innerAttributes));
						}
					}
				}
				return containerEntries;
			}

			public void applySort() {
				if (registeredEntries != null && registeredEntries.size() > 0) {
					ContainerEntry[] existingEntries = registeredEntries
							.toArray(new ContainerEntry[0]);
					registeredEntries.clear();
					for (ContainerEntry oneExistingEntry : existingEntries) {
						registeredEntries.add(oneExistingEntry);
					}
				} else {
					// Recursively sort the children
					for (PivotEntry pivotEntry : childPivotEntries.values()) {
						pivotEntry.applySort();
					}
				}
			}

			public void aggregateUniverse() {
				if (childPivotEntries != null && pivotedAttributes.size() > 0) {
					for (PivotEntry innerPivot : childPivotEntries.values()) {
						innerPivot.aggregateUniverse();
					}
				}
				for (Attribute attribute : aggrMap.keySet()) {
					aggregate(attribute);
				}
			}

			public void clearAggregation(Attribute attribute) {
				if (!pivotedAttributes.containsKey(attribute)) {
					if (childPivotEntries != null && pivotedAttributes.size() > 0) {
						for (PivotEntry innerPivot : childPivotEntries.values()) {
							innerPivot.clearAggregation(attribute);
						}
					}
					this.silentUpdate(attribute, null);
				}
			}

			public boolean collapse(boolean newCollapsingState) {
				if (!(collapsed == newCollapsingState)) {
					collapsed = newCollapsingState;
					return true;
				}
				return false;
			}

			public void applyPivot(ContainerEntry containerEntry) {
				PivotEntry pivotEntry = root;

				// Create the pivot entries if required, in case exists navigate
				// till
				// the leaf pivot

				for (Attribute pivotedAttribute : pivotedAttributes.keySet()) {
					Object substanceAtDepth = containerEntry
							.getSubstance(pivotedAttribute);
					substanceAtDepth = substanceAtDepth != null ? substanceAtDepth
							: DEFAULT_SUBSTANCE;
					PivotEntry childEntry = pivotEntry.getChild(substanceAtDepth);
					if (childEntry == null) {
						childEntry = new PivotEntry(substanceAtDepth, pivotEntry);
					}
					pivotEntry = childEntry;
				}
				// Register the physical entry reference
				pivotEntry.register(containerEntry);
			}

			public Iterator<? extends ContainerEntry> iterator() {
				if (childPivotEntries != null && pivotedAttributes.size() > 0) {
					return childPivotEntries.values().iterator();
				} else {
					return registeredEntries.iterator();
				}
			}
		}

		/**
		 * Behavior method to apply pivot on this container.
		 * 
		 * @param pivotArray
		 *            Attribute[]
		 * 
		 * @throws NullPointerException
		 *             if pivotArray is null
		 */
		public void applyPivot(Attribute[] pivotArray) {
			pivotedAttributes.clear();
			for (Attribute pivot : pivotArray) {
				Attribute registered = pivot.getRegisteredAttribute();
				if (registered != null) {
					//Clear any existing aggregation
					root.clearAggregation(registered);
					// Allow pivoting only on the preadded attributes
					pivotedAttributes.put(registered, true);
				}
			}
			// Can not aggregate on the pivoted columns so remove them from the
			// aggregation
			// as soon a pivot is applied
			for (Attribute attribute : pivotArray) {
				aggrMap.remove(attribute);
			}
			refilter(false);
		}
		
		@Override
		public void refilter(boolean resetSendState) {
			root.clear();
			// Re pivot everything based on new specification
			for (ContainerEntry containerEntry : getContainerDataEntries()) {
				entryAdded(containerEntry);
			}
			root.refreshPageView();
		}

		/**
		 * Behavior method to apply sort on this container.
		 * 
		 * @param sortOrder
		 *            SortOrder
		 * 
		 * @throws NullPointerException
		 *             if sortorder is null
		 */
		public void applySort(final LinkedHashMap<Attribute, Boolean> sortOrder) {
			this.sortOrder.clear();
			for (Entry<Attribute, Boolean> entry : sortOrder.entrySet()) {
				this.sortOrder.put(entry.getKey().getRegisteredAttribute(),
						entry.getValue());
			}
			root.applySort();
			root.refreshPageView();
		}

		/**
		 * Behavior method to apply aggregation on this container.
		 * 
		 * @param aggrSpec
		 *            LinkedHashMap<Attribute, Aggregator>
		 * 
		 * @throws NullPointerException
		 *             if aggrSpec is null
		 */
		public void applyAggregation(
				final LinkedHashMap<Attribute, Aggregator> aggrSpec) {
			// Clear aggregations on the attributes which no longer require
			// aggregation in new specification.
			for (Attribute oneAttribute : aggrMap.keySet()) {
				for(Aggregator aggr:aggrMap.get(oneAttribute).getChainedAggregators()){
					root.clearAggregation(aggr.getTargetAttr());
				}
				// Clear aggregations on outstanding ones
				root.clearAggregation(oneAttribute);
			}
			// Clear old stuff entirely
			this.aggrMap.clear();
			for (Aggregator oneAggregator : aggrSpec.values()) {
				oneAggregator.prepare();
				this.aggrMap.put(oneAggregator.getAggrAttr(),oneAggregator);
			}
			root.aggregateUniverse();
		}

		/**
		 * Behavior method to apply collapsing on this container. If node already in
		 * the state desired then it is no operation.
		 * 
		 * @param identity
		 *            rowNumber to be collapsed
		 * @param state
		 *            true to collapse/false to collapse
		 * 
		 * @throws NullPointerException
		 *             if aggrSpec is null
		 */

		public void applyCollapse(int identity, boolean state) {
			PivotEntry pivotEntry = directPivotAccess.get(identity);
			if (pivotEntry != null && pivotEntry.collapse(state)) {
				root.refreshPageView();
			}
		}

		public ConcreteContainerEntry[] getContainerEntries() {
			return indexedEntries != null ? indexedEntries : new ConcreteContainerEntry[0];
		}

		public int getEntryCount() {
			return indexedEntries != null ? indexedEntries.length : 0;
		}
		public void attributeRemoved(Attribute requestedAttribute){
			sortOrder.remove(requestedAttribute);
			if (aggrMap.containsKey(requestedAttribute)) {
				root.clearAggregation(requestedAttribute);
				aggrMap.remove(requestedAttribute);
				//TODO what about the expression aggregators?
			}
			if (pivotedAttributes.containsKey(requestedAttribute)) {
				pivotedAttributes.remove(requestedAttribute);
				root.clearAggregation(requestedAttribute);
				refilter(false);
			}
		}
		public void entryAdded(ContainerEntry containerEntry) {
			if(filterSpec.filter(containerEntry)){
				containerEntry.setFiltered(primeIdentity,true);
				root.applyPivot(containerEntry);
				root.refreshPageView();
			}
		}
		public void entryRemoved(ContainerEntry containerEntry) {
			PivotEntry pivotEntry = directPivotAccess.get(containerEntry
					.getIdentitySequence());
			if(pivotEntry!=null){
				pivotEntry.unregister(containerEntry);
				root.refreshPageView();
			}
		}
		public void entryUpdated(Attribute attribute, Object substance,
				ContainerEntry containerEntry) {
			// fetch the pivot holding this physical
			PivotEntry pivotEntry = directPivotAccess.get(containerEntry
					.getIdentitySequence());
			if(pivotEntry!=null){//Was it a match before?
				if(filterSpec.filter(containerEntry)){ //Yes, Still a match?
					if (pivotedAttributes.containsKey(attribute)) {
						pivotEntry.unregister(containerEntry);
						root.applyPivot(containerEntry);
						root.refreshPageView();
					} else {
						if (aggrMap.containsKey(attribute)) {
							pivotEntry.aggregateAndPropagate(attribute);
						}
						if (sortOrder.containsKey(attribute)) {
							pivotEntry.applySort();
							root.refreshPageView();
						}
					}
				}else{//Yes,No more a match?
					entryRemoved(containerEntry);
				}
			}else{//No, Is it matching now?
				entryAdded(containerEntry);
			}
		}
		public void reallocate(int physicalSize){
			root.reallocate(physicalSize);
		}
	}

	/**
	 * Create a pivot container with cascade schema and supplied pivot attribute
	 * array.
	 * 
	 * @param name
	 *            String Name of the Container
	 */
	public PivotContainer(String name, Properties props) {
		super(name, props);
	}

	@Override
	public void connect(final ConnectionEvent connectionEvent) {
		assert isConnected():"How the hell did you reach here";
		final Agent dcl = connectionEvent.getAgent();
		//When an target container requests connection
		//1. Send the connected event
		dispatchConnected(dcl,new ConnectionEvent(connectionEvent.getSource(),connectionEvent.getSink(),getKnownTransactionOrigins()));
		
		//2. Add the target container to the listener list
		listenerMap.put(connectionEvent.getSink(),buildFilterAgent(connectionEvent.getSink(),dcl));
		replay(connectionEvent);
	}
	
	@Override
	protected FilterAgent buildFilterAgent(String sink, Agent dcl) {
		return new PivotAgent(sink, dcl);
	}

	@Override
	public void dispatchEntryAdded(ContainerEntry containerEntry) {
		for(FilterAgent dcl : listenerMap.values()){
			PivotAgent pa = (PivotAgent) dcl;
			pa.entryAdded(containerEntry);
		}
	}

	@Override
	public void dispatchEntryRemoved(ContainerEntry containerEntry) {
		for(FilterAgent dcl : listenerMap.values()){
			PivotAgent pa = (PivotAgent) dcl;
			pa.entryRemoved(containerEntry);
		}
	}

	@Override
	public void dispatchEntryUpdated(Attribute attribute, Object substance,
			ContainerEntry containerEntry) {
		for(FilterAgent dcl : listenerMap.values()){
			PivotAgent pa = (PivotAgent) dcl;
			pa.entryUpdated(attribute, substance, containerEntry);
		}
	}

	protected void reComputeDefaultValues(final ConnectionEvent connectionEvent) {
		super.reComputeDefaultValues(connectionEvent);
		for(FilterAgent dcl : listenerMap.values()){
			PivotAgent pa = (PivotAgent) dcl;
			pa.reallocate(getPhysicalSize());
		}
	}
	
	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute) {
		for(FilterAgent dcl : listenerMap.values()){
			PivotAgent pa = (PivotAgent) dcl;
			pa.attributeRemoved(requestedAttribute);
		}
		super.dispatchAttributeRemoved(requestedAttribute);
	}
	@Override
	public PivotAgent getFilterAgent(String sink){
		return (PivotAgent)super.getFilterAgent(sink);
	}
}