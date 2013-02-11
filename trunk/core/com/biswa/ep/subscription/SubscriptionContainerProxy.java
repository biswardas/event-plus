package com.biswa.ep.subscription;

import java.util.Properties;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.substance.Substance;

public class SubscriptionContainerProxy extends ConcreteContainer implements
		SubscriptionSupport {

	SubscriptionContainerHandler subscriptionHandler = new SubscriptionContainerHandler(this);

	public SubscriptionContainerProxy(String name, Properties props) {
		super(name, props); 
		agent().attributeAdded(new ContainerStructureEvent(getName(),
				new SubscriptionContainerProxyProcessor()));
	}

	@Override
	public void replay(ConnectionEvent connectionEvent) {
	}

	@Override
	public void dispatchAttributeAdded(Attribute requestedAttribute) {
		subscriptionHandler.dispatchAttributeAdded(requestedAttribute);
	}

	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute) {
	}

	@Override
	public void dispatchEntryAdded(ContainerEntry containerEntry) {
		// Add the subscription
		subscriptionHandler.register(containerEntry);
	}

	@Override
	public void dispatchEntryRemoved(ContainerEntry containerEntry) {
		// Remove the subscription
		subscriptionHandler.unregister(containerEntry);
	}

	@Override
	public void dispatchEntryUpdated(Attribute attribute, Substance substance,
			ContainerEntry containerEntry) {
		subscriptionHandler.dispatchEntryUpdated(attribute, substance,
				containerEntry);
	}

	@Override
	public void disconnect(ConnectionEvent connectionEvent) {
		super.disconnect(connectionEvent);
		subscriptionHandler.disconnect(connectionEvent);
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
	public void applyFilter(final FilterSpec filterSpec) {
		assert false : "Filter Operation Not supported on this type of container.";
	}
}