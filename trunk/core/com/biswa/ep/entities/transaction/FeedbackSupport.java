package com.biswa.ep.entities.transaction;

/**Interface defined to feedback support for the containers who want to get
 * feedback to initiate next cycle. 
 * @author biswa
 *
 */
public interface FeedbackSupport {
	/**
	 * Method sets expectation on sources to expect feedback
	 * @param transactionEvent TransactionEvent
	 */
	void addFeedbackSource(TransactionEvent transactionEvent);

	/**
	 * Method removes expectation on sources that one originator 
	 * no long going to send feedback
	 * @param transactionEvent TransactionEvent
	 */
	void removeFeedbackSource(TransactionEvent transactionEvent);
	/**Method receives feedback from the sink container.
	 * 
	 * @param transactionEvent TransactionEvent
	 */
	void receiveFeedback(TransactionEvent transactionEvent);
}
