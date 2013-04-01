package com.biswa.ep.discovery;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.transaction.SubscriptionAgent;

public class RMIAccepterImpl extends Accepter {
	private Map<String,Remote> map = new HashMap<String,Remote>();
	public RMIAccepterImpl(ContainerManager scm){
		super(scm);
	}
	
	@Override
	public void publish(AbstractContainer cs) {
		RMIListenerImpl rl = new RMIListenerImpl(cs
				.agent());
		map.put(cs.getName(), rl);
		RMIListener stub;
		try {
			stub = (RMIListener) UnicastRemoteObject
					.exportObject(rl, 0);
			Binder binder = RegistryHelper.getBinder();			
			binder.bind(cs.getName(), stub);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}
	
	@Override
	public void unpublish(AbstractContainer cs) {
		try {
			Binder binder = RegistryHelper.getBinder();
			binder.unbind(cs.getName());
			UnicastRemoteObject.unexportObject(map.remove(cs.getName()), true);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}

	@Override
	public boolean listen(Listen listen, AbstractContainer cs) {
		String sourceName = listen.getContext()+"."+listen.getContainer();
		try {		
			Connector connecter = RegistryHelper.getConnecter(sourceName);
			connecter.connect(sourceName, cs.getName(),buildFilter(listen));
		} catch (Exception e) {
			System.err.println(sourceName + " not available to connect. will lazily connect." );
			return false;
		}
		return true;
	}

	@Override
	public void replay(Listen listen, AbstractContainer cs) {
		String sourceName = listen.getContext()+"."+listen.getContainer();	
		try {	
			Connector connecter = RegistryHelper.getConnecter(sourceName);
			connecter.replay(sourceName, cs.getName(),buildFilter(listen));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}

	@Override
	public boolean addFeedbackSource(Feedback feedback, AbstractContainer originatingContainer) {
		String listeningSchema = feedback.getContext()+"."+feedback.getContainer();
		try {
			Connector connecter = RegistryHelper.getConnecter(listeningSchema);
			originatingContainer.agent().addFeedbackAgent(new RMIFeedbackAgentImpl(feedbackAs(feedback,originatingContainer), listeningSchema,connecter));
		} catch (Exception e) {
			System.err.println(listeningSchema + " not available to add feedback source. will lazily connect." );
			return false;
		}
		return true;
	}

	@Override
	public SubscriptionAgent getSubscriptionAgent(String context,
			String container) {
		return new RMISubscriptionAgentImpl(context+"."+container);
	}
}
