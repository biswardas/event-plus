package com.biswa.ep.discovery;

import static com.biswa.ep.discovery.RMIDiscoveryManager.MBS;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.ObjectName;

import com.biswa.ep.deployment.mbean.Discovery;

public class BinderImpl implements Binder {
	private CopyOnWriteArrayList<Remote> slaveList = new CopyOnWriteArrayList<Remote>();
	@Override
	public void bind(String name, Remote obj) throws RemoteException {
		RegistryHelper.getRegistry().rebind(name, obj);
		try {
			ObjectName bindName = new ObjectName("ContainerSchema:name="+name);
			if(MBS.isRegistered(bindName)){
				System.out.println("Unregistering"+bindName);
				MBS.unregisterMBean(bindName);
			}
			System.out.println("Registering:"+bindName);
			MBS.registerMBean(new Discovery(obj), bindName);
		}catch(Exception e){
			System.err.println("Error while registering with JMX: "+name);
		}
	}

	@Override
	public void unbind(String acceptName) throws RemoteException {
		System.out.println("Unregistering"+acceptName);
		try {
			RegistryHelper.getRegistry().unbind(acceptName);
		} catch (NotBoundException e) {
			System.err.println("Error while unbinding from registry: "+acceptName);
		}
		try {
			ObjectName bindName = new ObjectName("ContainerSchema:name="+acceptName);
			MBS.unregisterMBean(bindName);
		} catch (Exception e) {
			System.err.println("Error while unbinding from JMX: "+acceptName);
		}
	}

	@Override
	public void bindSlave(Remote obj) throws RemoteException {
		slaveList.add(obj);
	}
	
	@Override
	public Remote getSlave() throws RemoteException {
		if(!slaveList.isEmpty()){
			return slaveList.remove(0);
		}
		return null;
	}
}
