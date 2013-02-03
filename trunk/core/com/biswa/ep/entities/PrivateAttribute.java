package com.biswa.ep.entities;

import com.biswa.ep.entities.substance.NullSubstance;
import com.biswa.ep.entities.substance.Substance;
/**Class used to add attributes which is not dependent on any other attribute and need not required to be propagated.
 * 
 * @author biswa
 *
 */

final public class PrivateAttribute extends Attribute{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3161468817883721474L;
	
	/**Concrete private attribute. Can be used to add private attributes which need not be propagated.
	 * 
	 * @param name
	 */
	public PrivateAttribute(String name) {
		super(name);
	}
	
	public PrivateAttribute(String name, int i) {
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

	@Override
	final public boolean propagate() {
		return false;
	}

	@Override
	final public boolean isChannel() {
		return false;
	}

	@Override
	final public boolean isStateless() {
		return false;
	}

	@Override
	final public boolean isStatic() {
		return false;
	}	
}