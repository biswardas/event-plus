package com.biswa.ep.entities.substance;

import com.biswa.ep.entities.substance.AbstractSubstance;


public class BooleanSubstance extends AbstractSubstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8463130090363086940L;
	private Boolean value;
	public BooleanSubstance(Boolean value){
		this.value = value;
	}
	public Boolean getValue() {
		return value;
	}
}
