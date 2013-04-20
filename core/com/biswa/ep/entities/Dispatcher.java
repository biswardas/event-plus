package com.biswa.ep.entities;


/** Interface exposing all methods required to dispatch updates the
 * down stream container.
 * 
 * @author biswa
 *
 */
public interface Dispatcher {
	/**Method delegates the container entry addition event
	 * 
	 * @param containerEntry
	 */
	void dispatchEntryAdded(ContainerEntry containerEntry);

	/**Method delegates the entry removal event
	 * 
	 * @param containerEntry
	 */
	void dispatchEntryRemoved(ContainerEntry containerEntry);

	/**Method delegates the entry updated event
	 * 
	 * @param attribute Attribute
	 * @param substance Substance
	 * @param containerEntry ContainerEntry
	 */
	void dispatchEntryUpdated(Attribute attribute, Object substance, ContainerEntry containerEntry);
	
	/**
	 * Method which delegates the attribute addition to the dispatcher thread.
	 * @param requestedAttribute Attribute
	 */
	void dispatchAttributeAdded(Attribute requestedAttribute);
	
	/**Method which delegates the attribute removal event
	 * 
	 * @param requestedAttribute Attribute
	 */
	void dispatchAttributeRemoved(Attribute requestedAttribute);
}
