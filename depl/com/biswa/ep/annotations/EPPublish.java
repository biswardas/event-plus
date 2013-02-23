package com.biswa.ep.annotations;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.LocalAccepterImpl;
import com.biswa.ep.discovery.RMIAccepterImpl;

public enum EPPublish {
	RMI(RMIAccepterImpl.class),
	LOCAL(LocalAccepterImpl.class);
	Class<? extends Accepter> handlerclass= null;

	EPPublish(Class<? extends Accepter> handlerclass) {
		this.handlerclass=handlerclass;
	}

	public Class<? extends Accepter> getHandler() {
		return handlerclass;
	}
}