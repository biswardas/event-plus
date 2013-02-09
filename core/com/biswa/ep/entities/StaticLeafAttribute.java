package com.biswa.ep.entities;

import com.biswa.ep.entities.substance.NullSubstance;
import com.biswa.ep.entities.substance.Substance;

public class StaticLeafAttribute extends StaticAttribute {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2662501124134104490L;
	
	public StaticLeafAttribute(String name) {
		super(name);
	}

	@Override
	protected Substance evaluate(Attribute attribute) throws Exception {
		return NullSubstance.NULL_SUBSTANCE;
	}
	
	@Override
	public final Attribute[] dependsOn() {
		return ZERO_DEPENDENCY;
	}
}
