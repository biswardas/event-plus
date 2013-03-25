package com.biswa.ep.deployment;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**Remote interface exposes the Deployer responsible to deploy containers
 * in remote VMs.
 * @author biswa
 *
 */
public interface EPDeployer extends Remote{	
	public String getName() throws RemoteException;
	public void deploy(String deploymentDescriptor) throws RemoteException;
	public boolean isAlive() throws RemoteException;
}
