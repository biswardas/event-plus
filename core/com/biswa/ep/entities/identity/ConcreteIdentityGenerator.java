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

	private ServiceLoader<IdentityGenerator> loader;
	
	public ConcreteIdentityGenerator(){
		loader = ServiceLoader.load(IdentityGenerator.class);
	}
	
	@Override
	final public int generateIdentity() {
        try {
            Iterator<IdentityGenerator> IdentityGenerators = loader.iterator();
            while (IdentityGenerators.hasNext()) {
            	IdentityGenerator d = IdentityGenerators.next();
                return d.generateIdentity();
            }
        } catch (ServiceConfigurationError serviceError) {
            throw new RuntimeException(serviceError);
        }
        throw new RuntimeException("No Identity Generator Sevice Located");
	}
}
