package com.biswa.ep.subscription;

import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;
/**
 * Default Client side subscription processor.
 * 
 * @author biswa
 *
 */
public class DefaultAttributeSubscription implements AttributeSubscription {
	
	@Override
	public Substance substitute(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		Substance substance = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().substitute(subscriptionEvent);
		return substance;
	}

	@Override
	public Substance unsubscribe(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		Substance substance = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().unsubscribe(subscriptionEvent);
		return substance;
	}

	@Override
	public Substance subscribe(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		Substance substance = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
		return substance;
	}
}
