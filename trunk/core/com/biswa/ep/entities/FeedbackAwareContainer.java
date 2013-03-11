package com.biswa.ep.entities;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.biswa.ep.entities.transaction.FeedbackEvent;
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
	private final int feedback_time_out;
	public FeedbackAwareContainer(String name,Properties props) {
		super(name,props);
		if(props.getProperty(FEEDBACK_TIME_OUT)!=null){
			feedback_time_out = Integer.parseInt(props.getProperty(FEEDBACK_TIME_OUT));
		}else{
			feedback_time_out = 0;
		}
	}
	
	@Override
	protected void check(){
		if(lastTransactionProcessed==0){
			completionFeedback(0);
		}
	}
	
	@Override
	public void completionFeedback(int transactionID) {
		if(transactionID==0 || lastTransactionProcessed==transactionID){//Some client joined the party flush all collected updates
			assert log("Receiving Feedback= "+transactionID+" at "+ System.currentTimeMillis());
			throttledDispatch();
		}
	}

	@Override
	protected void trackThrottledTransaction() {
		super.trackThrottledTransaction();
		if(feedback_time_out>0){
			final ContainerTask containerTask = new ContainerTask() {
				/**
				 * 
				 */
				private static final long serialVersionUID = -3941903416147756059L;
				int transactionBeingTracked = lastTransactionProcessed;
				@Override
				protected void runtask() {
					if(lastTransactionProcessed==transactionBeingTracked){
						System.err.println("Transaction TimedOut "+transactionBeingTracked);
						System.err.println("Forcing feedback completion");
						completionFeedback(0);
					}
				}
			};
			agent().getEventCollector().schedule(new Runnable() {
				@Override
				public void run() {
					agent().invokeOperation(containerTask);
				}

			}, feedback_time_out, TimeUnit.SECONDS);	
		}
	}
	@Override
	public void disconnect(ConnectionEvent connectionEvent) {
		super.disconnect(connectionEvent);
		agent().removeFeedbackSource(new FeedbackEvent(connectionEvent.getSink()));
	}
}
