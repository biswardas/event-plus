package com.biswa.ep.entities.transaction;

import java.io.Serializable;
/**Class identifies one distinct transaction traced back to the origin across process boundary.
 * 
 * @author Biswa
 *
 */
public class Transaction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5489177916207221196L;
	final private int transactionId;
	final private String origin;
	public Transaction(int transactionId,String origin){
		this.transactionId=transactionId;
		this.origin=origin;
	}
	public int getTransactionId() {
		return transactionId;
	}
	public String getOrigin() {
		return origin;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + transactionId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (transactionId != other.transactionId)
			return false;
		return true;
	}
}
