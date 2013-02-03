package com.biswa.ep.entities;



public interface StructureOperation{

	/**Method provides the operation when an attribute is added to the Listening container
	 * 
	 * @param ce ContainerEvent
	 */
	void attributeAdded(ContainerEvent ce);

	/**Method provides the operation when an attribute is removed to the Listening container
	 * 
	 * @param ce ContainerEvent
	 */
	void attributeRemoved(ContainerEvent ce);
}
