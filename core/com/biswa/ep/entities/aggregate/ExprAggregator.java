package com.biswa.ep.entities.aggregate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.dyna.ConcreteAttributeProvider;
public class ExprAggregator extends Aggregator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6493232721862230923L;
	private static final Pattern pattern = Pattern.compile("\\s*(.*?)\\s*\\-\\>\\s*\\((\\s*(.*?)\\s*=.*)\\)");
	private final String aggregateExpression;
	private Attribute compiledAttribute = null;
	private Attribute drivingAttribute = null;
	public ExprAggregator(String aggrAttr) {
		super((getDriver(aggrAttr)).group(3));
		Matcher matcher = getDriver(aggrAttr);
		aggregateExpression = matcher.group(2);
		drivingAttribute = new LeafAttribute(matcher.group(1));
	}
	static private Matcher getDriver(String aggrAttr) {
		Matcher matcher = pattern.matcher(aggrAttr);
		if(!matcher.matches()){
			throw new RuntimeException("Invalid Expression"+aggrAttr);
		}
		return matcher;
	}

	@Override
	protected Object aggregate() {
		return compiledAttribute.failSafeEvaluate(getAggrAttr(), getCurrentPivotEntry());
	}

	@Override
	public void prepare(AbstractContainer abs) {
		compiledAttribute = new ConcreteAttributeProvider().getAttribute(aggregateExpression,abs.getTypeMap());
		super.prepare(abs);
	}
	@Override
	public final boolean isExpression(){
		return true;
	}
	@Override
	public final Attribute getAggrAttr(){
		return drivingAttribute;
	}
}
