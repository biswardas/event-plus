package com.biswa.ep;
/**
 * Base class for all exceptions in EP framework.
 * @author Biswa
 *
 */
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
