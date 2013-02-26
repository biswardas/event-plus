package com.biswa.ep.entities.transaction;

/**Interface defined to feedback support for the containers who want to get
 * feedback to initiate next cycle. 
 * @author biswa
 *
 */
public interface FeedbackSupport {
	/**
	 * Method sets expectation on sources to expect feedback
	 * @param feedbackEvent FeedbackEvent
	 */
	void addFeedbackSource(FeedbackEvent feedbackEvent);

	/**
	 * Method removes expectation on sources that one originator 
	 * no long going to send feedback
	 * @param feedbackEvent FeedbackEvent
	 */
	void removeFeedbackSource(FeedbackEvent feedbackEvent);
	/**Method receives feedback from the sink container.
	 * 
	 * @param feedbackEvent FeedbackEvent
	 */
	void receiveFeedback(FeedbackEvent feedbackEvent);
}
