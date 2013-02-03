package com.biswa.ep.discovery;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TransactionGenerator extends Remote {
	/**
	 * Name of the Binder it self bound in registry.
	 */
	public static final String TRANSACTION_GENERATOR=TransactionGenerator.class.getName();
	
	int getNextTransactionID() throws RemoteException;
}
