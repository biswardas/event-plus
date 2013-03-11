package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.AbstractContainer;
/**Deployer Handler Interface to deploy specific type of container.
 * Every New type of container will require its respective deployment
 * handler.
 * 
 * @author biswa
 *
 */
public interface IDeployerHandler {
	/**
	 * 
	 * @param container Container definition to be deployed
	 * @param context Context in which the container has to be deployed
	 * @param containerManager ContainerManager deploy in the associated containerManager
	 * @return ContainerSchema
	 */
	AbstractContainer deploy(Container container, Context context,ContainerManager containerManager);
	
	/**Builds a qualified name for this container deployment
	 * 
	 * @param container Container
	 * @param context Context
	 * @return String
	 */
	String getQualifiedName(Container container, Context context);
	
	/**Sets expectation on the sink container that a source is
	 * about to be connected. 
	 * 
	 * @param container
	 * @param cs
	 */
	void expectConnected(Container container,
			AbstractContainer cs);

}
