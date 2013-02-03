package com.biswa.ep.entities.substance;

import com.biswa.ep.entities.substance.AbstractSubstance;


public class DecimalSubstance extends AbstractSubstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6561582484821159321L;
	private Double value;
	public DecimalSubstance(Double value){
		this.value = value;
	}
	public Double getValue() {
		return value;
	}
}
