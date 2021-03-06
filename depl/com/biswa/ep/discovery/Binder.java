package com.biswa.ep.discovery;

import java.rmi.RemoteException;

import com.biswa.ep.deployment.EPDeployer;
/**Remote interface exposes the Binder responsible to bind all remote
 * handles in the registry. All participating process must use the binder
 * to bind their remote handles.
 * 
 * @author biswa
 *
 */
public interface Binder extends EPService {
	/**
	 * Name of the Binder it self bound in registry.
	 */
	public static final String BINDER=Binder.class.getName();
	
	/**Method binds the remote object to the registry.
	 * 
	 * @param name String Name of the remote object used to bind object to registry.
	 * @param obj RMIListener Object performing remote function.
	 * @throws RemoteException
	 */
	void bind(String name, RMIListener obj) throws RemoteException;
	
	/** Method returns the remote stub bound by above.
	 * 
	 * @param name
	 * @return Remote Object
	 * @throws RemoteException
	 */
	Object lookup(String name) throws RemoteException;
	
	/**This method unbinds an name from the registry.
	 * 
	 * @param acceptName String
	 */
	void unbind(String acceptName) throws RemoteException;	
	
	/**This method binds a slave in registry.
	 * 
	 * @param obj Remote
	 */
	void bindSlave(EPDeployer obj) throws RemoteException;	
	
	/**This method binds application in registry.
	 * 
	 * @param obj EPDeployer
	 */
	void bindApp(EPDeployer obj) throws RemoteException;
	
	
	/**
	 * This method binds a slave in registry.
	 */
	EPDeployer getSlave() throws RemoteException;

	void checkHealth(boolean waitTillDone) throws RemoteException;

}
