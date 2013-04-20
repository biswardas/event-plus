package com.biswa.ep.entities;

import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.Spec;
/**Operations defined in this interface which operates on the data in present container. 
 * 
 * @author biswa
 *
 */

public interface DataOperation{
	/**Method provides the operation when an entry is added to the Listening container
	 * 
	 * @param ce ContainerEvent
	 */
	void entryAdded(ContainerEvent ce);

	/**Method provides the operation when an entry is removed to the Listening container
	 * 
	 * @param ce ContainerEvent
	 */
	void entryRemoved(ContainerEvent ce);

	/**Method provides the operation when an entry is updated to the Listening container
	 * 
	 * @param ce ContainerEvent
	 */
	void entryUpdated(ContainerEvent ce);	
	
	/**Method which applies a spec to current container. Sample spec like
	 * Pivot Spec, Aggregation Spec.
	 * 
	 * @param spec
	 */

	void applySpec(Spec spec);
	
	/**Method which allows to execute any arbitrary operation to be performed 
	 * in the current containers thread context. This method should be executed
	 * with proper care.
	 * 
	 * @param task ContainerTask
	 */
	void invokeOperation(ContainerTask task);
	
	/**
	 *Method to delete all entries in the underlying container. 
	 */
	void clear();
	
	/**Updates static and triggers the dependency
	 * 
	 * @param attribute Attribute
	 * @param substance Substance
	 * @param appliedFilter FilterSpec
	 */
	void updateStatic(Attribute attribute,Object substance,FilterSpec appliedFilter);
}
