package com.biswa.ep.entities.transaction;

import com.biswa.ep.subscription.SubscriptionEvent;

public class SubscriptionAgentImpl implements SubscriptionAgent {
	private Agent agent;
	public SubscriptionAgentImpl(Agent agent){
		this.agent=agent;
	}
	@Override
	public void subscribe(SubscriptionEvent subscriptionEvent) {
		agent.subscribe(subscriptionEvent);
	}
	@Override
	public void unsubscribe(SubscriptionEvent subscriptionEvent) {
		agent.unsubscribe(subscriptionEvent);
		
	}
	@Override
	public void substitute(SubscriptionEvent subscriptionEvent) {
		agent.substitute(subscriptionEvent);		
	}
}
