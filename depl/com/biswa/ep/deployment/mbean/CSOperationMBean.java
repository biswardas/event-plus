package com.biswa.ep.deployment.mbean;
/**Mbean exposure access. Feature demonstration and implementation example of the containers.
 * 
 * @author biswa
 *
 */
public interface CSOperationMBean {
	/**Removes an attribute from the container.
	 * 
	 * @param name
	 */
	public void removeAttribute(String name);
	
	/**Updates the static member of the container & trigger any associated dependencies.
	 * 
	 * @param attributeName String
	 * @param value String
	 */
	public void updateStatic(String attributeName,String value);

	/**Updates the static member of the container & trigger any associated dependencies with a filter.
	 * 
	 * @param attributeName String
	 * @param value String
	 * @param filter String
	 */
	public void updateStaticWithFilter(String attributeName,String value,String filter);
	/**
	 * Dumps the contents of the current container.
	 */
	public void dumpContainer();
	/**
	 * Dumps the current structure of the container.
	 */
	public void dumpMetaInfo();
	/**
	 * Dumps the current latency to the standard io
	 */
	public void latency();
	
	/**
	 * Pause the container for specified duration
	 * @param duration
	 */
	public void pauseForSeconds(int duration);
	
	/**
	 * Interrupt paused thread
	 * @return String
	 */
	public String interruptPausedThread();
}
