package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.subscription.SubscriptionContainerProxy;

public class ProxyDeploymentHandler extends DeploymentHandler{
	@Override
	public ConcreteContainer deploy(Container container,Context context,ContainerManager containerManager) {
		ConcreteContainer cs = new SubscriptionContainerProxy(getQualifiedName(container, context),getProperties(container.getParam()));
		super.deploy(cs, container, context, containerManager);
		return cs;
	}
}
