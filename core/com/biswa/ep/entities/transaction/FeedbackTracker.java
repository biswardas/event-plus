package com.biswa.ep.entities.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.biswa.ep.ClientToken;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.FeedbackAwareContainer;
import com.biswa.ep.entities.PropertyConstants;
/**Feedback tracker which tracks the feedback from the originator.
 * 
 * @author biswa
 *
 */
public final class FeedbackTracker {
	private final int feedback_time_out;
	private final ClientToken CLIENT_TOKEN = new ClientToken();
	/**
	 * The Feedbackcontainer it is associated with.
	 */
	private final FeedbackAwareContainer feedbackContainer;
	/**
	 * Current circuit completion code, used to determine whether all 
	 * sources have reported transaction completion.
	 */
	private int circuitCompletionCode = CLIENT_TOKEN.getCurrentState();
	/**
	 * Numerical equivalent of the source.
	 */
	private final Map<String,Integer> sourceToNumber = new HashMap<String,Integer>();
	/**Class which maintains state of each feedback/source.
	 * 
	 * @author biswa
	 *
	 */
	private class Feedback{
		/**
		 * Known sources at the begin of transaction
		 */
		int expected;
		/**
		 * Constructor to create one feedback instance with the 
		 * expected sources at the time of feedback receipt.
		 * @param expected
		 */
		Feedback(int expected){
			this.expected=expected;
		}
		/**
		 * Is this transaction expected to be complete with the 
		 * arrival of this message.
		 * @param sourceNum int
		 * @return boolean
		 */
		boolean isCircuitComplete(int sourceNum){
			expected = expected|sourceNum;
			return expected==ClientToken.ALL_AVAILABLE;
		}
		/** Is this feedback aware of the source?
		 * 
		 * @param sourceNum int
		 * @return boolean
		 */
		boolean awareOf(int sourceNum){
			return (expected|sourceNum)>expected;
		}
	}
	/**
	 * Last throttled transaction on feedbackContainer
	 */
	private int lastTransactionProcessed = 0;
	/**
	 * Transaction to circuit completion code.
	 */
	private Map<Integer,Feedback> feedbackMap = new HashMap<Integer,Feedback>();
	
	/**Feedback Tracker constructor
	 * 
	 * @param tranAdapter TransactionAdapter
	 */
	public FeedbackTracker(FeedbackAwareContainer feedbackContainer) {
		this.feedbackContainer=feedbackContainer;
		this.feedback_time_out=getFeedbackTimeout();

		//Since flush is done only when a feedback cycle is complete or
		//any transaction is committed by upstream there is no easy way 
		//of propagating changes to downstream containers. This periodically
		//checks if there is anything dirty need to be propagated.
		feedbackContainer.agent().getEventCollector().scheduleWithFixedDelay(new ContainerTask(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -1284221022963982658L;

			@Override
			protected void runtask() throws Throwable {
				checkAndGo();
			}
			
		}, 0, feedbackContainer.getTimedInterval(), TimeUnit.MILLISECONDS);
	}
	
	/**Method to track feedback for each transaction.
	 * 
	 * @param transactionID int 
	 * @param source String
	 */
	public void trackFeedback(int transactionID,String source){
		Integer sourceNumber = sourceToNumber.get(source);
		assert sourceNumber!=null:"Do I know you?"+source;
		if(sourceToNumber.size()>1){
			Feedback feedbackStatus = feedbackMap.get(transactionID);
			if(feedbackStatus==null){
				feedbackStatus = new Feedback(circuitCompletionCode);
				feedbackMap.put(transactionID, feedbackStatus);
			} 
			if(feedbackStatus.isCircuitComplete(sourceNumber)){
				markFeedbackCycleComplete(transactionID);
			}
			
		}else{//Lone source of feedback lets go ahead
			markFeedbackCycleComplete(transactionID);
		}
	}
	
	/**Educate this tracker that one originator has notified that
	 * it will send feedback to this container.
	 * @param source String
	 */
	public void addFeedbackSource(String source){
		//Check if this container has reincarnated. If so we may have to clean all awaiting feedbacks.
		removeFeedbackSource(source);
		//Re addition of the source
		sourceToNumber.put(source, CLIENT_TOKEN.getToken());
		circuitCompletionCode = CLIENT_TOKEN.getCurrentState();
		markFeedbackCycleComplete(0);
	}
	
	/** The originator ceased to exist. Please treat in store feedback
	 * accordingly.
	 * @param source String
	 */
	public void removeFeedbackSource(String source){
		Integer primeIdentity = sourceToNumber.get(source);
		if(primeIdentity!=null){//It seems I knew you before
			sourceToNumber.remove(source);
			CLIENT_TOKEN.releaseToken(primeIdentity);
			circuitCompletionCode = CLIENT_TOKEN.getCurrentState();
			//For each feedback state in store
			for(Entry<Integer,Feedback> entry:feedbackMap.entrySet()){
				Feedback feedback = entry.getValue();
				if(feedback.awareOf(primeIdentity)){//Seems this feedback was expecting this source
					if(feedback.isCircuitComplete(primeIdentity)){//Treat this has arrived.
						markFeedbackCycleComplete(entry.getKey());
					}
				}				
			}
		}
	}
	public void trackTransaction(int transactionID){
		if(!sourceToNumber.isEmpty()){
			this.lastTransactionProcessed = transactionID;
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
							feedbackContainer.log("Transaction TimedOut "+transactionBeingTracked);
							feedbackContainer.log("Forcing feedback completion");
							markFeedbackCycleComplete(0);
						}
					}
				};
				feedbackContainer.agent().getEventCollector().schedule(new Runnable() {
					@Override
					public void run() {
						feedbackContainer.agent().invokeOperation(containerTask);
					}
	
				}, feedback_time_out, TimeUnit.SECONDS);	
			}
		}
	}
	/**
	 * If not awaiting any feedback  from source then dispatch next
	 * throttled transaction.
	 */
	public void checkAndGo(){
		if(lastTransactionProcessed==0){
			markFeedbackCycleComplete(0);
		}
	}
	/**
	 * Cleans the transaction and triggers next throttled transaction cycle.
	 * @param transactionID int
	 */
	private void markFeedbackCycleComplete(int transactionID) {
		lastTransactionProcessed=0;
		feedbackMap.remove(transactionID);
		feedbackContainer.completionFeedback();
	}
	/**Returns the timed interval for this container
	 * 
	 * @return int interval in milli seconds
	 */
	final private int getFeedbackTimeout(){
		String feedbackTimeoutStr = feedbackContainer.getProperty(PropertyConstants.FEEDBACK_TIME_OUT);
		int feedbackTimeout = 1000;
		if(feedbackTimeoutStr!=null){
			feedbackTimeout = Integer.parseInt(feedbackTimeoutStr);
		}
		return feedbackTimeout;
	}
}
