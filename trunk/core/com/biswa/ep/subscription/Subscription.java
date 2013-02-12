package com.biswa.ep.subscription;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;

/**
 * Subscription support building block defining framework for both source and
 * sink container.
 * 
 * @author biswa
 * 
 */

abstract public class Subscription extends Attribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5786417978155855402L;

	protected Subscription(String name) {
		super(name);
	}

	@Override
	final public boolean isSubscription() {
		return true;
	}

	@Override
	final public boolean propagate() {
		return false;
	}

	@Override
	final protected Substance evaluate(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		return subscribe(attribute, containerEntry);
	}

	/**
	 * Entry point for the subscription activity. Implemented by Subscription
	 * Attributes and Processors.
	 * 
	 * @param attribute
	 *            Attribute
	 * @param containerEntry
	 *            ContainerEntry
	 * @return Substance
	 * @throws Exception
	 */
	public abstract Substance subscribe(Attribute attribute,
			ContainerEntry containerEntry) throws Exception;

	/**
	 * Subscription activity cleanup. Implemented by Subscription Attributes and
	 * Processors.
	 * 
	 * @param attribute
	 *            Attribute
	 * @param containerEntry
	 *            ContainerEntry
	 */
	public abstract void unsubscribe(ContainerEntry containerEntry);
}
