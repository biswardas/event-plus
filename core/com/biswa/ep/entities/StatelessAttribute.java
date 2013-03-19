package com.biswa.ep.entities;

/**Type which is not allocated any memory.
 * 
 * @author biswa
 *
 */
public abstract class StatelessAttribute extends Attribute{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3002054620107546209L;

	protected StatelessAttribute(String name) {
		super(name);
	}

	@Override
	final void setOrdinal(int ordinal) {
		throw new UnsupportedOperationException("Can not set ordinal on a staless attribute..");
	}

	@Override
	final public int getOrdinal() {
		throw new UnsupportedOperationException("Please do not ask ordinal on a staless attribute..");
	}

	@Override
	final public boolean isStateless() {
		return true;
	}
}
