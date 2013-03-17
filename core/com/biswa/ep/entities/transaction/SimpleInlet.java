package com.biswa.ep.entities.transaction;

import java.util.Properties;

public abstract class SimpleInlet implements Inlet {
	protected Agent agent = null;

	@Override
	public void setAgent(Agent agent, Properties props) {
		this.agent = agent;
	}
	
	protected abstract void failSafeInit() throws Exception;
	
	@Override
	public void init() {
		String producerName = getClass().getName();
		Thread procthread = new Thread(producerName){
			public void run() {
				try {
					failSafeInit();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		};
		procthread.start();
	}

	@Override
	public void terminate() {
		String producerName = getClass().getName();
		Thread procthread = new Thread(producerName){
			public void run() {
				try {
					failSafeTerminate();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		};
		procthread.start();
	}
	
	protected abstract void failSafeTerminate() throws Exception;
}
