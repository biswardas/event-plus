package com.biswa.ep.entities.transaction;
/** Local Feedback agent.
 * 
 * @author biswa
 *
 */
public class FeedbackAgentImpl implements FeedbackAgent {
	/**
	 * Feedback listening container agent.
	 */
	private Agent agent;
	/**
	 * Originator source.
	 */
	private String originator;
	/**Constructor to build a feedback agent.
	 * 
	 * @param originator String
	 * @param agent Agent
	 */
	public FeedbackAgentImpl(String originator,Agent agent){
		this.agent=agent;
		this.originator = originator;
	}
	@Override
	public void completionFeedback(int transactionId) {
		agent.receiveFeedback(new TransactionEvent(originator,transactionId));
	}
}
