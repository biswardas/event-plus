package com.biswa.ep.discovery;

import java.rmi.RemoteException;
import java.util.Properties;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.Spec;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.TransactionEvent;
/**
 * A proxy container is constructed when the sink container requests a connection
 * via RMI. This class acts a proxy client and transports the updates via the RMIListener.
 * @author biswa
 *
 */
public class RMIRemoteContainer extends AbstractContainer {
	private RMIListener rmiListener = null;
	private String source;
	private String sink;
	private Agent sourceAgent;
	
	public static RMIRemoteContainer createInstance(String source,String sink,FilterSpec filterSpec,Agent sourceAgent) {
		return new RMIRemoteContainer(source,sink,filterSpec,sourceAgent);
	}
	
	private RMIRemoteContainer(String source,String sink,FilterSpec filterSpec,Agent sourceAgent) {
		super(sink + "-Stub",new Properties());
		this.source = source;
		this.sink = sink;
		this.sourceAgent=sourceAgent;
		//Lookup the listener and attach to the proxy.
		rmiListener = RegistryHelper.getRMIListener(sink);
			agent()
			.addSource(
					new ConnectionEvent(source,sink));
			sourceAgent
					.connect(
							new ConnectionEvent(source,sink, this
									.agent(),filterSpec));
	}

	@Override
	public void attributeAdded(final ContainerEvent ce) {
		try {
			rmiListener.attributeAdded(ce);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}

	@Override
	public void attributeRemoved(ContainerEvent ce) {
		try {
			rmiListener.attributeRemoved(ce);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}

	@Override
	public void entryAdded(ContainerEvent ce) {
		try {
			rmiListener.entryAdded(ce);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}

	@Override
	public void entryRemoved(ContainerEvent ce) {
		try {
			rmiListener.entryRemoved(ce);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}

	@Override
	public void entryUpdated(ContainerEvent ce) {
		try {
			rmiListener.entryUpdated(ce);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}

	@Override
	public void connected(ConnectionEvent ce) {
		super.connected(ce);
		try {
			rmiListener
					.connected(ce);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}

	@Override
	public void beginTran(){
		try {
			final TransactionEvent te = new TransactionEvent(this.source,getCurrentTransactionOrigin(),getCurrentTransactionID());
			rmiListener.beginTran(te);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}
	
	@Override
	public void commitTran(){
		try {
			final TransactionEvent te = new TransactionEvent(this.source,getCurrentTransactionOrigin(),getCurrentTransactionID());
			rmiListener.commitTran(te);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}
	
	@Override
	public void rollbackTran(){
		try {
			final TransactionEvent te = new TransactionEvent(this.source,getCurrentTransactionOrigin(),getCurrentTransactionID());
			rmiListener.rollbackTran(te);
		} catch (RemoteException e) {
			cleanup(e);
		}
	}
	@Override
	public void connect(ConnectionEvent ce) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect(ConnectionEvent ce) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnected(ConnectionEvent containerEvent) {
		try {
			rmiListener
					.disconnected(containerEvent);
		} catch (RemoteException e) {
			cleanup(e);
		}finally{
			agent().destroy();
		}

	}
	
	private void cleanup(RemoteException e) {
		agent().destroy();
		System.err.println(Thread.currentThread().getName()+" Client disappeared: Error while dispatching:"+e);
		//Since it is a violent disconnection notify the source containers to disconnect
		ConnectionEvent conEvent = new ConnectionEvent(this.source,this.sink,
				agent());
		sourceAgent.disconnect(conEvent);
		//TODO will it be an over kill?
		RegistryHelper.checkHealth();
	}

	@Override
	public void applySpec(Spec spec) {
		throw new UnsupportedOperationException();
	}
	@Override
	protected Attribute[] getSubscribedAttributes() {
		throw new UnsupportedOperationException();
	};
	@Override
	protected Attribute[] getStatelessAttributes() {
		throw new UnsupportedOperationException();
	};
	@Override
	protected Attribute[] getStaticAttributes() {
		throw new UnsupportedOperationException();
	};
	@Override
	protected String[] getAllAttributeNames() {
		throw new UnsupportedOperationException();
	};
	
	public Attribute getAttributeByName(String attributeName) {
		throw new UnsupportedOperationException();
	};

	public ContainerEntry[] getContainerEntries() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub		
	}

	@Override
	public ContainerEntry getConcreteEntry(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateStatic(Attribute attribute, Substance substance,
			FilterSpec appliedFilter) {
		throw new UnsupportedOperationException();		
	};
}
