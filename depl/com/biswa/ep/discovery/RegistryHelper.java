package com.biswa.ep.discovery;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.rmi.PortableRemoteObject;

public class RegistryHelper implements DiscProperties{
	private static Registry registry;
	private static Binder binder;
	static{
		boolean auto = Boolean.getBoolean(PP_DIS_AUTO_REG);
		String registryHost=null;
		try {
			registryHost=System.getProperty(PP_REGISTRY_HOST,java.net.InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		int port=Integer.getInteger(PP_REGISTRY_PORT,Registry.REGISTRY_PORT);		
		try{
			init(registryHost,port);
		}catch(RemoteException e){
			if(!auto){
				try {
					RMIDiscoveryManager.main(new  String[0]);
					init(registryHost,port);
				} catch (Exception e1) {
					throw new RuntimeException("Discovery not running, Could not launch in process discovery "+registryHost+":"+port,e1);
				}
			}else{
				throw new RuntimeException("Discovery not running at "+registryHost+":"+port+",(Launching embedded discovery is disabled. Note Slave process can not start discovery manager).",e);				
			}
		}catch(NotBoundException e){
			throw new RuntimeException("Is Discovery running? Could not obtain binder from "+registryHost+":"+port,e);
		}
	}
	
	private static void init(String registryHost,int port) throws RemoteException, NotBoundException{
		if(registryHost==null){
			registry = LocateRegistry.getRegistry(registryHost,port);
		}else{
			registry = LocateRegistry.getRegistry(port);
		}
		binder = (Binder) registry.lookup(Binder.BINDER);
	}
	
	public static RMIListener getRMIListener(String name){
        try{
        	Object obj = registry.lookup(name);
        	return (RMIListener)  PortableRemoteObject.narrow(obj, RMIListener.class);
        }catch(Exception e){
        	throw new RuntimeException("Could not obtain the remote handle",e);
        }
	}
	
	public static Connector getConnecter(String name){
        try{
        	Object obj = registry.lookup(name);
        	return (Connector) PortableRemoteObject.narrow(obj, Connector.class);
        }catch(Exception e){
        	throw new RuntimeException("Could not obtain the remote handle:"+name,e);
        }
	}
	
	public static EntryReader getEntryReader(String name){
        try{
        	Object obj = registry.lookup(name);
        	return (EntryReader) PortableRemoteObject.narrow(obj, EntryReader.class);
        }catch(Exception e){
        	throw new RuntimeException("Could not obtain the remote handle:"+name,e);
        }
	}
	public static IdentityGenerator getIdentityGenerator(){
		try{
			Object obj = registry.lookup(IdentityGenerator.IDENTITY_GENERATOR);
			return (IdentityGenerator) PortableRemoteObject.narrow(obj, IdentityGenerator.class);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public static TransactionGenerator getTransactionGenerator() {
		try{
			Object obj = registry.lookup(TransactionGenerator.TRANSACTION_GENERATOR);
			return  (TransactionGenerator) PortableRemoteObject.narrow(obj, TransactionGenerator.class);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static Binder getBinder() {
		return binder;
	}
}
