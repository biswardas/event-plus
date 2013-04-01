package com.biswa.ep.discovery;

import java.rmi.RemoteException;

import com.biswa.ep.entities.transaction.FeedbackAgent;

public class RMIFeedbackAgentImpl extends FeedbackAgent {
	
	private String producer;
	private String consumer;
	private Connector connecter;
	public RMIFeedbackAgentImpl(String producer, String consumer,Connector connecter){
		this.producer = producer;
		this.consumer = consumer;
		this.connecter = connecter;
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
	@Override
	public String getFeedBackConsumer() {
		return consumer;
	}
	@Override
	public void addFeedbackSource() {
		try {
			connecter.addFeedbackSource(consumer,producer);		
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
