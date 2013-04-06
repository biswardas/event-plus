package com.biswa.ep.entities;

import java.util.Properties;

import com.biswa.ep.entities.transaction.FeedbackEvent;
import com.biswa.ep.entities.transaction.FeedbackTracker;
/**Container which waits for the client to report completion of a 
 * particular transaction, before sending next batch of updates. 
 * The container triggers the updates in following scenarios.<br>
 * 1. No transaction currently waiting.<br>
 * 2. Last client reported completion of transaction.<br>
 * 3. New feedback listener client joined the party.<br>
 * 
 * @author biswa
 *
 */

public class FeedbackAwareContainer extends ThrottledContainer {
	private final FeedbackTracker feedbackTracker;
	public FeedbackAwareContainer(String name,Properties props) {
		super(name,props);
		int feedback_time_out = 0;
		if(props.getProperty(FEEDBACK_TIME_OUT)!=null){
			feedback_time_out = Integer.parseInt(props.getProperty(FEEDBACK_TIME_OUT));
		}
		feedbackTracker = new FeedbackTracker(this,feedback_time_out,getTimedInterval());
		agent().setFeedbackTracker(feedbackTracker);
	}
	 
	public void completionFeedback() {
		if(!throttleTask.isActivated()){
			agent().invokeOperation(throttleTask);
			throttleTask.activate();
		}
	}

	@Override
	final public void beginTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.beginTran();
			feedbackTracker.trackTransaction(super.getCurrentTransactionID());
		}
	}
	
	@Override
	final public void commitTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.commitTran();
		}else{
			dispatchFeedback();
			feedbackTracker.checkAndGo();
		}
	}

	@Override
	final public void rollbackTran() {
		if(throttleTask.isExecuting()){
			//Only continue the transaction if it is a throttled dispatch.
			super.rollbackTran();
		}else{
			dispatchFeedback();
			feedbackTracker.checkAndGo();
		}
	}
	@Override
	public void disconnect(ConnectionEvent connectionEvent) {
		super.disconnect(connectionEvent);
		agent().removeFeedbackSource(new FeedbackEvent(connectionEvent.getSink()));
	}
}
