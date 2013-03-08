package com.biswa.ep.discovery;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import javax.rmi.PortableRemoteObject;
/**Reference implementation of the transaction generator service.
 * 
 * @author biswa
 *
 */
public class RMITransactionGenerator implements TransactionGenerator,com.biswa.ep.entities.transaction.TransactionGenerator {
	private static TransactionGenerator transactiongenerator;
	public RMITransactionGenerator(){
		try{
			Registry registry = RegistryHelper.getRegistry();
			Object obj = registry.lookup(TransactionGenerator.TRANSACTION_GENERATOR);
			transactiongenerator = (TransactionGenerator) PortableRemoteObject.narrow(obj, TransactionGenerator.class);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int getNextTransactionID() {
		int transactionID = 0;
		try {
			transactionID = transactiongenerator.getNextTransactionID();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		return transactionID; 
	}
}
