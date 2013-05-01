package com.biswa.ep.entities;

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
	private Class<? extends Object> type;
	/**Create a leaf attribute from the attribute argument. Basically shred the 
	 * dependencies so it can be vertically cascaded to subsequent containers. 
	 * 
	 * @param attribute
	 */
	public LeafAttribute(Attribute attribute) {
		super(attribute.getName());
		type=attribute.getType();
	}
	
	/**Concrete leaf attribute. Can be used to add the attributes not dependent on other attributes
	 * 
	 * @param name
	 */
	public LeafAttribute(String name) {
		super(name);
	}
	
	public LeafAttribute(String name, int minor) {
		super(name,minor);
	}
	
	@Override
	protected Object evaluate(Attribute attribute,
			ContainerEntry containerEntry) {
		return null;
	}

	@Override
	public final Attribute[] dependsOn() {
		return ZERO_DEPENDENCY;
	}
	
	@Override
	public Class<? extends Object> getType(){
		if(type!=null){
			return type;
		}else{
			return super.getType();
		}
	}
}