package com.biswa.ep.discovery;

import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import com.biswa.ep.deployment.Deployer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.LightWeightEntry;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.Spec;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.FeedbackEvent;
import com.biswa.ep.entities.transaction.TransactionEvent;
import com.biswa.ep.provider.CompiledAttributeProvider;
import com.biswa.ep.provider.ScriptEngineAttributeProvider;
import com.biswa.ep.subscription.SubscriptionEvent;

public class RMIListenerImpl implements RMIListener{
	WeakReference<Agent> weakReference;

	private Agent getAgent() {
		return weakReference.get();
	}

	public RMIListenerImpl(Agent cl) {
		weakReference = new WeakReference<Agent>(cl);
	}

	@Override
	public void attributeAdded(ContainerEvent ce) throws RemoteException {
		getAgent().attributeAdded(ce);
	}

	@Override
	public void attributeRemoved(ContainerEvent ce) throws RemoteException {
		getAgent().attributeRemoved(ce);
	}

	@Override
	public void entryAdded(ContainerEvent ce) throws RemoteException {
		getAgent().entryAdded(ce);
	}

	@Override
	public void entryRemoved(ContainerEvent ce) throws RemoteException {
		getAgent().entryRemoved(ce);
	}

	@Override
	public void entryUpdated(ContainerEvent ce) throws RemoteException {
		getAgent().entryUpdated(ce);
	}

	@Override
	public void beginTran(TransactionEvent te) throws RemoteException {
		getAgent().beginTran(te);
	}

	@Override
	public void commitTran(TransactionEvent te) throws RemoteException {
		getAgent().commitTran(te);
	}

	@Override
	public void rollbackTran(TransactionEvent te) throws RemoteException {
		getAgent().rollbackTran(te);
	}

	@Override
	public void connected(ConnectionEvent ce) throws RemoteException {
		getAgent().connected(ce);
	}

	@Override
	public void disconnected(ConnectionEvent ce) throws RemoteException {
		getAgent().disconnected(ce);
	}

	@Override
	public void connect(String source, String sink, FilterSpec filterSpec)
			throws RemoteException {
		RMIRemoteContainer.createInstance(source, sink, filterSpec, getAgent());
	}

	@Override
	public void disconnect(String source, String sink) throws RemoteException {
		getAgent().disconnect(new ConnectionEvent(source, sink));
	}

	@Override
	public void replay(String source, String sink, FilterSpec filterSpec)
			throws RemoteException {
		getAgent().replay(new ConnectionEvent(source, sink, filterSpec));
	}

	@Override
	public void addFeedbackSource(String consumer, String producer)
			throws RemoteException {
		getAgent().addFeedbackSource(new FeedbackEvent(producer));
	}

	@Override
	public void receiveFeedback(String consumer, String producer,
			int transactionID) throws RemoteException {
		getAgent().receiveFeedback(
				new FeedbackEvent(producer, transactionID));
	}

	@Override
	public void subscribe(SubscriptionEvent subscriptionEvent)
			throws RemoteException {
		getAgent().subscribe(subscriptionEvent);
	}

	@Override
	public void unsubscribe(SubscriptionEvent subscriptionEvent)
			throws RemoteException {
		getAgent().unsubscribe(subscriptionEvent);
	}

	@Override
	public void substitute(SubscriptionEvent subscriptionEvent)
			throws RemoteException {
		getAgent().substitute(subscriptionEvent);
	}

	@Override
	public void invokeOperation(ContainerTask task) {
		getAgent().invokeOperation(task);
	}

	@Override
	public void applySpec(Spec spec) throws RemoteException {
		getAgent().applySpec(spec);
	}
	
	@Override
	public TransportEntry getByID(final int id) throws RemoteException {
		final List<TransportEntry> holder = new ArrayList<TransportEntry>(1);
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		getAgent().invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					ContainerEntry conEntry = getContainer().getConcreteEntry(
							id);
					if (conEntry == null) {
						holder.add(null);
					} else {
						holder.add(conEntry.cloneConcrete());
					}
				} finally {
					s.release();
				}
			}
		});
		s.acquireUninterruptibly();
		return holder.get(0);
	}

	@Override
	public TransportEntry[] getByID(final int[] ids) throws RemoteException {
		final List<TransportEntry> holder = new ArrayList<TransportEntry>();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		getAgent().invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					for (int id : ids) {
						ContainerEntry conEntry = getContainer()
								.getConcreteEntry(id);
						if (conEntry == null) {
							holder.add(null);
						} else {
							holder.add(conEntry.cloneConcrete());
						}
					}
				} finally {
					s.release();
				}
			}
		});
		s.acquireUninterruptibly();
		return holder.toArray(new TransportEntry[0]);
	}

	@Override
	public TransportEntry[] getByFilter(final FilterSpec filterSpec)
			throws RemoteException {
		final List<TransportEntry> holder = new ArrayList<TransportEntry>();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		getAgent().invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					for (ContainerEntry containerEntry : getContainer()
							.getContainerEntries()) {
						if (filterSpec.filter(containerEntry)) {
							holder.add(containerEntry.cloneConcrete());
						}
					}
				} finally {
					s.release();
				}
			}
		});
		s.acquireUninterruptibly();
		return holder.toArray(new TransportEntry[0]);
	}

	@Override
	public int[] getIDs() throws RemoteException {
		final ArrayList<Integer> holder = new ArrayList<Integer>();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		getAgent().invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					for (ContainerEntry containerEntry : getContainer()
							.getContainerEntries()) {
						holder.add(containerEntry.getIdentitySequence());
					}
				} finally {
					s.release();
				}
			}
		});
		s.acquireUninterruptibly();
		Integer[] objectIds =  holder.toArray(new Integer[0]);
		int[] ids = new int[objectIds.length];
		for(int index=0;index<objectIds.length;index++){
			ids[index]=objectIds[index];
		}
		return ids;
	}


	@Override
	public int getEntryCount(final String name,final int isolation) throws RemoteException {
		return getAgent().getEntryCount(name,isolation);
	}

	@Override
	public LightWeightEntry getSortedEntry(final String name,final int id,final int isolation) throws RemoteException {
		return getAgent().getSortedEntry(name, id,isolation);
	}

	@Override
	public String[] getAttributes() throws RemoteException {
		return getAgent().getAttributes();
	}
	@Override
	public String getDeployerName() throws RemoteException {
		return Deployer.getName();
	}

	@Override
	public void addCompiledAttribute(String expression) throws RemoteException {
		com.biswa.ep.entities.Attribute schemaAttribute = new CompiledAttributeProvider().getAttribute(expression);
		ContainerEvent ce = new ContainerStructureEvent(getAgent().getName(),schemaAttribute);
		getAgent().attributeRemoved(ce);
		getAgent().attributeAdded(ce);		
	}

	@Override
	public void addScriptAttribute(String expression) throws RemoteException {
		com.biswa.ep.entities.Attribute schemaAttribute = new ScriptEngineAttributeProvider().getAttribute(expression);
		ContainerEvent ce = new ContainerStructureEvent(getAgent().getName(),schemaAttribute);
		getAgent().attributeRemoved(ce);
		getAgent().attributeAdded(ce);		
	}
}
