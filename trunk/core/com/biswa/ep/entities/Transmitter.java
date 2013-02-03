package com.biswa.ep.entities;
/**Transmitter transmits the events to down stream containers.
 * 
 * @author biswa
 *
 */
public interface Transmitter {
	/**Submits the task to downstream containers.
	 * 
	 * @param r
	 */
	public void submit(Runnable r);
	/**
	 * Destroys the transmitter
	 */
	public void destroy();
}
