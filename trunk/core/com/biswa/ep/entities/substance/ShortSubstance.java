package com.biswa.ep.entities.substance;



public class ShortSubstance extends Whole {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1251845500708700724L;
	private Short value;
	public ShortSubstance(Short value){
		this.value = value;
	}
	public Short getValue() {
		return value;
	}
}
