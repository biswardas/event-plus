package com.biswa.ep.deployment;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.biswa.ep.annotations.EPConType;
import com.biswa.ep.annotations.EPPublish;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.deployment.util.Publish;

public class EPDeployerImpl implements EPDeployer {


	@Override
	public String getName() throws RemoteException {
		return Deployer.getSufix();
	}
	@Override
	public void deploy(String deploymentDescriptor) throws RemoteException {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("com.biswa.ep.deployment.util");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			@SuppressWarnings("unchecked")
			JAXBElement<Context> jaxbElement = (JAXBElement<Context>) unmarshaller
					.unmarshal(new ByteArrayInputStream(deploymentDescriptor
							.getBytes()));
			
			Context context = jaxbElement.getValue();
			for(Container oneContainer:context.getContainer()){
				oneContainer.setName(oneContainer.getName()+getName());
				oneContainer.setType(EPConType.Basic.name());
				
				//Slave is always remote
				for(Listen oneListen:oneContainer.getListen()){
					oneListen.setMethod(EPPublish.RMI.name());
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
}
