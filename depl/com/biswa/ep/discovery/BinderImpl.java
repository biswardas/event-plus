package com.biswa.ep.discovery;

import static com.biswa.ep.discovery.RMIDiscoveryManager.MBS;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.ObjectName;

import com.biswa.ep.deployment.EPDeployer;
import com.biswa.ep.deployment.mbean.Discovery;

public class BinderImpl implements Binder,BinderImplMBean {
	private static final long DELAY = 60000;
	private ConcurrentHashMap<String,String> containerDeployerNameMap = new ConcurrentHashMap<String,String>();
	private ConcurrentHashMap<String,EPDeployer> instanceMap = new ConcurrentHashMap<String,EPDeployer>();
	private CopyOnWriteArrayList<EPDeployer> slaveList = new CopyOnWriteArrayList<EPDeployer>();
	public BinderImpl(){
		Timer t = new Timer("HealthCheckThread",true);
		t.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				for(Entry<String, EPDeployer> oneEntry:instanceMap.entrySet()){
					String name = oneEntry.getKey();
					EPDeployer epDeployer = oneEntry.getValue();
					try {
						epDeployer.isAlive();
					} catch (RemoteException e) {
						handlePeerDeath(name, epDeployer);
					}
				}
			}
		},DELAY,DELAY);
	}
	@Override
	public void bind(String name, RMIListener obj) throws RemoteException {
		RegistryHelper.getRegistry().rebind(name, obj);
		containerDeployerNameMap.put(name, obj.getDeployerName());
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
			containerDeployerNameMap.remove(acceptName);
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
	public void bindSlave(EPDeployer obj) throws RemoteException {
		slaveList.add(obj);
		bindApp(obj);
	}


	@Override
	public void bindApp(EPDeployer obj) throws RemoteException {
		instanceMap.put(obj.getName(), obj);
	}
	
	@Override
	public EPDeployer getSlave() throws RemoteException {
		if(!slaveList.isEmpty()){
			return slaveList.remove(0);
		}
		return null;
	}
	protected void handlePeerDeath(String name, EPDeployer epDeployer) {
		System.err.println("One VM Died:"+name);
		ArrayList<String> containers = new ArrayList<String>();
		Iterator<Entry<String, String>> iter = containerDeployerNameMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> containerDeployerEntry = iter.next();
			if(containerDeployerEntry.getValue().equals(name)){
				containers.add(containerDeployerEntry.getKey());
				iter.remove();
			}
		}
		instanceMap.remove(name);
		slaveList.remove(epDeployer);
		for(EPDeployer oneDeployer:instanceMap.values()){
			try {
				oneDeployer.peerDied(name, containers);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	}
	@Override
	public void shutDownAllDeployers() {
		for(EPDeployer epDeployer:instanceMap.values()){
			try {
				epDeployer.shutDown();
			} catch (RemoteException e) {
				//throw new RuntimeException(e);
			}
		}
		for(String key:containerDeployerNameMap.keySet()){
			try {
				RegistryHelper.getRegistry().unbind(key);
			} catch (Exception e) {
				//throw new RuntimeException(e);
			}
		}
		containerDeployerNameMap.clear();
		instanceMap.clear();
		slaveList.clear();
	}
}
