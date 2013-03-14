package com.biswa.ep;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.biswa.ep.entities.AbstractContainer;
/**
 * Named thread pool manages all the thread local before allocating more threads.
 * @author Biswa
 *
 */
public class NamedThreadFactory implements ThreadFactory {
	private static final AtomicInteger threadNumber = new AtomicInteger();
	private String name;
	private AbstractContainer container;
	private boolean daemon=true;
	/**
	 * Creates a daemon thread pool factory.
	 * @param name
	 */
	public NamedThreadFactory(String name) {
		this.name=name;
	}
	/**
	 * Allows to create a non daemon thread pool factory.
	 * 
	 * @param name String
	 * @param daemon boolean
	 */
	public NamedThreadFactory(String name,boolean daemon) {
		this.name=name;
		this.daemon=daemon;
	}
	
	/**Assigns a thread pool with knowledge of Container its going to work on.
	 * 
	 * @param name String
	 * @param container AbstractContainer
	 */
	public NamedThreadFactory(String name,AbstractContainer container) {
		this.name=name+"-"+container.getName();
		this.container=container;
	}
	
	@Override
	final public Thread newThread(final Runnable r) {
		Thread t = new Thread(r,name+"-"+threadNumber.incrementAndGet()){
			public void run(){
				if(container!=null){
					ContainerContext.initialize(container);
				}
				super.run();
			}
		};
		t.setDaemon(daemon);
		return t;
	}
}
