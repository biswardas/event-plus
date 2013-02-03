package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.subscription.ChannelContainer;

public class ChannelDeploymentHandler extends DeploymentHandler {
	@Override
	public ConcreteContainer deploy(Container container,Context context,ContainerManager containerManager) {
		ConcreteContainer cs = new ChannelContainer(getQualifiedName(container, context),getProperties(container.getParam()));
		deployCommon(container, cs,containerManager);
		expectConnected(container, cs);
		attachSources(container, cs,containerManager);
		return cs;
	}
}
