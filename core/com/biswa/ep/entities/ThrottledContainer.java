package com.biswa.ep.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.biswa.ep.ContainerContext;
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
		/**
		 * If this flag is set this indicated the task is already enqueued on the
		 * current container for execution. At any given point of time this task should
		 * not be queued more than one time.
		 */
		private boolean activated = false;
		/**
		 * Is it executing currently?
		 */
		private boolean executing = false;
		
		@Override
		protected void runtask() {
			if(!runtimeStressed()){
				executing = true;
				if(pendingUpdates){
					agent().beginDefaultTran();			
					throttledDispatch();
					agent().commitDefaultTran();
					pendingUpdates=false;
				}
				executing = false;
				activated = false;
			}
		}
		/**This method needs some tuning. This analyzes the state of the
		 * Runtime whether its safe to dispatch next batch of updates.
		 * 
		 * @return boolean whether runtime is stressed.
		 */
		private boolean runtimeStressed() {
			if(autoThrottling){
				Runtime runtime = Runtime.getRuntime();
				double totalMemory = runtime.totalMemory();
				double maxMemory = runtime.maxMemory();
				double freeMemory = runtime.freeMemory();			
				if(totalMemory/maxMemory>0.5 && freeMemory/totalMemory<0.5){
					try {
						Thread.sleep(100);
						runtime.gc();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					agent().invokeOperation(this);
					return true;
				}
			}
			return false;			
		}
		boolean isExecuting(){
			return executing;
		}
		void activate(){
			if(!activated){
				agent().invokeOperation(this);
				activated = true;
			}
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
	private ContainerEntry[] allEntries=null;
	
	/**
	 * Change set Containing Updates
	 */
	private Map<Integer,Map<Attribute,Object>> collectedUpdates = null;
	
	/**
	 * Returns whether to throttle based on the runtime state
	 * 
	 */
	private boolean autoThrottling = false;
	/**Constructor to build throttled container.
	 * 
	 * @param name String
	 * @param props Properties
	 */
	public ThrottledContainer(String name,Properties props) {
		super(name,props);
		autoThrottling = Boolean.parseBoolean(getProperty(RUNTIME_THROTTLE));
		initThrottling();
	}
	
	protected void initThrottling(){
		allEntries=new ContainerEntry[0];
		collectedUpdates = new HashMap<Integer,Map<Attribute,Object>>();
	}
	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute) {
		super.dispatchAttributeRemoved(requestedAttribute);
		//Cleanup any updates relevant to the attribute
		for(Entry<Integer,Map<Attribute,Object>> oneEntry:collectedUpdates.entrySet()){
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
	}
	
	@Override
	protected void performExclusiveStatelessAttribution(final Agent receiver,ContainerEntry containerEntry) {	
		if(throttleTask.isExecuting()){
			super.performExclusiveStatelessAttribution(receiver,containerEntry);
		}else{
			if(isClientsAttached() && !ContainerContext.STATELESS_QUEUE.get().isEmpty()){ 
				StatelessContainerEntry slcEntry = prepareStatelessProcessing(containerEntry);
				for (Attribute notifiedAttribute : ContainerContext.STATELESS_QUEUE.get()) {
					Object substance = notifiedAttribute.failSafeEvaluate(notifiedAttribute, slcEntry); 
					substance = slcEntry.silentUpdate(notifiedAttribute, substance);				
					//Not participating in filter direct dispatch
					dispatchEntryUpdated(notifiedAttribute,substance,containerEntry);
				}
			}
			ContainerContext.STATELESS_QUEUE.get().clear();
		}
	}
	
	@Override
	public void dispatchEntryUpdated(Attribute attribute, Object substance,
			ContainerEntry containerEntry) {
		//If this entry is not added during this cycle as the entry going to be transported 
		//do not bother about individual updates.
		if(!containerEntry.markedAdded()){
			Map<Attribute,Object> attrSubstanceMap = collectedUpdates.get(containerEntry.getIdentitySequence());
			if(attrSubstanceMap==null){
				attrSubstanceMap = new HashMap<Attribute,Object>();
				collectedUpdates.put(containerEntry.getIdentitySequence(), attrSubstanceMap);
				pendingUpdates = true;
			}
			attrSubstanceMap.put(attribute,substance);
			containerEntry.markDirty(true);
		}
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
	}

	
	@Override
	final public ContainerEntry[] getLogicalEntries() {
		if(dirty){
			allEntries=super.getLogicalEntries();
			dirty=false;
		}
		return allEntries;
	}

	/**Holy grail of a throttled container. Method which dispatches all the accumulated changes 
	 * on demand.
	 * 
	 */
	protected void throttledDispatch() {
		for(ContainerEntry containerEntry:getLogicalEntries()){
			switch(containerEntry.touchMode()){
				case ContainerEntry.MARKED_DIRTY:
					Map<Attribute,Object> attrSubstanceMap = collectedUpdates.get(containerEntry.getIdentitySequence());
					for(Entry<Attribute,Object> oneAttrEntry:attrSubstanceMap.entrySet()){
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
	}
	
	@Override
	final public String[] getKnownTransactionOrigins(){
		//Throttled container can not reveal its true origins 
		//as the downstream container do not see actual transaction 
		//instead see a coalesced transaction.
		return new String[]{getName()};
	}

	
	/**Returns the timed interval for this container
	 * 
	 * @return int interval in milli seconds
	 */
	final public int getTimedInterval(){
		String interval = getProperty(TIMED_INTERVAL);
		int interValDuration = 1000;
		if(interval!=null){
			interValDuration = Integer.parseInt(interval);
		}
		return interValDuration;
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
