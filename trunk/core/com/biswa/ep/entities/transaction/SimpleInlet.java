package com.biswa.ep.entities.transaction;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.SynchronousQueue;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerInsertEvent;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public abstract class SimpleInlet implements Inlet {
	protected SynchronousQueue<HashMap<Object, Object>> queue = new SynchronousQueue<HashMap<Object, Object>>();
	private final String SOURCE_NAME = "ANONYMOUS";

	private Agent agent = null;

	@Override
	public void setAgent(Agent agent, Properties props) {
		this.agent = agent;
	}
	
	protected abstract void failSafeInit() throws Exception;
	
	@Override
	public void init() {
		String producerName = getClass().getName();
		Thread t = new Thread(producerName) {
			public void run() {
				while (true) {
					HashMap<Object, Object> incomingObject = null;
					try {
						incomingObject = queue.take();
						int tranID = agent.getNextTransactionID();
						TransactionEvent te = new TransactionEvent(SOURCE_NAME,
								tranID);
						agent.beginTran(te);
						HashMap<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
						for (Entry<Object, Object> oneEntry : incomingObject
								.entrySet()) {
							hm.put(agent.cl.getAttributeByName(oneEntry
									.getKey().toString()), new ObjectSubstance(
									oneEntry.getValue()));
						}
						agent.entryAdded(new ContainerInsertEvent(SOURCE_NAME,
								new TransportEntry(agent.cl.generateIdentity(),
										hm), tranID));
						agent.commitTran(te);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		Thread procthread = new Thread(producerName){
			public void run() {
				try {
					failSafeInit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		procthread.start();
	}

	@Override
	public void terminate() {
	}
}
