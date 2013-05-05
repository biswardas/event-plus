package com.biswa.ep.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.store.PhysicalEntry;
import com.biswa.ep.subscription.Subscription;
import com.biswa.ep.subscription.SubscriptionAttribute;
/**Cascade schema manages the structural integrity of the container. Whenever an attribute is 
 * added/removed this class manages the transitive dependencies for those attributes. 
 * Key Responsibilities
 * 1. Addition & Removal of Attribute.
 * 2. Manage the inter Attribute Dependency.
 * @author biswa
 *
 */
public abstract class CascadeContainer extends AbstractContainer{
	/**AttributeMap contains the name to AttributeMapEntry mapping for the current container.
	 * 
	 * @author biswa
	 *
	 */
	final private class AttributeMap {
		/**
		 * ConcreteMap containing the attribute name to AttributeMapEntry.
		 */
		private final Map<String, AttributeMapEntry> attMapStore = new HashMap<String, AttributeMapEntry>();
		/**
		 * Sequence of the attribute in this container
		 */
		private int ordinal = 0;
		/**
		 * Oridinals which are returned to storage can be reused.
		 */
		private ArrayList<Integer> returnedOrdinalList = new ArrayList<Integer>(0);
		/**
		 * All attributes present in this container
		 */
		private String[] allAttributes = new String[0];
		/**
		 * Root Attributes for this container.
		 */
		private Attribute[] subscribedAttributes = new Attribute[0];
		
		/**
		 * Subscription Attributes added to this container.
		 */
		private Subscription[] subscriptionAttributes = new Subscription[0];
		
		/**
		 *Stateless Attributes present in this container. 
		 */
		private Attribute[] statelessAttributes = new Attribute[0];
		
		/**
		 *Static Attributes present in this container. 
		 */
		private Attribute[] staticAttributes = new Attribute[0];
		
		/**Return the AttributeMapEntry for the requested Attribute
		 * 
		 * @param attribute Attribute
		 * @return AttributeMapEntry
		 */
		private AttributeMapEntry get(Attribute attribute) {
			return attMapStore.get(attribute.getName());
		}
		
		/**Add the attribute along with its Entry
		 * 
		 * @param attribute Attribute
		 * @param attEntry AttributeMapEntry
		 */
		private void addAttribute(Attribute attribute, AttributeMapEntry attEntry) {
			if(attribute.requiresStorage()){
				attribute.setOrdinal(generateOrdinal());
			}
			attribute.prepare();
			attMapStore.put(attribute.getName(), attEntry);
		}
		
		/**Remove the attribute from the current Container
		 * 
		 * @param attribute
		 */
		private void remove(Attribute attribute) {			
			attMapStore.remove(attribute.getName());
			if(attribute.requiresStorage()){
				for(ContainerEntry containerEntry : getContainerDataEntries()){
					containerEntry.remove(attribute);
				}
				returnOrdinal(attribute.getOrdinal());
			}
		}
		
		/**Assigns ordinal to the attribute either from the reusable storage or generates a new one.
		 * 
		 * @return int
		 */
		private int generateOrdinal(){
			int assignedOrdinal = -1;
			if(returnedOrdinalList.isEmpty()){
				assignedOrdinal=ordinal++;	
			}else{
				assignedOrdinal = returnedOrdinalList.remove(0);
			}
			return assignedOrdinal;
		}
		
		/**Returns ordinal to reusable storage
		 * 
		 * @param returnedOrdinal int
		 */
		private void returnOrdinal(int returnedOrdinal){
			returnedOrdinalList.add(returnedOrdinal);
		}
		
		/**Returns the externally Subscribed attributes for the Container
		 * 
		 * @return Attribute[]
		 */
		private Attribute[] getSubscribedAttributes() {
			return subscribedAttributes;
		}


		/**Returns the stateless attributes for the Container
		 * 
		 * @return Attribute[]
		 */
		private Attribute[] getStatelessAttributes() {
			return statelessAttributes;
		}

		/**Returns the static attributes for the Container
		 * 
		 * @return Attribute[]
		 */
		private Attribute[] getStaticAttributes() {
			return staticAttributes;
		}
		
		/**Returns the externally Subscribed attributes for the Container
		 * 
		 * @return Subscription[]
		 */
		private Subscription[] getSubscriptionAttributes() {
			return subscriptionAttributes ;
		}
		
		/**Returns the Attribute names along with transitively added attributes.
		 * 
		 * @return String[]
		 */
		private String[] getAllAttributeNames() {
			return allAttributes;
		}
		
		/**Revalidates the root key set for the container
		 * 
		 * @return Attribute[]
		 */
		private Attribute[] refreshRootKeySet() {
			Collection<Attribute> rootSubscribedAttributes = new TreeSet<Attribute>(new Comparator<Attribute>(){
				@Override
				public int compare(Attribute o1, Attribute o2) {
					return o1.getName().compareTo(o2.getName());
				}				
			});
			for(AttributeMapEntry attrEntry : attMapStore.values()){				 
				if(attrEntry.attribute.propagate()){
					if(!attrEntry.attribute.isStatic()){
						rootSubscribedAttributes.add(attrEntry.attribute);
					}
				}
			}
			return rootSubscribedAttributes.toArray(Attribute.ZERO_DEPENDENCY);
		}
		
		/**Revalidates the subscription attribute for the container
		 * 
		 * @return Subscription[]
		 */
		private Subscription[] refreshSubscriptionAttributes() {
			Collection<Subscription> strCollection = new HashSet<Subscription>();
			for(AttributeMapEntry attrEntry : attMapStore.values()){				 
				if(attrEntry.attribute.isSubscription()){
					strCollection.add((Subscription) attrEntry.attribute);
				}
			}
			return strCollection.toArray(new Subscription[0]);
		}
		
		/**Revalidates the stateless attribute for the container
		 * 
		 * @return Attribute[]
		 */
		private Attribute[] refreshStatelessAttributes() {
			Queue<Attribute> statelessAttributes = new PriorityQueue<Attribute>();
			for(AttributeMapEntry attrEntry : attMapStore.values()){				 
				if(attrEntry.attribute.isStateless()){
					statelessAttributes.add(attrEntry.attribute);
				}
			}
			return statelessAttributes.toArray(Attribute.ZERO_DEPENDENCY);
		}
		
		/**Revalidates the static attribute for the container
		 * 
		 * @return Attribute[]
		 */
		private Attribute[] refreshStaticAttributes() {
			Queue<Attribute> staticAttributes = new PriorityQueue<Attribute>();
			for(AttributeMapEntry attrEntry : attMapStore.values()){				 
				if(attrEntry.attribute.isStatic()){
					staticAttributes.add(attrEntry.attribute);
				}
			}
			return staticAttributes.toArray(Attribute.ZERO_DEPENDENCY);
		}
		/**
		 * Revalidates the dependency graph
		 */
		private void revalidateDependencyGraph(){
			statelessAttributes = refreshStatelessAttributes();
			staticAttributes = refreshStaticAttributes();
			subscriptionAttributes = refreshSubscriptionAttributes();
			subscribedAttributes = refreshRootKeySet();
			allAttributes = (String[])attMapStore.keySet().toArray(new String[0]);
			for(AttributeMapEntry attrEntry : attMapStore.values()){
				if(attrEntry.isDependencyGraphDirty()){
					attrEntry.revalidateDependencyGraph();
				}
			}
		}

		@Override
		public String toString() {
			return "AttributeMap [attMapStore=" + attMapStore + "]";
		}		
	}
	/**One Attribute Entry along with its notify list and dependency queue.
	 * 
	 * @author biswa
	 *
	 */
	final private class AttributeMapEntry {
		/**
		 * Concrete Attribute.
		 */
		private final Attribute attribute;
		/**
		 * Other attributes which listen to the changes to this attribute.
		 */
		private final Map<Attribute, AtomicInteger> notifyList = new HashMap<Attribute, AtomicInteger>();
		/**
		 * Flag specifies if this dependency graph needs to be modified
		 */
		private boolean dependencyGraphDirty = true;
		/**AttributeMap Entry with attribute and entry type
		 * 
		 * @param attribute Attribute
		 */
		private AttributeMapEntry(Attribute attribute) {
			this.attribute = attribute;
		}
		/**Returns if this dependency graph is dirty.
		 * 
		 * @return boolean
		 */
		private boolean isDependencyGraphDirty() {
			return dependencyGraphDirty;
		}
		
		/**Add to the current entries notification list 
		 * 
		 * @param attribute Attribute
		 */
		private void addToNotifyList(Attribute attribute) {
			AtomicInteger listenerCount = notifyList.get(attribute);
			if (listenerCount == null) {
				notifyList.put(attribute, new AtomicInteger(1));
			} else {
				listenerCount.incrementAndGet();
			}
			dependencyGraphDirty = true;
		}
		
		/**Remove from the notification list.
		 * 
		 * @param attribute Attribute
		 */
		private void removeFromNotifyList(Attribute attribute) {
			AtomicInteger listenerCount = notifyList.get(attribute);
			if (listenerCount != null) {
				if (listenerCount.decrementAndGet() == 0) {
					notifyList.remove(attribute);
				}
			}
			dependencyGraphDirty = true;
		}
		
		/**
		 *Revalidates the dependency graph. 
		 */
		private void revalidateDependencyGraph() {
			Queue<Attribute> dependencyQueue = new PriorityQueue<Attribute>();
			Queue<Attribute> tempPriorityQueue = new PriorityQueue<Attribute>();
			tempPriorityQueue.addAll(notifyList.keySet());
			Attribute notifiedAttribute = null;
			while ((notifiedAttribute=tempPriorityQueue.poll())!=null) {
				if(!dependencyQueue.contains(notifiedAttribute)){
					//If it does not contain the notified attribute add it to dependency queue
					dependencyQueue.add(notifiedAttribute);	
				}
				//Add the cascaded dependencies to temporaryQueue
				tempPriorityQueue.addAll(attributeMap.get(notifiedAttribute).notifyList.keySet());			
			}
			attribute.setDependents(dependencyQueue.toArray(Attribute.ZERO_DEPENDENCY));
			dependencyGraphDirty=false;
		}
		@Override
		public String toString() {
			return "AttributeMapEntry [attribute=" + attribute
					+ ", notifyList=" + notifyList + "]";
		}		
	}
	/**
	 * Instance of the attributeMap
	 */
	private final AttributeMap attributeMap = new AttributeMap();
	/**Construct the Cascade Schema with the passed name.
	 * 
	 * @param name
	 */
	protected CascadeContainer(String name,Properties props) {
		super(name,props);
	}

	@Override
	public void attributeAdded(ContainerEvent ce) {
		Attribute requestedAttribute = ce.getAttribute();
		AttributeMapEntry attributeMapEntry = attributeMap
				.get(requestedAttribute);
		if (attributeMapEntry == null) {
			//New attribute please cleate an entry for me
			attributeMapEntry = new AttributeMapEntry(requestedAttribute);
			attributeMapEntry.attribute.setPropagate(true);
			dispatchAttributeAdded(requestedAttribute);
			//Manage requested attribute dependencies 
			manageAddedDependencies(attributeMapEntry);
			// Add the requested attribute
			attributeMap.addAttribute(requestedAttribute, attributeMapEntry);
			postAdded(requestedAttribute, attributeMapEntry);
		} else if(!attributeMapEntry.attribute.propagate()){
			requestedAttribute = attributeMapEntry.attribute;
			//Come here if earlier added with a transitive dependency
			//No qualified to be a root entry
			requestedAttribute.setPropagate(true);
			dispatchAttributeAdded(requestedAttribute);
			//Manage requested attribute dependencies 
			manageAddedDependencies(attributeMapEntry);
			postAdded(requestedAttribute, attributeMapEntry);
		}	
	}
	
	/**Adds the transitive attributes
	 * 
	 * @param requestedAttribute
	 * @return AttributeMapEntry
	 */
	private AttributeMapEntry addTransitiveAttribute(
			Attribute requestedAttribute) {
		AttributeMapEntry attributeMapEntry = attributeMap
				.get(requestedAttribute);
		if (attributeMapEntry == null) {
			attributeMapEntry = new AttributeMapEntry(requestedAttribute);
			//Manage transitive attribute dependencies
			manageAddedDependencies(attributeMapEntry);
			// Add the root attribute
			attributeMap.addAttribute(requestedAttribute, attributeMapEntry);
			// Attribute the transitive static attribute
			if(requestedAttribute.isStatic()){
				updateStatic(requestedAttribute,requestedAttribute.failSafeEvaluate(requestedAttribute, null),null);				
			}
		} else {
			manageAddedDependencies(attributeMapEntry);
		}
		return attributeMapEntry;
	}
	
	/**Manage the added dependencies for the attribute map entry
	 * 
	 * @param attributeMapEntry
	 */
	private void manageAddedDependencies(AttributeMapEntry attributeMapEntry) {
		Attribute[] dependsOnList = attributeMapEntry.attribute
				.dependsOn();
		for (Attribute dependsOnAttribute : dependsOnList) {
			AttributeMapEntry alreadyExistingAttributeEntry = attributeMap
					.get(dependsOnAttribute);
			if (alreadyExistingAttributeEntry == null) {
				alreadyExistingAttributeEntry = addTransitiveAttribute(dependsOnAttribute);
				alreadyExistingAttributeEntry
						.addToNotifyList(attributeMapEntry.attribute);
			} else {
				alreadyExistingAttributeEntry
						.addToNotifyList(attributeMapEntry.attribute);
				manageAddedDependencies(alreadyExistingAttributeEntry);
			}
		}
	}
	
	@Override
	public void attributeRemoved(ContainerEvent ce) {
		Attribute requestedAttribute = ce.getAttribute();
		AttributeMapEntry attributeMapEntry = attributeMap
				.get(requestedAttribute);
		if (attributeMapEntry != null) {
			//Use registered attribute hence forth
			requestedAttribute = attributeMapEntry.attribute;
			if (requestedAttribute.propagate()) {
				// Manage dependencies
				manageRemovedDependencies(requestedAttribute);
				dispatchAttributeRemoved(requestedAttribute);
				// Degrade it to non propagating nature
				requestedAttribute.setPropagate(false);
				// If there are no listeners on it remove itself
				if (attributeMapEntry.notifyList.isEmpty()){
					attributeMap.remove(requestedAttribute);
				}
				attributeMap.revalidateDependencyGraph();
			}
		}
	}

	/**Removes any transitively added attribute.
	 * 
	 * @param requestedAttribute
	 */
	private void removeTransitiveAttribute(
			Attribute requestedAttribute) {
		AttributeMapEntry attributeMapEntry = attributeMap
				.get(requestedAttribute);
		manageRemovedDependencies(attributeMapEntry.attribute);
		if (!attributeMapEntry.attribute.propagate()) {
			// If there are no listeners on it remove itself
			if (attributeMapEntry.notifyList.isEmpty())
				attributeMap.remove(attributeMapEntry.attribute);
		}
	}
	
	/**Cleanup any removal dependency
	 * 
	 * @param requestedAttribute Attribute
	 */
	private void manageRemovedDependencies(Attribute requestedAttribute) {
		// Manages its dependencies
		Attribute[] dependsOnList = requestedAttribute.dependsOn();
		for (Attribute dependsOnAttribute : dependsOnList) {
			AttributeMapEntry alreadyExistingAttributeEntry = attributeMap
					.get(dependsOnAttribute);
			//unregister from its dependencies list
			alreadyExistingAttributeEntry.removeFromNotifyList(requestedAttribute);
			//remove the transitive dependencies
			removeTransitiveAttribute(dependsOnAttribute);
		}
	}
	
	@Override
	final public Attribute getAttributeByName(String attributeName) {
		AttributeMapEntry attEntry = attributeMap.attMapStore.get(attributeName); 
		return attEntry==null?null:attEntry.attribute;
	}
	
	@Override
	final public Attribute[] getSubscribedAttributes() {
		return attributeMap.getSubscribedAttributes();
	}

	@Override
	final public Attribute[] getStatelessAttributes() {
		return attributeMap.getStatelessAttributes();
	}
	
	@Override
	final public Attribute[] getStaticAttributes() {
		return attributeMap.getStaticAttributes();
	}
	
	@Override
	final public String[] getAllAttributeNames() {
		return attributeMap.getAllAttributeNames();
	}
	/**
	 * cleanup method to destroy any subscriptions in this container, when an entry is removed.
	 */
	protected void destroy(ContainerEntry containerEntry){
		for(Subscription subsAttribute:attributeMap.getSubscriptionAttributes()){
			subsAttribute.unsubscribe(containerEntry);
		}		
	}
	
	/**Returns the raw data entry container for the given schema. This will  
	 * be containing the private entries so should not be used to send data back to
	 * the subsequent container.
	 * 
	 * @return PhysicalEntry[]
	 */
	abstract PhysicalEntry[] getContainerDataEntries();
	
	/**Returns the allocation size for the physical entry.
	 * 
	 * @return int
	 */
	final public int getPhysicalSize(){
		return attributeMap.ordinal;
	}
	
	/**This method performs post addition activities and the initializations.
	 * 
	 * @param requestedAttribute
	 * @param attributeMapEntry
	 */
	private void postAdded(Attribute requestedAttribute,
			AttributeMapEntry attributeMapEntry) {
		attributeMap.revalidateDependencyGraph();
		if(isConnected()){
			reComputeDefaultValues(null);
		}
		if(requestedAttribute.isStatic()){
			updateStatic(requestedAttribute, requestedAttribute.failSafeEvaluate(requestedAttribute, null),null);	
		}else{
			dispatchDataUpdates(attributeMapEntry.attribute);
		}
	}
	
	@Override
	public void connected(final ConnectionEvent connectionEvent) {
		reComputeDefaultValues(connectionEvent);
		super.connected(connectionEvent);
	}
	
	/**
	 * Method computes default values for the container. If a container does not require default values to
	 * be computed then over ride this to be no op.
	 * @param connectionEvent ConnectionEvent
	 */
	protected void reComputeDefaultValues(final ConnectionEvent connectionEvent) {
		if(getPhysicalSize()>0){
			PhysicalEntry defaultEntry = getDefaultEntry();
			defaultEntry.reallocate(getPhysicalSize());
			ArrayList<AttributeMapEntry> attMapEntries = new ArrayList<AttributeMapEntry>();
			attMapEntries.addAll(attributeMap.attMapStore.values());
			Collections.sort(attMapEntries, new Comparator<AttributeMapEntry>(){
				@Override
				public int compare(AttributeMapEntry o1, AttributeMapEntry o2) {
					return o1.attribute.compareTo(o2.attribute);
				}
			});
			for(AttributeMapEntry oneEntry:attMapEntries){
				Attribute attribute = oneEntry.attribute;
				if(attribute.isSubscription()){
					//TODO revisit the implementation below wrt subscription.
					SubscriptionAttribute subs = (SubscriptionAttribute)attribute;
					// If Receiving connected message(from any source) while container was disconnected.
					// OR receiving a re-connected message(from the source) this attribute cares. 
					//Subscriptions are needed to be performed again as subscription source getting reconnected.
					if(!isConnected() || (connectionEvent!=null && connectionEvent.getSource().equals(subs.getSource()))){
						//Connect the subscription agent
						if(subs.getSubAgent().connect()){
							for(ContainerEntry containerEntry:getContainerEntries()){
								subs.failSafeEvaluate(subs, containerEntry);
							}
						}
					}
				} else if(!attribute.isStateless() && !attribute.isStatic()){
					defaultEntry.silentUpdate(attribute,attribute.failSafeEvaluate(attribute, defaultEntry));	
				}
			}
		}
	}

	public abstract PhysicalEntry getDefaultEntry();

	/**Updates the static substance associated with the container, and the updates 
	 * are propagated to entries appropriate by the filter.
	 * 
	 * @param incomingAttribute Attribute
	 * @param substance Substance
	 * @param appliedFilter FilterSpec
	 */
	public void updateStatic(Attribute incomingAttribute,Object substance,FilterSpec appliedFilter) {
		Attribute attribute = incomingAttribute.getRegisteredAttribute();
		if(attribute != null){
			staticStorage.put(attribute,substance);
			propagateStatic(attribute, substance, appliedFilter);
			//Attribute all the static attributes first
			boolean hasNonStaticDependency = false;
			for(Attribute dependentAttribute:attribute.getDependents()){
				if(dependentAttribute.isStatic()){
					Object staticsubstance = dependentAttribute.failSafeEvaluate(attribute,null);
					staticStorage.put(dependentAttribute,staticsubstance);
					propagateStatic(dependentAttribute, staticsubstance, appliedFilter);
				}else{
					hasNonStaticDependency = true;
				}
			}
	
			//For applicable container entries
			//Oh yeah Rocket science here
			if(hasNonStaticDependency){
				agent().beginDefaultTran();
				if(appliedFilter==null){
					for(ContainerEntry containerEntry:getContainerEntries()){
						updateStatic(containerEntry,attribute);
					}	
				} else {
					appliedFilter.prepare(this);
					for(ContainerEntry containerEntry:getContainerEntries()){
						if(appliedFilter.filter(containerEntry)){
							updateStatic(containerEntry,attribute);
						}
					}
				}
				agent().commitDefaultTran();
			}			
		} else {
			log("Unknown Attribute: "+incomingAttribute);
		}
	}
	
	protected void propagateStatic(Attribute incomingAttribute,Object substance,FilterSpec appliedFilter){		
	}
	
	private void updateStatic(final ContainerEntry containerEntry,final Attribute attribute){
		ContainerTask containerTask = new ContainerTask(containerEntry.getIdentitySequence()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5538690989017407926L;

			@Override
			protected void runtask() {
				for(Attribute dependentAttribute:attribute.getDependents()){
					if(!dependentAttribute.isStatic()){
						processNotifiedAttribute(attribute,containerEntry,dependentAttribute);
					}
				}
				//Perform the stateless attribution
				performPostUpdateStatelessAttribution(containerEntry);				
			}
		};
		agent().getTaskHandler().executeNow(containerTask);
	}
	
	/**Evaluates the notified attribute and updates store appropriately.
	 * 
	 * @param notifyingAttribute Attribute
	 * @param containerEntry ContainerEntry
	 * @param notifiedAttribute Attribute
	 */
	final protected void processNotifiedAttribute(Attribute notifyingAttribute,
			ContainerEntry containerEntry, Attribute notifiedAttribute) {
		if(notifiedAttribute.isStateless()){
			//defer the stateless attribution
			ContainerContext.STATELESS_QUEUE.get().add(notifiedAttribute);
		}else{
			//Evaluate the being notified attribute
			Object substance = notifiedAttribute.failSafeEvaluate(notifyingAttribute, containerEntry);
			//Update the notified attribute entry
			substance = containerEntry.silentUpdate(notifiedAttribute, substance);
			//Dispatch it to the downstream containers.
			dispatchEntryUpdated(notifiedAttribute,substance,containerEntry);
		}
	}
	
	/**Method generates the entry update events when the attribute is later added / promoted to subscribed attributes.
	 * 
	 * @param attribute
	 */
	private void dispatchDataUpdates(Attribute attribute) {
		for(PhysicalEntry containerEntry : getContainerDataEntries()){
			containerEntry.reallocate(getPhysicalSize());
			Object substance  = attribute.failSafeEvaluate(attribute, containerEntry);
			if(!attribute.isStateless()){
				ContainerEvent ce = new ContainerUpdateEvent(getName(),
						containerEntry.getInternalIdentity(),
						attribute, substance
						,getCurrentTransactionID());
				entryUpdated(ce);
			}else{
				dispatchEntryUpdated(attribute, substance, containerEntry);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String attributeName:getAllAttributeNames()){
			Attribute attr = getAttributeByName(attributeName);
			sb.append(attr.toDependencyString());			
			sb.append("\n");
		}
		return sb.toString();
	}		
}