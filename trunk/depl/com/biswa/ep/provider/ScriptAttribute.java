package com.biswa.ep.provider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public class ScriptAttribute extends Attribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4278153623117657403L;
	private static final ScriptEngineManager manager = new ScriptEngineManager();
	private static final ScriptEngine engine = manager
			.getEngineByName("JavaScript");
	private final String expression;

	@Override
	protected Substance evaluate(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		for (Attribute dependency : dependsOn()) {
			Object input = dependency.getValue(containerEntry);
			engine.put(dependency.getName(), input);
		}
		Object object = engine.eval(expression);
		return new ObjectSubstance(object);
	}

	public ScriptAttribute(String expression) {
		super(expression.substring(0, expression.indexOf("=")));
		EPJavaObject jfo = new EPJavaObject(expression);
		for(String oneVariable:jfo.getVariables()){
			addDependency(new LeafAttribute(oneVariable));
		}
		this.expression = expression.substring(expression.indexOf("=") + 1);
	}
}
