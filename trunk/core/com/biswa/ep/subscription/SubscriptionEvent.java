package com.biswa.ep.subscription;

import com.biswa.ep.EPEvent;
/**
 * Carrier object for the subscription information. This object is propagated when a client container 
 * subscribes a Subscription container.
 * @author biswa
 *
 */
public class SubscriptionEvent extends EPEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8223517204217745643L;
	
	/**
	 * The subject which was subscribed
	 * 
	 */
	final private Object subject;
	/**
	 *The subscriber information 
	 */
	private SubscriptionRequest subscriptionRequest;
	
	/**
	 * 
	 * @param subject
	 * @param source
	 * @param subscriptionRequest
	 */
	public SubscriptionEvent(Object subject,String source, SubscriptionRequest subscriptionRequest) {
		super(source);
		this.subject=subject;
		this.subscriptionRequest=subscriptionRequest;
	}
	
	/**
	 * Subscribe which is being subscribed.
	 * @return Substance
	 */
	public Object getSubject() {
		return subject;
	}
	
	/**Wrapped subscription object for this source on behalf of this entity.
	 * 
	 * @return SubscriptionRequest
	 */
	public SubscriptionRequest getSubscriptionRequest() {
		return subscriptionRequest;
	}

	@Override
	public String toString() {
		return "SubscriptionEvent [source=" + getSource() + ", subject=" + subject
				+ ", subscriptionRequest=" + subscriptionRequest + "]";
	}	
}
