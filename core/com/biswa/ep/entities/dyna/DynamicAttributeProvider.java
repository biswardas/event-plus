package com.biswa.ep.entities.dyna;

import java.util.Map;

import com.biswa.ep.entities.Attribute;

public interface DynamicAttributeProvider {
	public Attribute getAttribute(String expression,Map<String,Class<? extends Object>> types);
}
