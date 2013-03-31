package com.biswa.ep.entities.transaction;
/** Local Feedback agent.
 * 
 * @author biswa
 *
 */
public class FeedbackAgentImpl extends FeedbackAgent {
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
		agent.receiveFeedback(new FeedbackEvent(originator,transactionId));
	}
	@Override
	public String getFeedBackConsumer() {
		return agent.getName();
	}
}
