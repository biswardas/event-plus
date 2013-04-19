package com.biswa.ep.provider;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.dyna.DynamicAttributeProvider;

public class ScriptEngineAttributeProvider implements DynamicAttributeProvider {
	@Override
	public Attribute getAttribute(String expression) {
		return new ScriptAttribute(expression);
	}
}
