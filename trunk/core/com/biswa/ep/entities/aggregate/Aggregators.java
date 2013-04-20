package com.biswa.ep.entities.aggregate;

public enum Aggregators {
	SUM(SumAggregator.class),
	AVG(AverageAggregator.class),
	EXPR(ExprAggregator.class);
	final public Class<? extends Aggregator> AGGR;
	private Aggregators(Class<? extends Aggregator> aggregatorClass){
		AGGR = aggregatorClass;
	}
	public Aggregator newInstance(String name){
		try {
			return AGGR.getConstructor(String.class).newInstance(name);
		} catch (Exception e) {
			throw new RuntimeException("Error Initializing aggregator Enums",e);
		}
	}
}
