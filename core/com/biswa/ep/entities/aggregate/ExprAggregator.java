package com.biswa.ep.entities.aggregate;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.dyna.ConcreteAttributeProvider;

public class ExprAggregator extends Aggregator {
	private final String aggregateExpression;
	private Attribute compiledAttribute = null;
	public ExprAggregator(String aggrAttr) {
		super(aggrAttr.substring(0, aggrAttr.indexOf('=')));
		aggregateExpression = aggrAttr;
	}

	@Override
	protected Object aggregate() {
		return compiledAttribute.failSafeEvaluate(getAggrAttr(), getCurrentPivotEntry());
	}

	@Override
	public void prepare() {
		compiledAttribute = new ConcreteAttributeProvider().getAttribute(aggregateExpression);
		super.prepare();
	}

}
