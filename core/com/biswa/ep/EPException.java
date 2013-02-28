package com.biswa.ep;

public class EPException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7957591048176258081L;
	public EPException(String message){
		super(message);
	}
	public EPException(String message,Throwable th){
		super(message,th);
	}	
}
