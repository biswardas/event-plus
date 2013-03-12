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
	private static TransactionGenerator tGen = null;
	static{
		ServiceLoader<TransactionGenerator> loader = ServiceLoader.load(TransactionGenerator.class);
        try {
            Iterator<TransactionGenerator> transactionGenerators = loader.iterator();
            while (transactionGenerators.hasNext()) {
            	tGen = transactionGenerators.next();
            	break;
            }
            if(tGen==null){
            	throw new RuntimeException("No Transaction Generator Sevice Located");
            }
        } catch (ServiceConfigurationError serviceError) {
            throw new RuntimeException(serviceError);
        }
	}
	
	@Override
	public int getNextTransactionID() {
		return tGen.getNextTransactionID();
	}
}
