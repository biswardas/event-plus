package com.biswa.ep.deployment.mbean;

import java.rmi.Remote;


public class Discovery implements DiscoveryMBean {
	
	private final Remote obj;

	public Discovery(Remote obj) {
		this.obj=obj;
	}

	public Remote getObj() {
		return obj;
	}
}
