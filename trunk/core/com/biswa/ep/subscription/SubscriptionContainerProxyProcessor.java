package com.biswa.ep.subscription;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;

public class SubscriptionContainerProxyProcessor extends SubscriptionContainerProcessor{

	public SubscriptionContainerProxyProcessor() {
		super("Proxy");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -881999755065551441L;

	@Override
	public Object subscribe(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		return null;
	}

	@Override
	public void unsubscribe(ContainerEntry containerEntry) {
	}

	@Override
	protected void init() {
	}

	@Override
	protected void terminate() {		
	}
}