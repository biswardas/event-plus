package com.biswa.ep.entities.substance;


public class InvalidSubstance extends AbstractSubstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final InvalidSubstance INVALID_SUBSTANCE = new InvalidSubstance();
	private InvalidSubstance(){}
	@Override
	public String getValue() {
		return "#ERROR";
	}
	
}
