package com.biswa.ep.provider;

import java.util.Map;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.dyna.DynamicAttributeProvider;

public class ScriptEngineAttributeProvider implements DynamicAttributeProvider {
	@Override
	public Attribute getAttribute(String expression,Map<String,Class<? extends Object>> typeMap) {
		return new ScriptAttribute(expression);
	}
}
