package com.biswa.ep.discovery;

import java.rmi.RemoteException;

import com.biswa.ep.entities.transaction.FeedbackAgent;

public class RMIFeedbackAgentImpl implements FeedbackAgent {
	
	private String producer;
	private String consumer;
	private Connector connecter;
	public RMIFeedbackAgentImpl(String producer, String consumer){
		this.producer = producer;
		this.consumer = consumer;		
		connecter = RegistryHelper.getConnecter(consumer);
	}
	@Override
	public void completionFeedback(int transactionId) {
		try {
			connecter.receiveFeedback(consumer,producer,transactionId);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
