package com.biswa.ep.subscription;

import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;

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
	Substance subscribe(SubscriptionAttribute subscriptionAttribute,
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
	Substance unsubscribe(SubscriptionAttribute subscriptionAttribute,
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
	Substance substitute(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry);
}