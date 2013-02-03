package com.biswa.ep.entities.transaction;
/**Interface which defines the agent to broadcast transaction completion
 * to the listening containers. Any remote feedback mechanism has to be
 * provided by the remoting provider.
 * 
 * @author biswa
 *
 */
public interface FeedbackAgent {
	/**
	 * Method tells the container that all
	 * down stream containers have processed 
	 * the transaction.
	 */
	void completionFeedback(int transactionId);
}
