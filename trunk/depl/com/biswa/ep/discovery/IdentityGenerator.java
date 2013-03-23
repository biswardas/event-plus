package com.biswa.ep.discovery;

import java.rmi.RemoteException;

public interface IdentityGenerator extends EPService {
	/**
	 * Name of the Binder it self bound in registry.
	 */
	public static final String IDENTITY_GENERATOR=IdentityGenerator.class.getName();
	
	int generateIdentity() throws RemoteException;
}
