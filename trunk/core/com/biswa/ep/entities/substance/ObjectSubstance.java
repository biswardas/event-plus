package com.biswa.ep.entities.substance;

public class ObjectSubstance extends AbstractSubstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2737983367886840566L;
	private Object value = null;
	public ObjectSubstance(Object value) {
		this.value = value;
	}
	@Override
	public Object getValue() {
		return value;
	}

}
