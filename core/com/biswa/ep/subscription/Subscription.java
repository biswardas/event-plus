package com.biswa.ep.subscription;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;


abstract public class Subscription extends Attribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5786417978155855402L;

	protected Subscription(String name) {
		super(name);
	}

	@Override
	final public boolean isChannel() {
		return true;
	}
	
	@Override
	final public boolean propagate() {
		return false;
	}
	
	@Override
	final public boolean isStateless() {
		return false;
	}

	@Override
	final public boolean isStatic() {
		return false;
	}

	@Override
	final protected Substance evaluate(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		return subscribe(attribute,containerEntry);
	}

	public abstract Substance subscribe(Attribute attribute,ContainerEntry containerEntry) throws Exception;
	public abstract void unsubscribe(ContainerEntry containerEntry);
}
