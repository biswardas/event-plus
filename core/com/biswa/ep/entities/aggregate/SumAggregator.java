package com.biswa.ep.entities.aggregate;


public class SumAggregator extends Aggregator {

	public SumAggregator(String aggrAttr) {
		super(aggrAttr);
	}

	@Override
	protected Double aggregate(Object[] inputSubstances) {
		Double intermediateAggr = 0d;
		for(Object substance:inputSubstances){
			if(substance==null){
				continue;
			}else{
				intermediateAggr = intermediateAggr + (Double) substance;
			}
		}
		return intermediateAggr;
	}
}
