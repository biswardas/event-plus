package com.biswa.ep;
/**
 * Base class for all errors in EP framework. If you see this something serious has gone wrong.
 * @author Biswa
 *
 */
public class EPError extends Error {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9196843979154664631L;
	public EPError(String message){
		super(message);
	}
	public EPError(String message,Throwable th){
		super(message,th);
	}	
}
