package com.biswa.ep.entities;

import java.io.Serializable;

/**Container Entry can be thought of one db record in a table.
 * 
 * @author biswa
 *
 */
public interface ContainerEntry extends Serializable{
	static final int LEFTORRIGHT=1<<30;
	static final int MARKED_ADDED=1<<29;
	static final int MARKED_REMOVED=1<<28;
	static final int MARKED_DIRTY=1<<27;
	static final int MARKED_PASSIVATED=1<<26;
	/**Which clients this entry is assigned.
	 * 
	 * @return int
	 */
	public int getToClient();

	/**
	 * @param agentPrimeIdentity
	 * @return boolean
	 * true - filter allowing this entry to be propagated
	 * false - filter denying this entry to be propagated 
	 */
	public boolean isFiltered(int agentPrimeIdentity);
	
	/**
	 * @param agentPrimeIdentity int
	 * @param filteredResult boolean
	 */
	public void setFiltered(int agentPrimeIdentity,boolean filteredResult);
	
	/**Is it a left  entry/right entry?
	 * left - true, right - false
	 * @return boolean
	 */
	public boolean isLeftTrueRightFalse();
	
	/**Sets whether this is a left table entry - 
	 * true -- belongs to left
	 * false -- belongs  to right
	 * @param leftTrueRightFalse
	 */
	public void setLeftTrueRightFalse(boolean leftTrueRightFalse);

	/** Returns the substance for the attribute.
	 * 
	 * @param attribute Attribute
	 * @return Substance
	 */
	public Object getSubstance(Attribute attribute);
	
	/**Schema to which this container entry belongs.
	 * 
	 * @return ConcreteContainer
	 */
	public ConcreteContainer getContainer();
	
	/** Silently update the container without generating any event
	 * required during intermediate updates.
	 * 
	 * @param attribute Attribute which is being updated.
	 * @param substance Substance new value
	 * @return Substance
	 */
	Object silentUpdate(Attribute attribute, Object substance);
	
	/** Silently update the container without generating any event
	 * required during intermediate updates.
	 * 
	 * @param attribute Attribute which is being updated.
	 * @param substance Substance new value
	 * @param minor int the multi value substance key
	 * @return Substance
	 */
	Object silentUpdate(Attribute attribute, Object substance,int minor);

	/** Removes the attribute from the Container Entry required when the 
	 * schema is being modified.
	 * 
	 * @param attribute Attribute which is being removed
	 */
	
	void remove(Attribute attribute);
	
	/** Removes the attribute from the Container Entry required when the 
	 * schema is being modified.
	 * 
	 * @param minor int the multi value substance key
	 * @param attribute Attribute which is being removed
	 */
	
	void remove(Attribute attribute,int minor);
	
	/**Returns the external identity of the entry
	 * 
	 * @return Returns the external identity of the entry
	 */
	public int getIdentitySequence();
	
	public boolean markedAdded();
	
	public boolean markedRemoved();
	
	public boolean markedDirty();
	
	public void markAdded(boolean flag);
	
	public void markRemoved(boolean flag);
	
	public void markDirty(boolean flag);

	public void reset();
	
	public int touchMode();	
	
	TransportEntry cloneConcrete();

	int getInternalIdentity();

	Object[] getSubstancesAsArray();

}
