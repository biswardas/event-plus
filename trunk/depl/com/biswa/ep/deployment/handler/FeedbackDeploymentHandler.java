package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.FeedbackAwareContainer;

public class FeedbackDeploymentHandler extends DeploymentHandler {
	@Override
	public ConcreteContainer deploy(Container container,Context context,ContainerManager containerManager) {
		ConcreteContainer cs = new FeedbackAwareContainer(getQualifiedName(container, context),getProperties(container.getParam()));
		deployCommon(container, cs,containerManager);
		expectConnected(container, cs);
		attachSources(container, cs,containerManager);
		return cs;
	}
}
