package com.biswa.ep.entities;


/**
 * Class used to add attributes which is not dependent on any other attribute
 * and need not required to be propagated.
 * 
 * @author biswa
 * 
 */

public abstract class PrivateAttribute extends Attribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3161468817883721474L;

	/**
	 * Concrete private attribute. Can be used to add private attributes which
	 * need not be propagated.
	 * 
	 * @param name
	 */
	public PrivateAttribute(String name) {
		super(name);
	}

	@Override
	public final boolean propagate() {
		return false;
	}
}