package com.biswa.ep.entities;

import com.biswa.ep.entities.substance.NullSubstance;
import com.biswa.ep.entities.substance.Substance;
/**Class used to add leaf attributes which is not dependent on any other attribute
 * 
 * @author biswa
 *
 */

final public class LeafAttribute extends Attribute{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6747330522138938344L;
	
	/**Create a leaf attribute from the attribute argument. Basically shred the 
	 * dependencies so it can be vertically cascaded to subsequent containers. 
	 * 
	 * @param attribute
	 */
	public LeafAttribute(Attribute attribute) {
		super(attribute.getName());
	}
	
	/**Concrete leaf attribute. Can be used to add the attributes not dependent on other attributes
	 * 
	 * @param name
	 */
	public LeafAttribute(String name) {
		super(name);
	}
	
	public LeafAttribute(String name, int i) {
		super(name,i);
	}
	
	@Override
	protected Substance evaluate(Attribute attribute,
			ContainerEntry containerEntry) {
		return NullSubstance.NULL_SUBSTANCE;
	}

	@Override
	protected Attribute[] dependsOn() {
		return ZERO_DEPENDENCY;
	}
}