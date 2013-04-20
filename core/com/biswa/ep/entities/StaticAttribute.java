package com.biswa.ep.entities;


/**Type which is not allocated any memory.
 * 
 * @author biswa
 *
 */
public abstract class StaticAttribute extends Attribute{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3002054620107546209L;

	protected StaticAttribute(String name) {
		super(name);
	}

	@Override
	final void setOrdinal(int ordinal) {
		throw new UnsupportedOperationException("Can not set ordinal on a static attribute..");
	}

	@Override
	final public int getOrdinal() {
		throw new UnsupportedOperationException("Please do not ask ordinal on a static attribute..");
	}

	@Override
	final public boolean isStatic() {
		return true;
	}
	
	@Override
	public boolean propagate(){
		return false;
	}

	@Override
	final protected Object evaluate(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		return evaluate(attribute);
	}
	
	abstract protected Object evaluate(Attribute attribute) throws Exception;
	
	/** Utility method to find the concrete value in the given container entry for this attribute
	 * 
	 * @param containerEntry ContainerEntry
	 * @param name String
	 * @return Object
	 */
	final protected Object getValue(ContainerEntry containerEntry,String name){
		return getStatic(name);
	}
	
	/** Utility method to find the concrete value in the given container entry for this attribute
	 * 
	 * @param containerEntry ContainerEntry
	 * @param attribute Attribute 
	 * @return Object
	 */
	final protected Object getValue(ContainerEntry containerEntry,Attribute attribute){
		return getStatic(attribute);
	}
}
