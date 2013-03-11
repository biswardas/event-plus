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
	 * @param externalIdentity identity of the entry to be deleted 
	 * @param transactionId transaction of this operation
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
