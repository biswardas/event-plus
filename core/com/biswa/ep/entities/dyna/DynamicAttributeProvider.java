package com.biswa.ep.entities.dyna;

import com.biswa.ep.entities.Attribute;

public interface DynamicAttributeProvider {
	public Attribute getAttribute(String expression);
}
