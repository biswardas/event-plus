package com.biswa.ep.discovery;

import java.rmi.RemoteException;
/**Reference implementation of the transaction generator service.
 * 
 * @author biswa
 *
 */
public class RMIIdentityGenerator implements IdentityGenerator,com.biswa.ep.entities.identity.IdentityGenerator {
	private static IdentityGenerator identityGenerator;
	public RMIIdentityGenerator(){
		identityGenerator = RegistryHelper.getIdentityGenerator();
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
