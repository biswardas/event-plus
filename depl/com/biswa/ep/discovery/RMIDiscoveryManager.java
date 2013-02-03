package com.biswa.ep.discovery;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.biswa.ep.deployment.mbean.Discovery;
/**
 * The class responsible to attach binder to the registry. This is required to
 * to allow processes running on other h/w to bind objects to this registry.
 * The must be started before deployers are started. 
 * Required system properties are 
 * pp.registryHost
 * pp.registryPort
 * @author biswa
 *
 */
public class RMIDiscoveryManager {
	public final static MBeanServer MBS = ManagementFactory.getPlatformMBeanServer();
	public static void main(String args[]) throws InterruptedException{
		Thread t = new Thread(new Runnable(){
			public void run(){
				String registryHost=null;
				int port=0;
				try{
					registryHost=System.getProperty("pp.registryHost");
					port=Integer.getInteger("pp.registryPort",Registry.REGISTRY_PORT);
					Registry registry = null;
					if(registryHost!=null)					
					registry = LocateRegistry.getRegistry(registryHost,port);
					else
					registry = LocateRegistry.createRegistry(port);
					
					BinderImpl binderimpl = new BinderImpl();
					Binder binder = (Binder) UnicastRemoteObject
							.exportObject(binderimpl, 0);
					registry.rebind(Binder.BINDER, binder);
					ObjectName bindName = new ObjectName("Services:name="+Binder.BINDER);
					MBS.registerMBean(new Discovery(binder), bindName);
					
					TransactionGeneratorImpl transgenImpl = new TransactionGeneratorImpl();
					TransactionGenerator transgen = (TransactionGenerator) UnicastRemoteObject
							.exportObject(transgenImpl, 0);
					registry.rebind(TransactionGenerator.TRANSACTION_GENERATOR, transgen);
					ObjectName transGenName = new ObjectName("Services:name="+TransactionGenerator.TRANSACTION_GENERATOR);
					MBS.registerMBean(new Discovery(transgen), transGenName);
					
				} catch(Exception e){
					e.printStackTrace();
					throw new RuntimeException("Error occured while starting registry"+registryHost+":"+port);
				}
			}
		},"pp.registryserver"
		);
		t.setDaemon(false);
		t.start();
		t.join();
	}
}
