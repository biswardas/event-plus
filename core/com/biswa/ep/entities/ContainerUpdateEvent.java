package com.biswa.ep.entities;

public class ContainerUpdateEvent extends ContainerEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 222179228449623824L;
	private int externalIdentity; 
	private Object sc;
	private int transactionId;
	private Attribute attribute;
	/**Constructor used to create Container Event while updating an container entry
	 * 
	 * @param source name of the source which sends the event to sink.
	 * @param externalIdentity identity of the entry to be deleted 
	 * @param attribute attribute which value has modified
	 * @param sc the new substance for the attribute
	 * @param transactionId transaction of this operation
	 */
	public ContainerUpdateEvent(String source, int externalIdentity,
			Attribute attribute,Object sc,int transactionId) {
		super(source);
		this.externalIdentity=externalIdentity;
		this.attribute=attribute;
		this.transactionId=transactionId;
		this.sc=sc;
		
	}
	/**Entry on which this update is being applied
	 * 
	 * @return int 
	 */
	public int getIdentitySequence(){
		return externalIdentity;
	}
	
	/**To the transaction the current event belongs. 
	 * 
	 * @return int
	 */
	public int getTransactionId() {
		return transactionId;
	}
	
	/**
	 * 
	 * @return Substance Substance which has been changed.
	 */
	public Object getSubstance(){
		return sc;
	}
	
	/**
	 * 
	 * @return Attribute Returns the attribute
	 */
	public Attribute getAttribute(){
		return attribute;
	}
	@Override
	public String toString() {
		return "ContainerUpdateEvent [externalIdentity=" + externalIdentity
				+ ", sc=" + sc + ", transactionId=" + transactionId
				+ ", attribute=" + attribute + "]";
	}	
}
