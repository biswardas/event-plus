package com.biswa.ep.deployment;

import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.transaction.FeedbackAgentImpl;
import com.biswa.ep.entities.transaction.SubscriptionAgent;
import com.biswa.ep.entities.transaction.SubscriptionAgentImpl;
import com.biswa.ep.entities.transaction.TransactionEvent;

public class LocalAccepterImpl extends Accepter {
	public LocalAccepterImpl(ContainerManager scm) {
		super(scm);
	}
	@Override
	public void listen(Listen listen, AbstractContainer cs) {
		String sourceName = listen.getContext()+"."+listen.getContainer();
		AbstractContainer sourceSchema = getContainerManager().getSchema(sourceName);
		AbstractContainer sinkSchema = getContainerManager().getSchema(cs.getName());
		sourceSchema.agent().connect(new ConnectionEvent(sourceName,cs.getName(),sinkSchema.agent(),buildFilter(listen)));
	}
	@Override
	public void replay(Listen listen, AbstractContainer cs) {
		String sourceName = listen.getContext()+"."+listen.getContainer();
		AbstractContainer sourceSchema = getContainerManager().getSchema(sourceName);
		AbstractContainer sinkSchema = getContainerManager().getSchema(cs.getName());
		sourceSchema.agent().replay(new ConnectionEvent(sourceName,cs.getName(),sinkSchema.agent(),buildFilter(listen)));
	}
	@Override
	public void addFeedbackSource(Feedback feedback, AbstractContainer cs) {
		String listeningSchema = feedback.getContext()+"."+feedback.getContainer();
		AbstractContainer listeningContainer = getContainerManager().getSchema(listeningSchema);
		listeningContainer.agent().addFeedbackSource(new TransactionEvent(feedbackAs(feedback,cs)));
		AbstractContainer originatingContainer = getContainerManager().getSchema(cs.getName());
		originatingContainer.agent().addFeedbackAgent(new FeedbackAgentImpl(feedbackAs(feedback,cs), listeningContainer.agent()));
	}
	@Override
	public SubscriptionAgent getSubscriptionAgent(String context,
			String container) {
		String listeningSchema = context+"."+container;
		AbstractContainer listeningContainer = getContainerManager().getSchema(listeningSchema);
		return new SubscriptionAgentImpl(listeningContainer.agent());
	}
}
