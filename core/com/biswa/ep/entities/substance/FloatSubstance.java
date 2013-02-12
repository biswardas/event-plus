package com.biswa.ep.entities.substance;



public class FloatSubstance extends Real {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5162001902782688919L;
	private Float value;
	public FloatSubstance(Float value){
		this.value = value;
	}
	public Float getValue() {
		return value;
	}
}
