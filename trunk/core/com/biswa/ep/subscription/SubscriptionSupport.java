package com.biswa.ep.subscription;
/**Subscription methods which are supported by a subscription container.
 * 
 * @author biswa
 *
 */
public interface SubscriptionSupport {
	/**Subscribes a subject for the current entity
	 * 
	 * @param subscriptionEvent SubscriptionEvent
	 */
	void subscribe(SubscriptionEvent subscriptionEvent);
	/**Unsubscribes a subject for this container entity
	 * 
	 * @param subscriptionEvent SubscriptionEvent
	 */
	void unsubscribe(SubscriptionEvent subscriptionEvent);
	
	/**replaces any previous subscription and subscribes to
	 * new subject.
	 * 
	 * @param subscriptionEvent SubscriptionEvent
	 */
	void substitute(SubscriptionEvent subscriptionEvent);
}
