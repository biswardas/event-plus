package com.biswa.ep.entities;


public class ContainerInsertEvent extends ContainerEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 222179228449623824L;
	private final int transactionId;
	private final TransportEntry ce;
	/** Constructor used to create Container Event while adding/deleting a container entry
	 * 
	 * @param source String name of the source which sends the event to sink.
	 * @param ce TransportEntry Entry to be added to the sink container.
	 */
	public ContainerInsertEvent(String source, TransportEntry ce,int transactionId) {
		super(source);
		this.transactionId=transactionId;
		this.ce = ce;
		
	}
	/**Entry on which this update is being applied
	 * 
	 * @return int 
	 */
	public int getIdentitySequence(){
		return ce.getIdentitySequence();
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
	 * @return TransportEntry Container Entry which is being added/removed/updated
	 */
	public TransportEntry getTransportEntry(){
		return ce;
	}
}
