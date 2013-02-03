package com.biswa.ep.entities.transaction;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
/**Concrete Transaction Generator hides the actual implementation of the 
 * transaction number generator by using ServiceLoader. ServiceLoader loads
 * all the available implementation of TransactionGenerator and uses the first
 * available one to generate Transaction.
 * 
 * @author biswa
 *
 */
public class TransactionGeneratorImpl implements TransactionGenerator {

	private ServiceLoader<TransactionGenerator> loader;
	
	public TransactionGeneratorImpl(){
		loader = ServiceLoader.load(TransactionGenerator.class);
	}
	
	@Override
	public int getNextTransactionID() {
        try {
            Iterator<TransactionGenerator> transactionGenerators = loader.iterator();
            while (transactionGenerators.hasNext()) {
            	TransactionGenerator d = transactionGenerators.next();

        		int tranID = d.getNextTransactionID();
        		assert log("Generating Transaction "+tranID);
                return tranID;
            }
        } catch (ServiceConfigurationError serviceError) {
            throw new RuntimeException(serviceError);
        }
        throw new RuntimeException("No Transaction Generator Sevice Located");
	}

	boolean log(String str) {
		System.out.println(Thread.currentThread().getName()+":"+str);
		return true;
	}
}
