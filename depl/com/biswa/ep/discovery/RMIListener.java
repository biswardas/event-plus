package com.biswa.ep.discovery;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.spec.Spec;
import com.biswa.ep.entities.transaction.TransactionEvent;
/**Sink side interface receives all the remote task and delegates the operation to
 * the sink container. This method is invoked by the remote proxy Container.
 * This object lives in the Sink context.
 * @author biswa
 *
 */
public interface RMIListener extends Connector,EntryReader,Remote {	
	void attributeAdded(ContainerEvent containerEvent) throws RemoteException;
	void attributeRemoved(ContainerEvent containerEvent) throws RemoteException;
	void entryAdded(ContainerEvent containerEvent) throws RemoteException;
	void entryRemoved(ContainerEvent containerEvent) throws RemoteException;
	void entryUpdated(ContainerEvent containerEvent) throws RemoteException;
	
	void beginTran(TransactionEvent te) throws RemoteException;
	void commitTran(TransactionEvent te) throws RemoteException;
	void rollbackTran(TransactionEvent te) throws RemoteException;
	
	void connected(ConnectionEvent ce) throws RemoteException;
	void disconnected(ConnectionEvent ce) throws RemoteException;
	void invokeOperation(ContainerTask task) throws RemoteException;
	void applySpec(Spec spec) throws RemoteException;
	void addCompiledAttribute(String expression) throws RemoteException;
	void addScriptAttribute(String expression) throws RemoteException;
	String getDeployerName() throws RemoteException;
}
