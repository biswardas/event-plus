package com.biswa.ep.entities.substance;

public class PivotedSubstance extends IndirectedSubstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6290684903688909571L;
	
	public PivotedSubstance(Substance actualSubstance){
		super(actualSubstance);
	}
	
	@Override
	public boolean isAggr() {
		return true;
	}
}
