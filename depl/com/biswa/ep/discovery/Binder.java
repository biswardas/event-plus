package com.biswa.ep.discovery;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**Remote interface exposes the Binder responsible to bind all remote
 * handles in the registry. All participating process must use the binder
 * to bind their remote handles.
 * 
 * @author biswa
 *
 */
public interface Binder extends Remote {
	/**
	 * Name of the Binder it self bound in registry.
	 */
	public static final String BINDER=Binder.class.getName();
	
	/**Method binds the remote object to the registry.
	 * 
	 * @param name String Name of the remote object used to bind object to registry.
	 * @param obj Remote Object performing remote function.
	 * @throws RemoteException
	 */
	void bind(String name, Remote obj) throws RemoteException;
	
	/**This method unbinds an name from the registry.
	 * 
	 * @param acceptName String
	 */
	void unbind(String acceptName) throws RemoteException;
}
