package com.biswa.ep.subscription;

import java.util.HashMap;
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
	final private HashMap<Object, ContainerEntry> containerEntrySet = new HashMap<Object, ContainerEntry>();

	protected SimpleSubscriptionProcessor(String name) {
		super(name);
	}

	public abstract SynchronousQueue<HashMap<Object, Object>> getQueue();

	@Override
	public void init() {
		String producerName = getClass().getName();
		Thread t = new Thread(producerName) {
			public void run() {
				while (true) {
					HashMap<Object, Object> incomingObject;
					try {
						incomingObject = getQueue().take();
						begin();
						for (Entry<Object, Object> oneEntry : incomingObject
								.entrySet()) {
							update(containerEntrySet.get(oneEntry.getKey()),
									new ObjectSubstance(oneEntry.getValue()));
						}
						commit();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
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
		getQueue().add(hm);
		return containerEntry.getSubstance(this);
	};

	@Override
	public void unsubscribe(ContainerEntry containerEntry) {
		Object subject = super.getValue(containerEntry, getSubjectAttribute());
		containerEntrySet.remove(subject);
		unsubscribe(super.getValue(containerEntry, getSubjectAttribute()));
	}
}
