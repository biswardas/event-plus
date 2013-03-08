package com.biswa.ep.discovery;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import javax.rmi.PortableRemoteObject;
/**Reference implementation of the transaction generator service.
 * 
 * @author biswa
 *
 */
public class RMIIdentityGenerator implements IdentityGenerator,com.biswa.ep.entities.identity.IdentityGenerator {
	private static IdentityGenerator identityGenerator;
	public RMIIdentityGenerator(){
		try{
			Registry registry = RegistryHelper.getRegistry();
			Object obj = registry.lookup(IdentityGenerator.IDENTITY_GENERATOR);
			identityGenerator = (IdentityGenerator) PortableRemoteObject.narrow(obj, IdentityGenerator.class);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int generateIdentity() {
		int id = 0;
		try {
			id = identityGenerator.generateIdentity();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		return id; 
	}
}
