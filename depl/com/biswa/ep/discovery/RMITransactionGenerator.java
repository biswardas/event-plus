package com.biswa.ep.discovery;

import java.rmi.RemoteException;
/**Reference implementation of the transaction generator service.
 * 
 * @author biswa
 *
 */
public class RMITransactionGenerator implements TransactionGenerator,com.biswa.ep.entities.transaction.TransactionGenerator {
	private static TransactionGenerator transactiongenerator;
	public RMITransactionGenerator(){
		transactiongenerator = RegistryHelper.getTransactionGenerator();
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
