package com.biswa.ep.discovery;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistryHelper {
	private static final String PP_REGISTRY_PORT = "pp.registryPort";
	private static final String PP_REGISTRY_HOST = "pp.registryHost";
	private static Registry registry;
	private static Binder binder;
	static{
		String registryHost=null;
		int port=0;
		try{
			registryHost=System.getProperty(PP_REGISTRY_HOST);
			port=Integer.getInteger(PP_REGISTRY_PORT,Registry.REGISTRY_PORT);
			if(registryHost==null){
				registry = LocateRegistry.getRegistry(registryHost,port);
			}else{
				registry = LocateRegistry.getRegistry(port);
			}
			binder = (Binder) registry.lookup(Binder.BINDER);
		}catch(RemoteException e){
			e.printStackTrace();
			throw new RuntimeException("Is Registry running? Could not connect to registry"+registryHost+":"+port);
		}catch(NotBoundException e){
			e.printStackTrace();
			throw new RuntimeException("Is Discovery running? Could not obtain binder from "+registryHost+":"+port);
		}
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
        	throw new RuntimeException("Could not obtain the remote handle",e);
        }
		return connecter;
	}
	
	public static EntryReader getEntryReader(String name){
		EntryReader entryReader;
        try{
        	entryReader = (EntryReader) registry.lookup(name);
        }catch(Exception e){
        	throw new RuntimeException("Could not obtain the remote handle",e);
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
