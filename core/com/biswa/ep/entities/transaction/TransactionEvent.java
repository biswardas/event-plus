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
	private int transactionId;

	public TransactionEvent(String source) {
		super(source);
	}

	public TransactionEvent(String source,int transactionId) {
		this(source);
		this.transactionId = transactionId;
	}
	/**
	 * Returns the transaction ID
	 * @return int
	 */
	public int getTransactionId() {
		return transactionId;
	}

}
