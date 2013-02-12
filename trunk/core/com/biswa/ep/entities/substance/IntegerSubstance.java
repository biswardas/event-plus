package com.biswa.ep.entities.substance;



public class IntegerSubstance extends Whole {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7293348453247437368L;
	private Integer value;
	public IntegerSubstance(Integer value){
		this.value = value;
	}
	public Integer getValue() {
		return value;
	}
}
