package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.TransactionProxy;

public class TransactionDeploymentHandler extends  AbstractDeploymentHandler {

	@Override
	public AbstractContainer deploy(Container container, Context context,
			ContainerManager containerManager) {
		AbstractContainer cs = new TransactionProxy(getQualifiedName(container, context),getProperties(container.getParam()));
		expectConnected(container, cs);
		return cs;
	}
}
