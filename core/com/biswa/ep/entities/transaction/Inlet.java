package com.biswa.ep.entities.transaction;

import java.util.Properties;

/**Interface used to accept data from outside world.
 * 
 * @author biswa
 *
 */
public interface Inlet {
	void setAgent(Agent agent,Properties props);
	void init();
	void terminate();
}
