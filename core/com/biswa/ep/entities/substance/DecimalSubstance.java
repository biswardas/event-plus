package com.biswa.ep.entities.substance;



public class DecimalSubstance extends Real {
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
