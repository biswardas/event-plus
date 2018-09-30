package com.biswa.ep.entities;


public final class StaticLeafAttribute extends StaticAttribute {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2662501124134104490L;
	
	public StaticLeafAttribute(String name) {
		super(name);
	}

	@Override
	protected Object evaluate(Attribute attribute) throws Exception {
		return null;
	}
	
	@Override
	public final Attribute[] dependsOn() {
		return ZERO_DEPENDENCY;
	}
}
