package com.biswa.ep.entities.transaction;

/**Interface defined to handle relay transaction requests. And perform
 * due optimization required for the duration of the transaction.
 * @author biswa
 *
 */
public interface TransactionRelay {
	/**
	 * Method to begin transaction
	 * @param transactionId
	 */
	void beginTran();
	/**
	 * Method to commit transaction
	 */
	void commitTran();
	/**
	 * Method to rollback transaction
	 */
	void rollbackTran();
	
	/**
	 * Method tells the container that all
	 * down stream containers have processed 
	 * the transaction.
	 */
	void completionFeedback(int transactionId);
	
	/**
	 * Method adds a feedback agent to the container which can be used to notify the
	 * container interested in listening the feedback.
	 */
	void addFeedbackAgent(FeedbackAgent feedBackAgent);
}
