package com.biswa.ep.subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.SynchronousQueue;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public abstract class SimpleSubscriptionProcessor extends
		SubscriptionContainerProcessor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Thread subscriptionThread = null;
	final protected SynchronousQueue<Map<Object, Object>> queue = new SynchronousQueue<Map<Object, Object>>();
	
	final private HashMap<Object, ContainerEntry> containerEntrySet = new HashMap<Object, ContainerEntry>();

	protected SimpleSubscriptionProcessor(String name) {
		super(name);
	}

	protected abstract void failSafeInit() throws Exception;
	
	@Override
	public void init() {
		String producerName = getClass().getName();
		subscriptionThread = new Thread(producerName) {
			public void run() {
				while (true) {
					Map<Object, Object> incomingObject;
					try {
						incomingObject = queue.take();
						begin();
						for (Entry<Object, Object> oneEntry : incomingObject
								.entrySet()) {
							ContainerEntry containerEntry = containerEntrySet.get(oneEntry.getKey());
							if(containerEntry!=null){
								//Possibly unsubscribed however external world yet to acknowledge
								update(containerEntry,new ObjectSubstance(oneEntry.getValue()));
							}
						}
						commit();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (Throwable th){
						th.printStackTrace();
						rollback();
					}
				}
			}
		};
		subscriptionThread.start();
		try {
			failSafeInit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public abstract Object subscribe(Object subject);

	public abstract void unsubscribe(Object subject);

	@Override
	public Substance subscribe(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		Object subject = super.getValue(containerEntry, getSubjectAttribute());
		containerEntrySet.put(subject, containerEntry);
		HashMap<Object, Object> hm = new HashMap<Object, Object>();
		hm.put(subject, subscribe(subject));
		queue.put(hm);
		return containerEntry.getSubstance(this);
	};

	@Override
	public void unsubscribe(ContainerEntry containerEntry) {
		Object subject = super.getValue(containerEntry, getSubjectAttribute());
		containerEntrySet.remove(subject);
		unsubscribe(super.getValue(containerEntry, getSubjectAttribute()));
	}
	
	@Override
	public void terminate() {
		String producerName = getClass().getName();
		destroyExternalWorld(producerName);
	}

	private void destroyExternalWorld(String producerName) {
		Thread procthread = new Thread(producerName){
			public void run() {
				try {
					failSafeTerminate();
					subscriptionThread.interrupt();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		};
		procthread.start();
	}
	
	protected abstract void failSafeTerminate() throws Exception;
}
