package com.biswa.ep.entities.aggregate;

public enum Aggregators {
	SUM(SumAggregator.class),
	AVG(AverageAggregator.class);
	final public Aggregator AGGR;
	private Aggregators(Class<? extends Aggregator> aggregatorClass){
		try {
			AGGR = aggregatorClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Error Initializing aggregator Enums",e);
		}
	}
}
