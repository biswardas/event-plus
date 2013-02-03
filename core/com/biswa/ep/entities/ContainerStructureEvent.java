package com.biswa.ep.entities;

public class ContainerStructureEvent extends ContainerEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3014103905038828409L;
	private Attribute attribute;
	/**Container Event Constructor used to send attribute add/remove events.
	 * 
	 * @param source String name of the source which sends the event to sink. 
	 * @param attribute Attribute attribute supposed to be attached.
	 */
	public ContainerStructureEvent(String source, Attribute attribute) {
		super(source);
		this.attribute = attribute;
	}	
	
	/**
	 * 
	 * @return Attribute Returns the attribute
	 */
	public Attribute getAttribute(){
		return attribute;
	}
}
