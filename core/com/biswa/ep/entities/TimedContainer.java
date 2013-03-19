package com.biswa.ep.entities;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
/**Container dispatches accumulated changes in fixed interval. 
 * The interval can be tuned by setting TIMED_INTERVAL on the container. 
 * @author biswa
 *
 */

public class TimedContainer extends ThrottledContainer{
	public TimedContainer(String name,Properties props) {
		super(name,props);
		invokePeriodically(throttleTask, 0, getTimedInterval(), TimeUnit.MILLISECONDS);
	}
	
	/**Returns the timed interval for this container
	 * 
	 * @return int interval in milli seconds
	 */
	public int getTimedInterval(){
		String interval = getProperty(TIMED_INTERVAL);
		int interValDuration = 1000;
		if(interval!=null){
			interValDuration = Integer.parseInt(interval);
		}
		return interValDuration;
	}
}