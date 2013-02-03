package com.biswa.ep.deployment;

import com.biswa.ep.discovery.RMIAccepterImpl;

public enum ContainerAccepter {
	RMI(RMIAccepterImpl.class),
	LOCAL(LocalAccepterImpl.class);
	Class<? extends Accepter> handlerclass= null;

	ContainerAccepter(Class<? extends Accepter> handlerclass) {
		this.handlerclass=handlerclass;
	}

	public Class<? extends Accepter> getHandler() {
		return handlerclass;
	}
}