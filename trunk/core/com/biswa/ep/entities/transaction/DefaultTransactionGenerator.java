package com.biswa.ep.entities.transaction;

import java.util.concurrent.atomic.AtomicInteger;
/**Reference implementation of the transaction generator service.
 * 
 * @author biswa
 *
 */
public class DefaultTransactionGenerator implements TransactionGenerator {
	static final AtomicInteger trangen = new AtomicInteger(0); 
	@Override
	public int getNextTransactionID() {		
		return trangen.incrementAndGet();
	}
}
