package com.biswa.ep.discovery;

import java.rmi.RemoteException;

public interface TransactionGenerator extends EPService {
	/**
	 * Name of the Binder it self bound in registry.
	 */
	public static final String TRANSACTION_GENERATOR=TransactionGenerator.class.getName();
	
	int getNextTransactionID() throws RemoteException;
}
