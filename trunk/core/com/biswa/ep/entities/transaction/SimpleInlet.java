package com.biswa.ep.entities.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.SynchronousQueue;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerInsertEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public abstract class SimpleInlet implements Inlet {
	protected SynchronousQueue<Map<Object, Object>> queue = new SynchronousQueue<Map<Object, Object>>();
	private Map<Object,Attribute> cachedAttr = new HashMap<Object,Attribute>(){
		private static final long serialVersionUID = -8944792144074973287L;

		@Override
		public Attribute get(Object key){
			Attribute attribute = super.get(key);
			if(attribute==null){
				put(key,(attribute=new LeafAttribute(key.toString())));
			}
			return attribute;
		}
	};

	private Agent agent = null;

	@Override
	public void setAgent(Agent agent, Properties props) {
		this.agent = agent;
	}
	
	protected abstract void failSafeInit() throws Exception;
	
	@Override
	public void init() {
		String producerName = getClass().getName();
		initExternalWorld(producerName);
		Thread t = new Thread(producerName) {
			public void run() {
				while (true) {
					Map<Object, Object> incomingObject = null;
					try {
						incomingObject = queue.take();
						int tranID = agent.getNextTransactionID();
						TransactionEvent te = new TransactionEvent(agent.cl.getName(),agent.cl.getName(),
								tranID);
						agent.beginTran(te);
						HashMap<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
						for (Entry<Object, Object> oneEntry : incomingObject
								.entrySet()) {
							Substance substance =  new ObjectSubstance(oneEntry.getValue());
							hm.put(cachedAttr.get(oneEntry.getKey()),substance);
						}
						agent.entryAdded(new ContainerInsertEvent(agent.cl.getName(),
								new TransportEntry(agent.cl.generateIdentity(),
										hm), tranID));
						agent.commitTran(te);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
		t.start();
	}

	private void initExternalWorld(String producerName) {
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
	}
}
