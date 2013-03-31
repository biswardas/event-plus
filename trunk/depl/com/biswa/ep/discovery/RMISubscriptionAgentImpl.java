package com.biswa.ep.discovery;

import java.rmi.RemoteException;

import com.biswa.ep.entities.transaction.SubscriptionAgent;
import com.biswa.ep.subscription.SubscriptionEvent;

public class RMISubscriptionAgentImpl implements SubscriptionAgent {
	private String source;
	private Connector connecter;
	public RMISubscriptionAgentImpl(String source){
		this.source=source;
	}
	@Override
	public void subscribe(SubscriptionEvent subscriptionEvent) {
		try {
			connecter.subscribe(subscriptionEvent);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}
	@Override
	public void unsubscribe(SubscriptionEvent subscriptionEvent) {
		try {
			connecter.unsubscribe(subscriptionEvent);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}
	@Override
	public void substitute(SubscriptionEvent subscriptionEvent) {
		try {
			connecter.substitute(subscriptionEvent);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}
	@Override
	public boolean connect() {
		try {
			connecter = RegistryHelper.getConnecter(source);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
