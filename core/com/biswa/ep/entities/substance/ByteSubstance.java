package com.biswa.ep.entities.substance;



public class ByteSubstance extends Whole {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6975192502350234436L;
	private Byte value;
	public ByteSubstance(Byte value){
		this.value = value;
	}
	public Byte getValue() {
		return value;
	}
}
