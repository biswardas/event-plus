package com.biswa.ep;

public abstract class UncaughtExceptionHandler{
	static{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.err.println("Severe error occured in Thread:+"+t.getName());
				e.printStackTrace();		
			}
		});
	}
}
