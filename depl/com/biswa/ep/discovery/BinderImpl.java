package com.biswa.ep.discovery;

import static com.biswa.ep.discovery.RMIDiscoveryManager.MBS;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import com.biswa.ep.NamedThreadFactory;
import com.biswa.ep.deployment.EPDeployer;
import com.biswa.ep.deployment.mbean.Discovery;

public class BinderImpl implements Binder,BinderImplMBean {
	private static final long DELAY = Long.getLong(DiscProperties.PP_REG_HC_INTERVAL, 60000);
	private ConcurrentHashMap<String,String> containerToInstanceMap = new ConcurrentHashMap<String,String>();
	private ConcurrentHashMap<String,EPDeployer> instanceMap = new ConcurrentHashMap<String,EPDeployer>();
	private CopyOnWriteArrayList<EPDeployer> slaveFreePool = new CopyOnWriteArrayList<EPDeployer>();
	private ScheduledThreadPoolExecutor stp = new ScheduledThreadPoolExecutor(1,new NamedThreadFactory("HealthCheckThread",true));
	private ConcurrentHashMap<String,Object> remoteObjects = new ConcurrentHashMap<String,Object>();
	public BinderImpl(){
		stp.scheduleAtFixedRate(new Runnable(){
			public void run(){
				checkHealthNow();
			}
		},DELAY,DELAY,TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void bind(String name, RMIListener obj) throws RemoteException {
		if(containerToInstanceMap.containsKey(name)){
			throw new RemoteException(name+" instance already exists...");
		}
		remoteObjects.put(name, obj);
		String memberName = obj.getDeployerName();
		if(memberName==null){
			memberName=name;
		}
		containerToInstanceMap.put(name, memberName);
		addToMBeanServer(name, obj);
		broadCastContainerDeployed(name);
	}

	@Override
	public Object lookup(String name) throws RemoteException {
		Object remoteObj = remoteObjects.get(name);
		if(remoteObj==null){
			throw new RemoteException(name + " object not bound");
		}
		return remoteObj;
	}

	private void broadCastContainerDeployed(String name) {
		for(EPDeployer oneDeployer:instanceMap.values()){
			try {
				oneDeployer.containerDeployed(name);
			} catch (RemoteException e) {
				//throw new RuntimeException(e);
			}
		}
	}
	
	private void addToMBeanServer(String name, RMIListener obj) {
		try {
			ObjectName bindName = new ObjectName("ContainerSchema:name="+name);
			MBS.registerMBean(new Discovery(obj), bindName);
			System.out.println("Registered:"+bindName);
		}catch(Exception e){
			System.err.println("Error while registering with JMX: "+name);
		}
	}

	@Override
	public void unbind(String acceptName) throws RemoteException {
		remoteObjects.remove(acceptName);
		containerToInstanceMap.remove(acceptName);
		removeFromMBeanServer(acceptName);
		broadCastContainerDestroyed(acceptName);
	}

	private void broadCastContainerDestroyed(String name) {
		for(EPDeployer oneDeployer:instanceMap.values()){
			try {
				oneDeployer.containerDestroyed(name);
			} catch (RemoteException e) {
				//throw new RuntimeException(e);
			}
		}
	}
	
	private void removeFromMBeanServer(String acceptName) {
		try {
			ObjectName bindName = new ObjectName("ContainerSchema:name="+acceptName);
			MBS.unregisterMBean(bindName);
			System.out.println("Unregistered:"+bindName);
		} catch (Exception e) {
			System.err.println("Error while unbinding from JMX: "+acceptName);
		}
	}

	@Override
	public void bindSlave(EPDeployer obj) throws RemoteException {
		slaveFreePool.add(obj);
		bindApp(obj);
	}


	@Override
	public void bindApp(EPDeployer obj) throws RemoteException {
		instanceMap.put(obj.getName(), obj);
	}
	
	@Override
	public EPDeployer getSlave() throws RemoteException {
		if(!slaveFreePool.isEmpty()){
			return slaveFreePool.remove(0);
		}
		return null;
	}
	@Override
	public void shutDownAllDeployers(boolean spareRegistry) {
		for(EPDeployer epDeployer:instanceMap.values()){
			try {
				epDeployer.shutDown();
			} catch (RemoteException e) {
				//throw new RuntimeException(e);
			}
		}
		if(!spareRegistry){
			System.exit(0);
		}
	}

	@Override
	public synchronized void checkHealth(boolean waitTillDone){
		if(waitTillDone){
			checkHealthNow();
		}else{
			stp.execute(new Runnable(){
				public void run(){
					checkHealthNow();
				}
			});	
		}
	} 
	private synchronized void checkHealthNow(){
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
	private void handlePeerDeath(String name, EPDeployer epDeployer) {
		System.err.println("One VM terminated(executing cleanup procedure):"+name);
		Iterator<Entry<String, String>> iter = containerToInstanceMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, String> containerDeployerEntry = iter.next();
			if(containerDeployerEntry.getValue().equals(name)){
				String containerName = containerDeployerEntry.getKey();
				try {
					unbind(containerName);
				} catch (Exception e) {
					//throw new RuntimeException(e);
				}
			}
		}
		instanceMap.remove(name);
		slaveFreePool.remove(epDeployer);
	}
}
