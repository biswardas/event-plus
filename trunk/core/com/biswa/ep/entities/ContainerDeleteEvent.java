package com.biswa.ep.entities;


public class ContainerDeleteEvent extends ContainerEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 222179228449623824L;
	private final int externalIdentity; 
	private final int transactionId;
	/**Constructor used to create Container Event while updating an container entry
	 * 
	 * @param source name of the source which sends the event to sink.
	 * @param ce Container Entry in which the substance has changed 
	 * @param attribute attribute which value has modified
	 * @param sc the new substance for the attribute
	 */
	public ContainerDeleteEvent(String source, int externalIdentity,int transactionId) {
		super(source);
		this.externalIdentity=externalIdentity;
		this.transactionId=transactionId;
		
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
}
