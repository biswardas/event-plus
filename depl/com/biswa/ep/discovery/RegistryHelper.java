package com.biswa.ep.discovery;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistryHelper {
	private static final String PP_REGISTRY_PORT = "pp.registryPort";
	private static final String PP_REGISTRY_HOST = "pp.registryHost";
	private static final String PP_REGISTRY_AUTO = "pp.auto.registry.disable";
	private static Registry registry;
	private static Binder binder;
	static{
		boolean auto = Boolean.getBoolean(PP_REGISTRY_AUTO);
		String registryHost=System.getProperty(PP_REGISTRY_HOST);
		int port=Integer.getInteger(PP_REGISTRY_PORT,Registry.REGISTRY_PORT);		
		try{
			init(registryHost,port);
		}catch(RemoteException e){
			if(!auto){
				try {
					RMIDiscoveryManager.main(new  String[0]);
					init(registryHost,port);
				} catch (Exception e1) {
					throw new RuntimeException("Registry not running? Could not launch in process registry "+registryHost+":"+port,e1);
				}
			}else{
				throw new RuntimeException("Is Registry running? Could not connect to registry "+registryHost+":"+port,e);				
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
        RMIListener comp;
        try{
        	comp = (RMIListener) registry.lookup(name);
        }catch(Exception e){
        	throw new RuntimeException("Could not obtain the remote handle",e);
        }
		return comp;
	}
	
	public static Connector getConnecter(String name){
		Connector connecter;
        try{
        	connecter = (Connector) registry.lookup(name);
        }catch(Exception e){
        	throw new RuntimeException("Could not obtain the remote handle:"+name,e);
        }
		return connecter;
	}
	
	public static EntryReader getEntryReader(String name){
		EntryReader entryReader;
        try{
        	entryReader = (EntryReader) registry.lookup(name);
        }catch(Exception e){
        	throw new RuntimeException("Could not obtain the remote handle:"+name,e);
        }
		return entryReader;
	}

	public static Registry getRegistry() {
		return registry;
	}
	
	public static Binder getBinder() {
		return binder;
	}	
}
