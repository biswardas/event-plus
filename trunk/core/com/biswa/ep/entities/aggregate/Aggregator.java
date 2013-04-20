package com.biswa.ep.entities.aggregate;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.LeafAttribute;

abstract public class Aggregator{
	private final Attribute aggrAttr;
	public Aggregator(String aggrAttr){
		this.aggrAttr = new LeafAttribute(aggrAttr);
	}
	public Attribute getAggrAttr() {
		return aggrAttr;
	}
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
