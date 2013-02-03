package com.biswa.ep.entities.transaction;

import java.util.EventObject;
/**
 * Transaction information carrier object.
 * @author biswa
 *
 */
public class TransactionEvent extends EventObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8744460021578275985L;
	private int transactionId;
	private String source;

	/**Event Object source is not serializable.
	 * The class maintains source object in form of String so it can be
	 * serialized over network.
	 * @param source
	 */
	public TransactionEvent(String source) {
		super(source);
		this.source=source;
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

	@Override
	public String getSource() {
		return source;
	}

}
