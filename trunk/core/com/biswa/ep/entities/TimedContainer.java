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

	@Override
	final public void beginTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.beginTran();
		}
	}
	
	@Override
	final public void commitTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.commitTran();
		}else{
			dispatchFeedback();
		}
	}

	@Override
	final public void rollbackTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.rollbackTran();
		}else{
			dispatchFeedback();
		}
	}
}