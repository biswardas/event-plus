package com.biswa.ep.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import com.biswa.ep.entities.aggregate.Aggregator;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.substance.NullSubstance;
import com.biswa.ep.entities.substance.ObjectSubstance;
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
	 * Attributes pivoted on
	 */
	private final Map<Attribute,Boolean> pivotedAttributes = new LinkedHashMap<Attribute,Boolean>();
	
	private final Map<Attribute,Aggregator> aggrMap=new LinkedHashMap<Attribute, Aggregator>();

	private final Map<Attribute,Boolean> sortOrder = new LinkedHashMap<Attribute,Boolean>();
	
	private final Map<Integer,PivotEntry> directPivotAccess = new HashMap<Integer,PivotEntry>();
	/**
	 * Virtual root of all pivot / leaf entry
	 */
	private PivotEntry root;

	private ContainerEntry[] indexedEntries = null;
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
	public void dispatchEntryAdded(ContainerEntry containerEntry) {
		root.applyPivot(containerEntry);
		refreshPageView();
	}
	
	@Override
	public void dispatchEntryRemoved(ContainerEntry containerEntry){
		PivotEntry pivotEntry = directPivotAccess.get(containerEntry.getIdentitySequence());
		pivotEntry.unregister(containerEntry);
		refreshPageView();
	}
	
	@Override
	public void dispatchEntryUpdated(Attribute attribute, Substance substance, ContainerEntry containerEntry){
		// fetch the pivot holding this physical
		PivotEntry pivotEntry = directPivotAccess.get(containerEntry.getIdentitySequence()); 
		if (pivotedAttributes.containsKey(attribute)) {
			pivotEntry.unregister(containerEntry);
			root.applyPivot(containerEntry);
			refreshPageView();
		} else {
			if(aggrMap.containsKey(attribute)){
				pivotEntry.aggregateAndPropagate(attribute);
			}
			if(sortOrder.containsKey(attribute)){
				pivotEntry.applySort();
				refreshPageView();
			}
		}			
	}

	private void refreshPageView() {
		root.dirty=true;
		indexedEntries=root.getContainerEntries();
	}

	@Override
	public void dispatchAttributeAdded(Attribute requestedAttribute) {
	}

	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute) {
		sortOrder.remove(requestedAttribute);
		aggrMap.remove(requestedAttribute);
		if (pivotedAttributes.containsKey(requestedAttribute)) {
			pivotedAttributes.remove(requestedAttribute);
			rePivot();
		}
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
		private final Comparator<ContainerEntry> recordComparator = new Comparator<ContainerEntry>() {
			@Override
			public int compare(ContainerEntry o1, ContainerEntry o2) {
				for(Entry<Attribute, Boolean> oneEntry:sortOrder.entrySet()){
					if(pivotedAttributes.containsKey(oneEntry.getKey())){
						//This is already pivoted on this, So skip it during sorting
						continue;
					}
					Substance o1Substance = o1.getSubstance(oneEntry.getKey());
					Substance o2Substance = o2.getSubstance(oneEntry.getKey());
					int compareValue = o1Substance.compareTo(o2Substance);
					if(compareValue==0){
						continue;
					}else{
						return compareValue*(oneEntry.getValue()?1:-1);
					}
				}
				return o1.getIdentitySequence()-o2.getIdentitySequence();
			}
		};
		/**
		 * Substance on which this pivot has been created
		 */
		private final Substance substance;
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
		private final TreeMap<Substance, PivotEntry> childPivotEntries;
		private final TreeSet<ContainerEntry> registeredEntries;
		
		/**
		 * Summary Entry associated with this pivot.
		 */
		private ContainerEntry summaryEntry;
		/**
		 * State of this pivot entry
		 */
		private boolean collapsed=false;
		/**
		 * Is this node dirty?
		 */
		private boolean dirty = false;
		/**
		 * Constructor to create the Pivot entry. This is the ROOT constructor
		 */
		private PivotEntry() {
			this.parent = null;
			this.pivotDepth = 0;
			this.substance = DEFAULT_SUBSTANCE;
			this.registeredEntries = new TreeSet<ContainerEntry>(recordComparator);
			this.childPivotEntries = new TreeMap<Substance, PivotEntry>();			
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
		private PivotEntry(Substance substanceAtDepth,
				PivotEntry parent) {
			this.substance = substanceAtDepth;
			this.parent = parent;
			this.pivotDepth = parent.pivotDepth+1;
			// Initialize the leaf container only for the leaf pivot
			if (pivotDepth == pivotedAttributes.size()) {
				this.childPivotEntries = null;
				registeredEntries = new TreeSet<ContainerEntry>(recordComparator);
			} else {
				this.registeredEntries = null;
				childPivotEntries = new TreeMap<Substance, PivotEntry>();
			}
			this.parent.childPivotEntries.put(substanceAtDepth, this);
			letTheWorldKnow();
		}
		
		/**
		 * Method broadcasts the world that a pivot entry has been created.
		 */
		private void letTheWorldKnow() {			

			int identity=generateIdentity();
			directPivotAccess.put(identity, this);
			
			summaryEntry = getContainerEntryStore().create(identity);
			//TODO cant we do just simple create but not add to Store.
			getContainerEntryStore().remove(identity);
			
			// Additional pivoted Entry to be created
			// Create the qualifier entries for the pivot entries
			Stack<Substance> substanceStack=new Stack<Substance>();
			// Create the pivot entries if required, in case exists navigate till
			// the leaf pivot
			PivotEntry pivotEntry = this;
			while (pivotEntry.pivotDepth!=0) {
				substanceStack.push(pivotEntry.substance);
				pivotEntry=pivotEntry.parent;
			}
			for(Attribute oneAttribute:pivotedAttributes.keySet()){
				if(!substanceStack.isEmpty()){
					summaryEntry.silentUpdate(oneAttribute, substanceStack.pop());
				}else{
					break;
				}
			}
		}
		
		/**Fetched the child based the substance provided,
		 * Returns null in case no such child present.
		 * 
		 * @param substanceAtDepth Substance
		 * @return PivotEntry
		 * 
		 * @throws NullPointerException if substanceAtDepth is null
		 */
		public PivotEntry getChild(Substance substanceAtDepth) {
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
			dirty=true;
			// Remove the physical data
			registeredEntries.remove(containerEntry);
			directPivotAccess.remove(containerEntry.getIdentitySequence());
			for (Attribute attribute : aggrMap.keySet()) {
				aggregateAndPropagate(attribute);
			}
			if (registeredEntries.size() == 0) {
				if(parent!=null){//NOT GROUPED
					parent.removePivot(this.substance);
				}
			}
		}

		/**
		 * Remove pivot based on this substance on the current pivot
		 * 
		 * @param substance Substance
		 */
		private void removePivot(Substance substance) {
			PivotEntry deletedEntry = childPivotEntries.remove(substance);
			directPivotAccess.remove(deletedEntry.summaryEntry.getIdentitySequence());
			if (parent != null && childPivotEntries.isEmpty()) {
				parent.removePivot(this.substance);
			}
		}

		/**
		 * Registers the physical with the current pivot
		 * 
		 * @param containerEntry ContainerEntry
		 */
		private void register(ContainerEntry containerEntry) {
			dirty=true;
			directPivotAccess.put(containerEntry.getIdentitySequence(), this);
			registeredEntries.add(containerEntry);			
			for (Attribute attribute : aggrMap.keySet()) {
				aggregateAndPropagate(attribute);
			}
		}
		
		/**Aggregates and propagates the attribute vertically outward.
		 * 
		 * @param attribute Attribute
		 */
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
				summaryEntry.silentUpdate(attribute, aggregator.failSafeaggregate(inputSubstances
								.toArray(new Substance[0])));
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
					containerEntries[i] = pivotEntries[i].summaryEntry;
				}

			} else {// Leaf level Pivot
				containerEntries = registeredEntries.toArray(new ContainerEntry[0]);
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
				for (PivotEntry pivotEntry : childPivotEntries.values().toArray(new PivotEntry[0])) {
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

		public ContainerEntry[] getContainerEntries() {
			if(dirty){
				dirty=false;
				return computeContainerEntries(pivotedAttributes.keySet().toArray(new Attribute[0])).toArray(new ContainerEntry[0]);
			}else{
				return indexedEntries;
			}
		}
		/**Method responsible to generate view for the given collapsed structure.
		 * This list is strictly for viewing. Dont execute any operation on this
		 * list.
		 * 
		 * @param attributes pivotedAttributed relative to this node.
		 * @return ArrayList<ContainerEntry>
		 */
		private ArrayList<ContainerEntry> computeContainerEntries(Attribute[] attributes){
			ArrayList<ContainerEntry> containerEntries = new ArrayList<ContainerEntry>();
			//Root is always included.
			//Include all non terminals if they contain at least one terminal
			//Include all non terminals if they contain at least two non terminal 
			if(this==root || attributes.length==0 || childPivotEntries.size()>1){
				containerEntries.add(summaryEntry);
			}
			if(!collapsed){
				if(attributes.length==0){
					containerEntries.addAll(registeredEntries);
				}else{
					Boolean order = sortOrder.get(attributes[0]);
					//If order not specified it is natural order.
					order=(order==null)?true:order;
					Attribute[] innerAttributes=Arrays.copyOfRange(attributes,1,attributes.length);
					for(PivotEntry pivotEntry:order?childPivotEntries.values():childPivotEntries.descendingMap().values()){
						containerEntries.addAll(pivotEntry.computeContainerEntries(innerAttributes));
					}
				}
			}
			return containerEntries;
		}

		public void applySort() {
			if(registeredEntries!=null && registeredEntries.size()>0){
				ContainerEntry[] existingEntries = registeredEntries.toArray(new ContainerEntry[0]);
				registeredEntries.clear();
				for(ContainerEntry oneExistingEntry:existingEntries){
					registeredEntries.add(oneExistingEntry);
				}
			}else{
				//Recursively sort the children
				for(PivotEntry pivotEntry:childPivotEntries.values()){
					pivotEntry.applySort();
				}
			}
		}

		public void aggregateUniverse(){		
			if (childPivotEntries != null && pivotedAttributes.size()>0) {
				for(PivotEntry innerPivot:childPivotEntries.values()){
					innerPivot.aggregateUniverse();				
				}
			}
			for(Attribute attribute:aggrMap.keySet()){
				aggregate(attribute);
			}
		}

		public void clearAggregation(){		
			if (childPivotEntries != null && pivotedAttributes.size()>0) {
				for(PivotEntry innerPivot:childPivotEntries.values()){
					innerPivot.clearAggregation();				
				}
			}
			for(Attribute attribute:aggrMap.keySet()){
				summaryEntry.silentUpdate(attribute, NullSubstance.NULL_SUBSTANCE);
			}
		}
		
		public boolean collapse(boolean newCollapsingState){
			if(!(collapsed==newCollapsingState)){
				collapsed=newCollapsingState;
				return true;
			}
			return false;
		}


		public void applyPivot(ContainerEntry containerEntry) {
			PivotEntry pivotEntry = root;
			
			// Create the pivot entries if required, in case exists navigate till
			// the leaf pivot
			
			for (Attribute pivotedAttribute:pivotedAttributes.keySet()) {
				Substance substanceAtDepth = containerEntry.getSubstance(pivotedAttribute);
				substanceAtDepth = substanceAtDepth != null ? substanceAtDepth: DEFAULT_SUBSTANCE;
				PivotEntry childEntry = pivotEntry.getChild(substanceAtDepth);
				if (childEntry == null) {
					childEntry = new PivotEntry(substanceAtDepth, pivotEntry);
				}
				pivotEntry = childEntry;
			}
			// Register the physical entry reference
			pivotEntry.register(containerEntry);
		}
	}


	private void rePivot() {
		if (root != null) {
			root.clear();
			System.out.println("Total No OF Entries:"+getContainerDataEntries().length);
			// Re pivot everything based on new specification
			for (ContainerEntry containerEntry : getContainerDataEntries()) {
				if(containerEntry==root.summaryEntry) continue;
				root.applyPivot(containerEntry);
			}
			refreshPageView();
		}
	}
	
	/**Behavior method to apply pivot on this container.
	 * 
	 * @param pivotArray Attribute[]
	 * 
	 * @throws NullPointerException if pivotArray is null
	 */
	public void applyPivot(Attribute[] pivotArray) {
		pivotedAttributes.clear();
		for(Attribute pivot:pivotArray){
			Attribute registered = pivot.getRegisteredAttribute();
			if(registered!=null){
				//Allow pivoting only on the preadded attributes
				pivotedAttributes.put(registered,true);
			}
		}
		//Can not aggregate on the pivoted columns so remove them from the aggregation
		//as soon a pivot is applied
		for(Attribute attribute:pivotArray){
			aggrMap.remove(attribute);
		}
		rePivot();
	}	

	/**Behavior method to apply sort on this container.
	 * 
	 * @param sortOrder SortOrder
	 * 
	 * @throws NullPointerException if sortorder is null
	 */
	public void applySort(final LinkedHashMap<Attribute,Boolean> sortOrder){
		this.sortOrder.clear();
		for(Entry<Attribute, Boolean> entry:sortOrder.entrySet()){
			this.sortOrder.put(entry.getKey().getRegisteredAttribute(),entry.getValue());
		}
		root.applySort();
		refreshPageView();
	}
	
	/**Behavior method to apply aggregation on this container.
	 * 
	 * @param aggrSpec LinkedHashMap<Attribute, Aggregator>
	 * 
	 * @throws NullPointerException if aggrSpec is null
	 */
	public void applyAggregation(final LinkedHashMap<Attribute, Aggregator> aggrSpec) {		
		//Clear aggregations on the attributes which no longer require aggregation in new 
		//specification.
		aggrMap.keySet().removeAll(aggrSpec.keySet());
		if(!aggrMap.isEmpty()){
			//Clear aggregations on outstanding ones
			root.clearAggregation();
			//Clear old stuff entirely
			this.aggrMap.clear();
		}
		
		//Apply the new aggregation, remember we can not apply aggregation only on the
		//new attribute set instead need to aggregate every thing as old ones aggregation
		//may have changes.
		for(Attribute attribute:pivotedAttributes.keySet()){
			//TODO override pivots over ride aggregation what about when pivots are removed
			//then regular aggregations should take over.
			//TODO also when pivot is removed then the default aggregation must be
			//cleaned up
			aggrSpec.remove(attribute);
		}
		for(Entry<Attribute, Aggregator> entry:aggrSpec.entrySet()){
			this.aggrMap.put(entry.getKey().getRegisteredAttribute(),entry.getValue());
		}
		root.aggregateUniverse();
	}

	
	/**Behavior method to apply collapsing on this container. If node already in the state desired 
	 * then it is no operation.
	 * 
	 * @param identity rowNumber to be collapsed
	 * @param state true to collapse/false to collapse
	 * 
	 * @throws NullPointerException if aggrSpec is null
	 */

	public void applyCollapse(int identity, boolean state) {
		PivotEntry pivotEntry = directPivotAccess.get(identity);
		if(pivotEntry!=null && pivotEntry.collapse(state)){
			refreshPageView();
		}
	}
	
	@Override
	public ContainerEntry[] getContainerEntries() {
		return indexedEntries!=null?indexedEntries:new ContainerEntry[0];
	}

	@Override
	public int getEntryCount() {
		return indexedEntries!=null?indexedEntries.length:0;
	}
	@Override
	public void commitTran(){
		indexedEntries = root.getContainerEntries();
		super.commitTran();
	}	
	@Override
	public void rollbackTran(){
		indexedEntries = root.getContainerEntries();
		super.rollbackTran();
	}
}