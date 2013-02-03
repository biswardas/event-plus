package com.biswa.ep.entities.identity;

import java.util.concurrent.atomic.AtomicInteger;
/**Reference implementation of the Identity generator service.
 * 
 * @author biswa
 *
 */
public class DefaultIdentityGenerator implements IdentityGenerator {
	/**
	 * Unique identity generator. The external id is used to identify the requested container entry 
	 * however the propagation happens on the generated identity. i.e. if we are listening two 
	 * containers and merging data in one container then the the external identity may conflict in the
	 * union container. 
	 */
	final private static AtomicInteger identitySequence = new AtomicInteger(10000);
 
	@Override
	public int generateIdentity() {		
		return identitySequence.incrementAndGet();
	}
}
