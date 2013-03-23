package com.biswa.ep.deployment.handler;

import java.io.ByteArrayOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.rmi.PortableRemoteObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import com.biswa.ep.annotations.EPPublish;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.EPDeployer;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.deployment.util.Publish;
import com.biswa.ep.discovery.RegistryHelper;
import com.biswa.ep.entities.ConcreteContainer;

public class ForkJoinDeploymentHandler extends DeploymentHandler {
	@Override
	public ConcreteContainer deploy(Container container,Context context,ContainerManager containerManager) {
		ArrayList<Listen> listenList = new ArrayList<Listen>();
		try {
			JAXBContext jc = JAXBContext
					.newInstance("com.biswa.ep.deployment.util");
			Marshaller marshaller = jc.createMarshaller();
			Context transportableContext = new Context();
			transportableContext.setName(context.getName());
			transportableContext.getContainer().add(container);	
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			ByteArrayOutputStream byteArrayOPS = new ByteArrayOutputStream();
			marshaller.marshal( new JAXBElement<Context>(new QName("http://code.google.com/p/event-plus","Context"), Context.class, transportableContext ), byteArrayOPS);
			String deploymentDescriptor = byteArrayOPS.toString();
			
			//TODO load the engines to deploy
			Registry registry = RegistryHelper.getRegistry();
			Remote remote = registry.lookup("1");
			EPDeployer epDeployer = (EPDeployer) PortableRemoteObject.narrow(remote,EPDeployer.class);
			
			
			System.out.println("Attempting to Deploy Context in remote VM");
			System.out.println(deploymentDescriptor);
			epDeployer.deploy(deploymentDescriptor);
			
			addToSlaveListenerList(container, context, listenList, epDeployer);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//Dont listen usuals
		container.getListen().clear();
		//Listen to slaves
		container.getListen().addAll(listenList);
		//Set publish method to remote
		for(Publish publish:container.getPublish()){
			publish.setMethod(EPPublish.RMI.name());
		}
		//TODO What about the concurrency how to guarantee remote done with its deployment
		ConcreteContainer cs = new ConcreteContainer(getQualifiedName(container, context),getProperties(container.getParam()));
		super.deploy(cs, container, context, containerManager);
		return cs;
	}

	protected void addToSlaveListenerList(Container container, Context context,
			ArrayList<Listen> listenList, EPDeployer epDeployer)
			throws RemoteException {
		Listen listen = new Listen();
		listen.setContext(context.getName());
		listen.setContainer(container.getName()+epDeployer.getName());
		listen.setMethod(EPPublish.RMI.name());
		listenList.add(listen);
	}
}
