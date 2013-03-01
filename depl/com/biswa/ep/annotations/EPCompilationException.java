package com.biswa.ep.annotations;

public class EPCompilationException extends RuntimeException {
	enum ErrorCode{ACCESS}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public EPCompilationException(String message){
		super(message);
	}
	public EPCompilationException(String message,ErrorCode errorCode){
		super(message+" [ErrorCode:"+errorCode+"]");
	}
}
