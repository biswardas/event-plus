package com.biswa.ep.deployment.handler;

import java.util.Comparator;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.JoinContainer;
import com.biswa.ep.entities.JoinContainer.JoinPolicy;

public class JoinDeploymentHandler extends DeploymentHandler {

	@Override
	public ConcreteContainer deploy(Container container, Context context,ContainerManager containerManager) {
		Comparator<ContainerEntry> predicate = new Comparator<ContainerEntry>() {
			@Override
			public int compare(ContainerEntry o1, ContainerEntry o2) {
				return 0;
			}
		};
		String leftContainer = null;
		String rightContainer = null;
		for (Listen listen : container.getListen()) {
			if ("Left".equals(listen.getSide())) {
				leftContainer = listen.getContainer();
			} else {
				rightContainer = listen.getContainer();
			}
		}
		ConcreteContainer cs = new JoinContainer(getQualifiedName(container,
				context), JoinPolicy.valueOf(container.getJoinPolicy()
				.getType()), predicate,
				context.getName() + "." + leftContainer, context.getName()
						+ "." + rightContainer,getProperties(container.getParam()));
		applyFilter(container, cs);
		expectConnected(container, cs);
		return cs;
	}

}
