package com.biswa.ep.entities.aggregate;

import com.biswa.ep.entities.substance.InvalidSubstance;
import com.biswa.ep.entities.substance.NullSubstance;
import com.biswa.ep.entities.substance.Substance;

abstract public class Aggregator{

	
	protected abstract Substance aggregate(Substance[] inputSubstances);
	
	public Substance failSafeaggregate(Substance[] inputSubstances) {
		Substance aggergatedSubstance = NullSubstance.NULL_SUBSTANCE;
		try{
			return aggregate(inputSubstances);
		}catch(Exception e){
			aggergatedSubstance = InvalidSubstance.INVALID_SUBSTANCE; 
		}
		return aggergatedSubstance;
	}
}
