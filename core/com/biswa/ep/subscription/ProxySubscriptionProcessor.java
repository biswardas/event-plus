package com.biswa.ep.subscription;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;

public class ProxySubscriptionProcessor extends SubscriptionProcessor{

	public ProxySubscriptionProcessor() {
		super("Proxy");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -881999755065551441L;

	@Override
	public Substance subscribe(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		return null;
	}

	@Override
	public void unsubscribe(ContainerEntry containerEntry) {
	}

	@Override
	protected void init() {
	}
}