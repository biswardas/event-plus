package com.biswa.ep.deployment;

import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.transaction.FeedbackAgentImpl;
import com.biswa.ep.entities.transaction.SubscriptionAgent;
import com.biswa.ep.entities.transaction.SubscriptionAgentImpl;

public class LocalAccepterImpl extends Accepter {
	public LocalAccepterImpl(ContainerManager scm) {
		super(scm);
	}
	@Override
	public boolean listen(Listen listen, AbstractContainer sinkSchema) {
		String sourceName = listen.getContext()+"."+listen.getContainer();
		AbstractContainer sourceSchema = getContainerManager().getSchema(sourceName);
		sourceSchema.agent().connect(new ConnectionEvent(sourceName,sinkSchema.getName(),sinkSchema.agent(),buildFilter(sinkSchema,listen)));
		return true;
	}
	@Override
	public void replay(Listen listen, AbstractContainer sinkSchema) {
		String sourceName = listen.getContext()+"."+listen.getContainer();
		AbstractContainer sourceSchema = getContainerManager().getSchema(sourceName);
		sourceSchema.agent().replay(new ConnectionEvent(sourceName,sinkSchema.getName(),sinkSchema.agent(),buildFilter(sinkSchema,listen)));
	}
	@Override
	public boolean addFeedbackSource(Feedback feedback, AbstractContainer sinkSchema) {
		String listeningSchema = feedback.getContext()+"."+feedback.getContainer();
		AbstractContainer listeningContainer = getContainerManager().getSchema(listeningSchema);
		sinkSchema.agent().addFeedbackAgent(new FeedbackAgentImpl(feedbackAs(feedback,sinkSchema), listeningContainer.agent()));
		return true;
	}
	@Override
	public SubscriptionAgent getSubscriptionAgent(String context,
			String container) {
		String listeningSchema = context+"."+container;
		AbstractContainer listeningContainer = getContainerManager().getSchema(listeningSchema);
		return new SubscriptionAgentImpl(listeningContainer.agent());
	}
}
