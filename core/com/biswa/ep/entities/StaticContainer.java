package com.biswa.ep.entities;

import java.util.Properties;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.store.PhysicalEntry;
import com.biswa.ep.entities.transaction.Agent;
/**This container only relays the transaction. No data or attributes are propagated from this container.
 * 
 * @author biswa
 *
 */
public class StaticContainer extends CascadeContainer {

	public StaticContainer(String name, Properties props) {
		super(name, props);
	}
	@Override
	public void connect(final ConnectionEvent connectionEvent) {
		assert isConnected():"How the hell did you reach here";
		final Agent dcl = connectionEvent.getAgent();

		//1. Write the static attributes to the requesting container 
		for(Attribute attribute:getStaticAttributes()){
			final Attribute staticAttribute = new StaticLeafAttribute(attribute.getName());
			final Object substance = getStatic(attribute);
			final ContainerEvent containerEvent = new ContainerStructureEvent(getName(),staticAttribute);
			getEventDispatcher().submit(new Runnable(){
				public void run(){
					dcl.attributeAdded(containerEvent);
					dcl.updateStatic(staticAttribute, substance, null);
				}
			});
		}
		
		final ConnectionEvent connectedEvent = new ConnectionEvent(connectionEvent.getSource(),connectionEvent.getSink(),getKnownTransactionOrigins());
		
		//2. Send connected event
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.connected(connectedEvent);
			}
		});	

		final FilterAgent filterAgent = buildFilterAgent(connectionEvent.getSink(),dcl);
		//3. Add the target container to the listener list
		listenerMap.put(connectionEvent.getSink(),filterAgent);
		FilterSpec incomingFilter = connectionEvent.getFilterSpec();
		if(incomingFilter!=null){
			incomingFilter = incomingFilter.prepare(this);
			filterAgent.setFilterSpec(filterSpec.chain(incomingFilter));
		}else{
			filterAgent.setFilterSpec(filterSpec);
		}
	}
	
	@Override
	public void replay(final ConnectionEvent connectionEvent) {
	}
	
	@Override
	public void entryAdded(ContainerEvent ce) {
	}

	@Override
	public void entryRemoved(ContainerEvent ce) {
	}

	@Override
	public void entryUpdated(ContainerEvent ce) {	}

	@Override
	public void clear() {
	}

	@Override
	public ContainerEntry[] getLogicalEntries() {
		return null;
	}
	
	@Override
	public int getEntryCount() {
		return 0;
	}

	@Override
	public ContainerEntry getConcreteEntry(int id) {
		return null;
	}
	
	@Override
	PhysicalEntry[] getPhysicalEntries() {
		return null;
	}
	@Override
	protected void propagateStatic(final Attribute attribute,
			final Object substance, final FilterSpec appliedFilter) {
		for(FilterAgent filterAgent : getFilterAgents()){
			filterAgent.agent.invokeOperation(new ContainerTask() {			
				/**
				 * 
				 */
				private static final long serialVersionUID = -4607242904007658810L;

				@Override
				protected void runtask() {
					ContainerContext.CONTAINER.get().updateStatic(attribute, substance, appliedFilter);
				}
			});
		}
	}
	@Override
	public final void dumpContainer(){
		verbose("##################Begin Dumping Container "+getName());
		for(Attribute attribute:getStaticAttributes()){
			final Object substance = getStatic(attribute);
			verbose(attribute+"="+substance);
		}
		verbose("##################End Dumping Container "+getName());
	}
	@Override
	protected void reComputeDefaultValues(final ConnectionEvent connectionEvent) {		
	}
	@Override
	public PhysicalEntry getDefaultEntry() {
		throw new IllegalStateException("I should never have been invoked"+getName());
	}
}
