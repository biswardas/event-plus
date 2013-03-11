package com.biswa.ep.entities.transaction;

import com.biswa.ep.EPEvent;
/**
 * Transaction information carrier object.
 * @author biswa
 *
 */
public class TransactionEvent extends EPEvent{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8744460021578275985L;
	private final int transactionId;
	private final String origin;
	public TransactionEvent(String origin) {
		this(origin,origin,0);
	}
	public TransactionEvent(String origin,int transactionId) {
		this(origin,origin,transactionId);
	}
	public TransactionEvent(String source,String origin,int transactionId) {
		super(source);
		assert origin!=null;
		this.origin=origin;
		this.transactionId = transactionId;
	}
	/**
	 * Returns the transaction ID
	 * @return int
	 */
	public int getTransactionId() {
		return transactionId;
	}
	/**
	 * Source of origin of this transaction
	 * @return String
	 */
	public String getOrigin() {
		return origin;
	}
	@Override
	public String toString() {
		return "TransactionEvent [transactionId=" + transactionId + ", origin="
				+ origin + ", source=" + getSource() + "]";
	}	
}
