package com.biswa.ep.entities.aggregate;


public class SumAggregator extends Aggregator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4302847925466344642L;

	public SumAggregator(String aggrAttr) {
		super(aggrAttr);
	}

	@Override
	protected Object aggregate() {
		Double intermediateAggr = 0d;
		while(hasNext()){
			Object substance = getNextObject();
			if(substance==null){
				continue;
			}else{
				intermediateAggr = intermediateAggr + (Double) substance;
			}
		}
		return intermediateAggr;
	}
}
