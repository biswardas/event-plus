package com.biswa.ep.subscription;

import com.biswa.ep.entities.ContainerEntry;
/**
 * Default Client side subscription processor.
 * 
 * @author biswa
 *
 */
public class SubscriptionAttrHandlerImpl implements SubscriptionAttrHandler {
	
	@Override
	public Object substitute(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		Object substance = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().substitute(subscriptionEvent);
		return substance;
	}

	@Override
	public Object unsubscribe(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		Object substance = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().unsubscribe(subscriptionEvent);
		return substance;
	}

	@Override
	public Object subscribe(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		Object substance = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
		return substance;
	}
}
