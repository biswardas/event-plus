package com.biswa.ep.entities;

import java.util.EventObject;


import com.biswa.ep.entities.substance.Substance;
/**The event which is sent to update the container as well as adding and dropping Attributes.
 * 
 * @author biswa
 *
 */
abstract public class ContainerEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7835838912501961664L;

	
	/**Container Event Constructor used to send events.
	 * 
	 * @param source String name of the source which sends the event to sink. 
	 */
	public ContainerEvent(String source) {
		super(source);
	}
	
	
	/**
	 * 
	 * @return Attribute Returns the attribute
	 */
	public Attribute getAttribute(){
		throw new UnsupportedOperationException("Must be overridden in subclass");
	}
	
	/**
	 * 
	 * @return TransportEntry Container Entry which is being added/removed/updated
	 */
	public TransportEntry getTransportEntry(){
		throw new UnsupportedOperationException("Must be overridden in subclass");
	}
	
	/**Entry on which this update is being applied
	 * 
	 * @return int 
	 */
	public int getIdentitySequence(){
		throw new UnsupportedOperationException("Must be overridden in subclass");
	}
	
	/**
	 * 
	 * @return Substance Substance which has been changed.
	 */
	public Substance getSubstance(){
		throw new UnsupportedOperationException("Must be overridden in subclass");
	}
	
	@Override
	public String getSource(){
		return (String)super.getSource();
	}
	
	/**To the transaction the current event belongs. 
	 * 
	 * @return int
	 */
	public int getTransactionId() {
		throw new UnsupportedOperationException("Must be overridden in subclass");
	}
}
