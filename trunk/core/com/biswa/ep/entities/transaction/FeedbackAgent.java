package com.biswa.ep.entities.transaction;
/**Interface which defines the agent to broadcast transaction completion
 * to the listening containers. Any remote feedback mechanism has to be
 * provided by the remoting provider.
 * 
 * @author biswa
 *
 */
public abstract class FeedbackAgent {
	/**
	 * Method tells the container that all
	 * down stream containers have processed 
	 * the transaction.
	 */
	public abstract void completionFeedback(int transactionId);
	/**
	 * Returns the consumer of Feedback.
	 * @return String
	 */
	public abstract String getFeedBackConsumer();
	@Override
	public int hashCode() {
		return getFeedBackConsumer().hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj!=null){
			return getFeedBackConsumer().equals(((FeedbackAgent)obj).getFeedBackConsumer());
		}else{
			return false;
		}
	}
}
