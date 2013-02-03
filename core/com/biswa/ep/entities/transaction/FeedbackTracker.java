package com.biswa.ep.entities.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.biswa.ep.ClientToken;
/**Feedback tracker which tracks the feedback from the originator.
 * 
 * @author biswa
 *
 */
public final class FeedbackTracker {
	private final ClientToken CLIENT_TOKEN = new ClientToken();
	/**
	 * The transaction adapter it is associated with.
	 */
	private TransactionAdapter tranAdapter;
	/**
	 * Current circuit completion code, used to determine whether all 
	 * sources have reported transaction completion.
	 */
	private int circuitCompletionCode = CLIENT_TOKEN.getCurrentState();
	/**
	 * Numerical equivalent of the source.
	 */
	private Map<String,Integer> sourceToNumber = new HashMap<String,Integer>();
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
			assert expected%sourceNum==0:"Sorry I did not know you when transaction begun.";
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
	 * Transaction to circuit completion code.
	 */
	private Map<Integer,Feedback> feedbackMap = new HashMap<Integer,Feedback>();
	
	/**Feedback Tracker constructor
	 * 
	 * @param tranAdapter TransactionAdapter
	 */
	public FeedbackTracker(TransactionAdapter tranAdapter) {
		this.tranAdapter=tranAdapter;
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
				feedbackMap.remove(transactionID);
				tranAdapter.completionFeedback(transactionID);
			}
			
		}else{//Lone source of feedback lets go ahead
			tranAdapter.completionFeedback(transactionID);
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
		tranAdapter.completionFeedback(0);
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
						tranAdapter.completionFeedback(entry.getKey());
					}
				}				
			}
		}
	}
}
