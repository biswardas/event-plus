package com.biswa.ep.discovery;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.transaction.SubscriptionAgent;

public class RMIAccepterImpl extends Accepter {
	public RMIAccepterImpl(ContainerManager scm){
		super(scm);
	}
	
	@Override
	public void publish(AbstractContainer cs) {
		RMIListenerImpl rl = new RMIListenerImpl(cs
				.agent());
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
			RMIListener remoteObj= RegistryHelper.getRMIListener(cs.getName());
			UnicastRemoteObject.unexportObject(remoteObj, true);
			Binder binder = RegistryHelper.getBinder();
			binder.unbind(cs.getName());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}

	@Override
	public void listen(Listen listen, AbstractContainer cs) {
		String sourceName = listen.getContext()+"."+listen.getContainer();		
		Connector connecter = RegistryHelper.getConnecter(sourceName);
		try {
			connecter.connect(sourceName, cs.getName(),buildFilter(listen));
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}

	@Override
	public void replay(Listen listen, AbstractContainer cs) {
		String sourceName = listen.getContext()+"."+listen.getContainer();		
		Connector connecter = RegistryHelper.getConnecter(sourceName);
		try {
			connecter.replay(sourceName, cs.getName(),buildFilter(listen));
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}

	@Override
	public void addFeedbackSource(Feedback feedback, AbstractContainer cs) {
		String listeningSchema = feedback.getContext()+"."+feedback.getContainer();		
		Connector connecter = RegistryHelper.getConnecter(listeningSchema);
		try {
			connecter.addFeedbackSource(listeningSchema, feedbackAs(feedback,cs));
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		AbstractContainer originatingContainer = getContainerManager().getSchema(cs.getName());
		originatingContainer.agent().addFeedbackAgent(new RMIFeedbackAgentImpl(feedbackAs(feedback,cs), listeningSchema));
	}

	@Override
	public SubscriptionAgent getSubscriptionAgent(String context,
			String container) {
		return new RMISubscriptionAgentImpl(context+"."+container);
	}
}
