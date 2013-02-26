package com.biswa.ep.entities.transaction;

import com.biswa.ep.EPEvent;
/**
 * Transaction information carrier object.
 * @author biswa
 *
 */
public class FeedbackEvent extends EPEvent{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5353624173264843145L;
	private int transactionId;

	public FeedbackEvent(String source) {
		super(source);
	}

	public FeedbackEvent(String source,int transactionId) {
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
