package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.PivotContainer;

public class PivotDeploymentHandler extends DeploymentHandler{

	public ConcreteContainer deploy(Container container,Context context,ContainerManager containerManager) {
		ConcreteContainer cs = new PivotContainer(getQualifiedName(container, context),getProperties(container.getParam()));
		
		deployCommon(container, cs,containerManager);
		
		expectConnected(container, cs);
		
		return cs;
	}
}
