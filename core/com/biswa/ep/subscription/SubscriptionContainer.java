package com.biswa.ep.subscription;

import java.util.Properties;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.FeedbackAwareContainer;
import com.biswa.ep.entities.spec.FilterSpec;

public class SubscriptionContainer extends FeedbackAwareContainer implements SubscriptionSupport{	
	
	private SubscriptionContainerHandler subscriptionHandler;
	public SubscriptionContainer(String name, Properties props) {
		super(name, props);
		subscriptionHandler = new SubscriptionContainerHandler(this);		
	}

	@Override
	public void replay(ConnectionEvent connectionEvent) {
	}
	
	@Override
	public void entryRemoved(ContainerEvent ce) {
		deletePhysicalEntry(ce);
	}
	@Override
	public void dispatchAttributeAdded(Attribute requestedAttribute) {
		subscriptionHandler.dispatchAttributeAdded(requestedAttribute);
	}

	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute) {
	}
	
	@Override
	protected void reComputeDefaultValues(final ConnectionEvent connectionEvent) {		
	}
	
	@Override
	public void dispatchEntryAdded(ContainerEntry containerEntry) {
		subscriptionHandler.register(containerEntry);
		dirty=true;
	}

	@Override
	public void dispatchEntryRemoved(ContainerEntry containerEntry) {
		subscriptionHandler.unregister(containerEntry);
		dirty=true;
	}

	@Override
	public void dispatchEntryUpdated(Attribute attribute, Object substance,
			ContainerEntry containerEntry) {
		pendingUpdates = true;
		subscriptionHandler.collectUpdates(attribute,substance,containerEntry);
	}

	@Override
	public void subscribe(SubscriptionEvent subscriptionEvent) {
		subscriptionHandler.subscribe(subscriptionEvent);
	}

	@Override
	public void unsubscribe(SubscriptionEvent subscriptionEvent) {
		subscriptionHandler.unsubscribe(subscriptionEvent);
	}

	@Override
	public void substitute(SubscriptionEvent subscriptionEvent) {
		subscriptionHandler.substitute(subscriptionEvent);
	}
	
	@Override
	public void disconnect(ConnectionEvent connectionEvent) {
		super.disconnect(connectionEvent);
		subscriptionHandler.disconnect(connectionEvent);
	}
	
	@Override
	protected void throttledDispatch() {
		subscriptionHandler.processCollectedUpdates();
	}
	
	@Override
	public void applyFilter(final FilterSpec filterSpec){
		assert false:"Filter Operation Not supported on this type of container.";
	}

	@Override
	public void destroy() {
		subscriptionHandler.terminate();
		super.destroy();
	}	
}