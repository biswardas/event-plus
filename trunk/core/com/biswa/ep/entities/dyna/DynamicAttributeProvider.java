package com.biswa.ep.entities.dyna;

import java.util.Map;

import com.biswa.ep.entities.Attribute;
/**
 * Framework to provide dynamic attribution on the container. Implementation of this
 * allows to insert dynamic attribute in runtime. 
 * @author Biswa
 *
 */
public interface DynamicAttributeProvider {
	/**
	 * 
	 * @param expression String Expression to be inserted in the container.
	 * @param types Map attribute name and type to generate attribute.
	 * @return Attribute dynamically generated attribute
	 */
	public Attribute getAttribute(String expression,Map<String,Class<? extends Object>> types);
}
