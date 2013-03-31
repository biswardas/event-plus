package com.biswa.ep.entities.transaction;

import com.biswa.ep.subscription.SubscriptionSupport;

public interface SubscriptionAgent extends SubscriptionSupport{
	/**
	 * Method responsible to connect to source before any subscription attemp is made.
	 */
	boolean connect();
}
