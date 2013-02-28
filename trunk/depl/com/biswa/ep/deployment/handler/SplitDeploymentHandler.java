package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.SplitContainer;

public class SplitDeploymentHandler extends DeploymentHandler {
	@Override
	public ConcreteContainer deploy(Container container,Context context,ContainerManager containerManager) {
		SplitContainer cs = new SplitContainer(getQualifiedName(container, context),getProperties(container.getParam()));
		super.deploy(cs, container, context, containerManager);
		return cs;
	}
}
