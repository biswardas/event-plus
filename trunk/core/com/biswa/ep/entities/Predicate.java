package com.biswa.ep.entities;

import java.io.Serializable;

import com.biswa.ep.entities.dyna.ConcreteAttributeProvider;

public class Predicate implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4601421301686976655L;
	private transient Attribute compiledAttribute = null;
	private String predicate = null;

	public Predicate(String predicate) {
		this.predicate = predicate;
	}

	public Predicate chain(Predicate predicate) {
		return this;
	}

	public boolean visit(ContainerEntry containerEntry) {
		Boolean booleanValue = (Boolean) compiledAttribute.failSafeEvaluate(
				compiledAttribute, containerEntry);
		if (booleanValue == null)
			return false;
		return booleanValue;
	}

	public void prepare() {
		compiledAttribute = new ConcreteAttributeProvider()
				.getAttribute(predicate);
	}
}