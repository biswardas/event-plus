package com.biswa.ep.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.Agent;
/**Abstract Throttled container supports throttling of the incoming changes.
 * Any subclass of this container starts its own transaction. So please be mindful 
 * when using a throttled container in a diamond branch. 
 * 
 * @author biswa
 *
 */
public abstract class ThrottledContainer extends ConcreteContainer {
	final protected class ThrottleTask extends ContainerTask{
		/**
		 * 
		 */
		private static final long serialVersionUID = -196177963922732735L;
		private boolean queued = false;
		/**
		 * Is it coalescing currently.
		 */
		private boolean executing = false;
		@Override
		protected void runtask() {
			executing = true;
			throttledDispatch();
			executing = false;
			queued = false;
		}
		boolean isQueued(){
			return queued;
		}
		boolean isExecuting(){
			return executing;
		}
		void setQueued(){
			queued = true;
		}
	};
	final protected ThrottleTask throttleTask = new ThrottleTask();
	/**
	 * Are there any pending updates on this container?
	 */
	protected boolean pendingUpdates = false;
	
	/**
	 * Is cache dirty?
	 */
	protected boolean dirty = false;
	
	/**
	 * Cached entries in this container. Will not allow passivation?//TODO
	 */
	private ContainerEntry[] allEntries=new ContainerEntry[0];
	
	/**
	 * Change set Containing Updates
	 */
	private Map<Integer,Map<Attribute,Substance>> collectedUpdates = new HashMap<Integer,Map<Attribute,Substance>>();
	
	/**Constructor to build throttled container.
	 * 
	 * @param name String
	 * @param props Properties
	 */
	public ThrottledContainer(String name,Properties props) {
		super(name,props);
	}

	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute) {
		super.dispatchAttributeRemoved(requestedAttribute);
		//Cleanup any updates relevant to the attribute
		for(Entry<Integer,Map<Attribute,Substance>> oneEntry:collectedUpdates.entrySet()){
			oneEntry.getValue().remove(requestedAttribute);
		}
	}
	
	@Override
	public void dispatchEntryAdded(ContainerEntry containerEntry) {
		//Entry is being added so ignore previous updates on the same record
		collectedUpdates.remove(containerEntry.getIdentitySequence());
		//Remember to add it to outgoing list
		containerEntry.markAdded(true);
		pendingUpdates = true;
		dirty = true;
		check();
	}
	
	@Override
	protected void performExclusiveStatelessAttribution(final Agent receiver,ContainerEntry containerEntry) {	
		if(throttleTask.isExecuting()){
			super.performExclusiveStatelessAttribution(receiver,containerEntry);
		}else{
			if(isClientsAttached() && !ContainerContext.STATELESS_QUEUE.get().isEmpty()){ 
				StatelessContainerEntry slcEntry = prepareStatelessProcessing(containerEntry);
				for (Attribute notifiedAttribute : ContainerContext.STATELESS_QUEUE.get()) {
					Substance substance = notifiedAttribute.failSafeEvaluate(notifiedAttribute, slcEntry); 
					substance = slcEntry.silentUpdate(notifiedAttribute, substance);				
					//Not participating in filter direct dispatch
					dispatchEntryUpdated(notifiedAttribute,substance,containerEntry);
				}
			}
			ContainerContext.STATELESS_QUEUE.get().clear();
		}
	}
	
	@Override
	public void dispatchEntryUpdated(Attribute attribute, Substance substance,
			ContainerEntry containerEntry) {
		//If this entry is not added during this cycle as the entry going to be transported 
		//do not bother about individual updates.
		if(!containerEntry.markedAdded()){
			Map<Attribute,Substance> attrSubstanceMap = collectedUpdates.get(containerEntry.getIdentitySequence());
			if(attrSubstanceMap==null){
				attrSubstanceMap = new HashMap<Attribute,Substance>();
				collectedUpdates.put(containerEntry.getIdentitySequence(), attrSubstanceMap);
				pendingUpdates = true;
			}
			attrSubstanceMap.put(attribute,substance);
			containerEntry.markDirty(true);
		}
		check();
	}
	
	@Override
	public void entryRemoved(ContainerEvent ce) {
		assert log("Removing Entry"+ce.toString());
		//Obtain the physical entry
		ContainerEntry containerEntry = getConcreteEntry(ce.getIdentitySequence());
		if(containerEntry.markedAdded()){
			//Added after the last throttle cycle
			//So physically remove it.
			deletePhysicalEntry(ce);
		}else{
			//Just mark deleted don't physically removed them until
			//next throttle cycle.
			containerEntry.markRemoved(true);
			pendingUpdates = true;
		}
	}
	
	protected void deletePhysicalEntry(ContainerEvent ce){
		super.entryRemoved(ce);
		dirty = true;
	}
	
	@Override
	public void dispatchEntryRemoved(ContainerEntry containerEntry) {
		//Entry is being added so ignore previous updates on the same record
		collectedUpdates.remove(containerEntry.getIdentitySequence());
		//Since the physical entry added in the last cycle has been removed
		//No need to do anything.
		check();
	}

	@Override
	public void beginTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.beginTran();
		}
	}
	@Override
	public void commitTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.commitTran();
		}else{
			//If it is an ordinary transaction and check if qualifies for a direct dispatch.
			check();
		}
	}

	@Override
	public void rollbackTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.rollbackTran();
		}else{
			//If it is an ordinary transaction and check if qualifies for a direct dispatch.
			check();
		}
	}
	
	@Override
	final public ContainerEntry[] getContainerEntries() {
		if(dirty){
			allEntries=super.getContainerEntries();
			dirty=false;
		}
		return allEntries;
	}

	/**
	 * Scenario where no updates are generated before last cycle completed and no transaction in progress.
	 */
	protected void check(){};
	/**Holy grail of a throttled container. Method which dispatches all the accumulated changes 
	 * on demand.
	 * 
	 */
	protected void throttledDispatch() {
		if(pendingUpdates){
			agent().beginDefaultTran();			
			for(ContainerEntry containerEntry:getContainerEntries()){
				switch(containerEntry.touchMode()){
					case ContainerEntry.MARKED_DIRTY:
						Map<Attribute,Substance> attrSubstanceMap = collectedUpdates.get(containerEntry.getIdentitySequence());
						for(Entry<Attribute,Substance> oneAttrEntry:attrSubstanceMap.entrySet()){
							super.dispatchEntryUpdated(oneAttrEntry.getKey(), oneAttrEntry.getValue(), containerEntry);
						}
						containerEntry.reset();
						continue;
	
					case ContainerEntry.MARKED_REMOVED:
						//Physically delete the record
						deletePhysicalEntry(new ContainerDeleteEvent(getName(), containerEntry.getInternalIdentity(), 0));
						//We consume the removal so force a dispatch. 
						super.dispatchEntryRemoved(containerEntry);
						containerEntry.reset();
						continue;
	
					case ContainerEntry.MARKED_ADDED:
						//Dispatch the addition
						super.dispatchEntryAdded(containerEntry);
						containerEntry.reset();
						continue;
				}
			}
			
			collectedUpdates.clear();
			agent().commitDefaultTran();
			pendingUpdates=false;
		}
	}
	
	@Override
	final public String[] getKnownTransactionOrigins(){
		//Throttled container can not reveal its true origins 
		//as the downstream container do not see actual transaction 
		//instead see a coalesced transaction.
		return new String[]{getName()};
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ThrottledContainer [pendingUpdates=")
				.append(pendingUpdates).append("]\n")
				.append(super.toString());
		return builder.toString();
	}
}
