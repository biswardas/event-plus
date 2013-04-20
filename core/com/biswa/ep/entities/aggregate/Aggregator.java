package com.biswa.ep.entities.aggregate;

abstract public class Aggregator{

	
	protected abstract Object aggregate(Object[] inputSubstances);
	
	public Object failSafeaggregate(Object[] inputSubstances) {
		Object aggergatedSubstance = null;
		try{
			return aggregate(inputSubstances);
		}catch(Exception e){
			aggergatedSubstance = null; 
		}
		return aggergatedSubstance;
	}
}
