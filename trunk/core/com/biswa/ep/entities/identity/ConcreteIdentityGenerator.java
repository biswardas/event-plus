package com.biswa.ep.entities.identity;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
/**Concrete Identity Generator hides the actual implementation of the 
 * Identity number generator by using ServiceLoader. ServiceLoader loads
 * all the available implementation of IdentityGenerator and uses the first
 * available one to generate Identity.
 * 
 * @author biswa
 *
 */
public class ConcreteIdentityGenerator implements IdentityGenerator {
	private static IdentityGenerator idGen = null;
	static{
		ServiceLoader<IdentityGenerator> loader = ServiceLoader.load(IdentityGenerator.class);
        try {
            Iterator<IdentityGenerator> IdentityGenerators = loader.iterator();
            while (IdentityGenerators.hasNext()) {
            	idGen = IdentityGenerators.next();
            	break;
            }
            if(idGen==null){
                throw new RuntimeException("No Identity Generator Sevice Located");
            }
        } catch (ServiceConfigurationError serviceError) {
            throw new RuntimeException(serviceError);
        }
	}
	
	@Override
	final public int generateIdentity() {
		return idGen.generateIdentity();
	}
}
