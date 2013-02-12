package com.biswa.ep.entities.substance;



public class LongSubstance extends Whole {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5727634988931606398L;
	private Long value;
	public LongSubstance(Long value){
		this.value = value;
	}
	public Long getValue() {
		return value;
	}
}
