package com.biswa.ep.subscription;

import java.util.EventObject;

import com.biswa.ep.entities.substance.Substance;
/**
 * Carrier object for the subscription information. This object is propagated when a client container 
 * subscribes a Subscription container.
 * @author biswa
 *
 */
public class SubscriptionEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8223517204217745643L;
	/**
	 * The target source which will fulfill the obligation
	 * 
	 */
	private String source;
	
	/**
	 * The subject which was subscribed
	 * 
	 */
	final private Substance subject;
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
	public SubscriptionEvent(Substance subject,String source, SubscriptionRequest subscriptionRequest) {
		super(source);
		this.subject=subject;
		this.source=source;
		this.subscriptionRequest=subscriptionRequest;
	}
	
	/**Name of the source being subscribed.
	 * @return String
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Subscribe which is being subscribed.
	 * @return Substance
	 */
	public Substance getSubject() {
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
		return "SubscriptionEvent [source=" + source + ", subject=" + subject
				+ ", subscriptionRequest=" + subscriptionRequest + "]";
	}	
}
