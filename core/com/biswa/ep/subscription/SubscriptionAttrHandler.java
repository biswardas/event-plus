package com.biswa.ep.subscription;

import com.biswa.ep.entities.ContainerEntry;

/**
 * Sink side handler interface to dispatch subscription requests to Source
 * containers hosting Subscription Processors.
 * 
 * @author biswa
 * 
 */
public interface SubscriptionAttrHandler {

	/**
	 * Subscribes a subject for the current entity
	 * 
	 * @param subscriptionAttribute
	 *            SubscriptionAttribute
	 * @param containerEntry
	 *            ContainerEntry
	 * @return Substance
	 */
	Object subscribe(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry);

	/**
	 * Unsubscribes a subject for this container entity
	 * 
	 * @param subscriptionAttribute
	 *            SubscriptionAttribute
	 * @param containerEntry
	 *            ContainerEntry
	 * @return Substance
	 */
	Object unsubscribe(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry);

	/**
	 * replaces any previous subscription and subscribes to new subject.
	 * 
	 * @param subscriptionAttribute
	 *            SubscriptionAttribute
	 * @param containerEntry
	 *            ContainerEntry
	 * @return Substance
	 */
	Object substitute(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry);
}
