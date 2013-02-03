package com.biswa.ep.entities.aggregate;

import com.biswa.ep.entities.substance.InvalidSubstance;
import com.biswa.ep.entities.substance.NullSubstance;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public class SumAggregator extends Aggregator {

	@Override
	protected Substance aggregate(Substance[] inputSubstances) {
		Double intermediateAggr = 0d;
		for(Substance substance:inputSubstances){
			if(substance==InvalidSubstance.INVALID_SUBSTANCE){
				return InvalidSubstance.INVALID_SUBSTANCE;
			}else if(substance==NullSubstance.NULL_SUBSTANCE){
				continue;
			}else{
				intermediateAggr = intermediateAggr + (Double) substance.getValue();
			}
		}
		return new ObjectSubstance(intermediateAggr);
	}
}
