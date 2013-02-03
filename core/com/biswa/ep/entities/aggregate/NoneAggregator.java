package com.biswa.ep.entities.aggregate;

import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public class NoneAggregator extends Aggregator {
	@Override
	protected Substance aggregate(Substance[] inputSubstances) {
		return new ObjectSubstance("");
	}
}
