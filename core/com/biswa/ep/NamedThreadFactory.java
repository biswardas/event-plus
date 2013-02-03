package com.biswa.ep;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.biswa.ep.entities.AbstractContainer;

public class NamedThreadFactory implements ThreadFactory {
	private static final AtomicInteger threadNumber = new AtomicInteger();
	private String name;
	private AbstractContainer container;
	private boolean daemon=true;
	public NamedThreadFactory(String name) {
		this.name=name;
	}
	public NamedThreadFactory(String name,boolean daemon) {
		this.name=name;
		this.daemon=daemon;
	}
	public NamedThreadFactory(String name,AbstractContainer container) {
		this.name=name+"-"+container.getName();
		this.container=container;
	}
	@Override
	public Thread newThread(final Runnable r) {
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
