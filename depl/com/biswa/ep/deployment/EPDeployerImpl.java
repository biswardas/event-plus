package com.biswa.ep.deployment;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.concurrent.Future;

import com.biswa.ep.annotations.EPConType;
import com.biswa.ep.annotations.EPPublish;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.deployment.util.Publish;
import com.biswa.ep.deployment.util.Subscribe;

public class EPDeployerImpl implements EPDeployer {
	private String name = null;
	public EPDeployerImpl(String name) {
		this.name = name;
	}
	@Override
	public String getName() throws RemoteException {
		return name;
	}
	@Override
	public void deploy(String deploymentDescriptor) throws RemoteException {
		try {			
			Context context = Deployer.buildContext(new ByteArrayInputStream(deploymentDescriptor
					.getBytes()));
			for(Container oneContainer:context.getContainer()){
				oneContainer.setName(oneContainer.getName()+getName());
				oneContainer.setType(EPConType.Basic.name());

				//Slave is always remote
				for(Listen oneListen:oneContainer.getListen()){
					oneListen.setMethod(EPPublish.RMI.name());
				}
				
				//Slave is always remote change the local subscriptions to remote
				for(Subscribe oneSubscribe:oneContainer.getSubscribe()){
					oneSubscribe.setMethod(EPPublish.RMI.name());
				}
				
				//No feedback from Slaves
				oneContainer.getFeedback().clear();
				
				//Slave is always remote
				for(Publish onePublish:oneContainer.getPublish()){
					onePublish.setMethod(EPPublish.RMI.name());
				}
			}
			Future<?> future = Deployer.deploy(context,true);
			future.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public boolean isAlive() throws RemoteException {
		return true;		
	}
	@Override
	public void shutDown() throws RemoteException {
		Deployer.asynchronouslyShutDown();		
	}
	@Override
	public void containerDeployed(String sourceName) throws RemoteException {
		Deployer.containerDeployed(sourceName);
	}
	@Override
	public void containerDestroyed(String sourceName) throws RemoteException {
		Deployer.containerDestroyed(sourceName);
	}
}
