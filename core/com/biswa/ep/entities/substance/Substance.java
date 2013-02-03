package com.biswa.ep.entities.substance;

import java.io.Serializable;

/**The atomic interface which carries a piece of information.
 * This is a type safe wrapper carrier of value.
 * @author biswa
 *
 */
public interface Substance extends Serializable,Comparable<Substance>{	
	/** The value which is carried in the enclosing substance.
	 * 
	 * @return Object
	 */
	public Object getValue();
	
	/**Returns if this is a multi value substance
	 * 
	 * @return boolean
	 */
	public boolean isMultiValue();
	
	/**Returns if this is an aggregate value
	 * 
	 * @return aggregate
	 */
	public boolean isAggr();
}
