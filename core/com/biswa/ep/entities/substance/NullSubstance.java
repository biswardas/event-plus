package com.biswa.ep.entities.substance;


public class NullSubstance extends AbstractSubstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6469567301201322642L;
	public static final NullSubstance NULL_SUBSTANCE = new NullSubstance();
	private NullSubstance(){}
	@Override
	public String getValue() {
		return "#NULL";
	}	
}
