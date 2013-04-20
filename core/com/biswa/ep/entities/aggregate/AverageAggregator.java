package com.biswa.ep.entities.aggregate;


public class AverageAggregator extends Aggregator {

	@Override
	protected Object aggregate(Object[] inputSubstances) {
		Double intermediateAggr = 0d;
		for(Object substance:inputSubstances){
			if(substance==null){
				continue;
			}else{
				intermediateAggr = intermediateAggr + (Double) substance;
			}
		}
		return intermediateAggr/inputSubstances.length;
	}
}
