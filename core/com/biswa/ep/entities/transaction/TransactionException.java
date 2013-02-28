package com.biswa.ep.entities.transaction;

import com.biswa.ep.EPException;

public class TransactionException extends EPException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3716870319777721718L;

	public TransactionException(String message) {
		super(message);
	}

	public TransactionException(String message,Throwable th) {
		super(message,th);
	}
}
