package com.biswa.ep.subscription;

import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import com.biswa.ep.EPEvent;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.FeedbackEvent;
import com.biswa.ep.entities.transaction.TransactionEvent;

public class SubscriptionContainerTest {
	private static final String SUBSCRIPTION_CONTAINER = "subscriptionContainer";

	private static final String LISTENING_CONTAINER = "ListeningContainer";
	SubscriptionRequest subRequest = new SubscriptionRequest(LISTENING_CONTAINER,100, new LeafAttribute("Result"));
	Substance substance = new ObjectSubstance("BISWA");
	SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,SUBSCRIPTION_CONTAINER,subRequest);
	
	SubscriptionContainer subscriptionContainer = null;	
	Properties props = new Properties();
	Semaphore semaphore = new Semaphore(1);
	private ConcreteContainer listeningContainer = new ConcreteContainer(LISTENING_CONTAINER, new Properties());
	
	Agent agent = new Agent(listeningContainer){//Fictitious agent 

		@Override
		public void beginTran(TransactionEvent te) {
			System.out.println("TransactionEvent BeginTran:"+te);
			semaphore.release();
		}

		@Override
		public void commitTran(TransactionEvent te) {
			System.out.println("TransactionEvent CommitTran:"+te);
			semaphore.release();
			subscriptionContainer.agent().completionFeedback(te.getTransactionId());
		}

		@Override
		public void attributeAdded(ContainerEvent ce) {
			fail("Should not receive this");
		}

		@Override
		public void attributeRemoved(ContainerEvent ce) {
			fail("Should not receive this");
		}

		@Override
		public void entryAdded(ContainerEvent ce) {
			fail("Should not receive this");
		}
		
		@Override
		public void connected(final ConnectionEvent ce) {
			System.out.println("Received ConnectionEvent:"+ce);
			semaphore.release();
		}

		@Override
		public void entryRemoved(ContainerEvent ce) {
			fail("Should not receive this");
		}

		@Override
		public void entryUpdated(ContainerEvent ce) {
			System.out.println("Updated:"+ce);
			semaphore.release();
		}		
	};
	
	@Test
	public void testSubscribe() {
		buildSubscriptionContainer();
		subscriptionContainer.agent().attributeAdded(new ContainerStructureEvent(subscriptionContainer.getName(),new DummySubscriptionProcessor()));
		
		
		semaphore.drainPermits();
		subscriptionContainer.agent().subscribe(subscriptionEvent);
		subscriptionContainer.agent().substitute(subscriptionEvent);
		subscriptionContainer.agent().unsubscribe(subscriptionEvent);
		subscriptionContainer.agent().subscribe(subscriptionEvent);
		subscriptionContainer.agent().substitute(subscriptionEvent);
		subscriptionContainer.agent().unsubscribe(subscriptionEvent);
		subscriptionContainer.agent().subscribe(subscriptionEvent);
		subscriptionContainer.agent().substitute(subscriptionEvent);
		subscriptionContainer.agent().unsubscribe(subscriptionEvent);
		subscriptionContainer.agent().subscribe(subscriptionEvent);
		subscriptionContainer.agent().substitute(subscriptionEvent);
		subscriptionContainer.agent().unsubscribe(subscriptionEvent);
		subscriptionContainer.agent().subscribe(subscriptionEvent);
		subscriptionContainer.agent().substitute(subscriptionEvent);
		subscriptionContainer.agent().unsubscribe(subscriptionEvent);
		subscriptionContainer.agent().subscribe(subscriptionEvent);
		subscriptionContainer.agent().substitute(subscriptionEvent);
		subscriptionContainer.agent().unsubscribe(subscriptionEvent);
		//-------------------------------------------------------
		subscriptionContainer.agent().subscribe(subscriptionEvent);
		semaphore.acquireUninterruptibly();
		semaphore.acquireUninterruptibly(3);
		semaphore.acquireUninterruptibly(3);
		semaphore.acquireUninterruptibly(3);
	}
	protected SubscriptionContainer buildSubscriptionContainer() {
		semaphore.drainPermits();
		subscriptionContainer = new SubscriptionContainer(SUBSCRIPTION_CONTAINER,props);
		subscriptionContainer.agent().addSource(new ConnectionEvent(EPEvent.DEF_SRC, EPEvent.DEF_SRC));
		subscriptionContainer.agent().connected(new ConnectionEvent(EPEvent.DEF_SRC, EPEvent.DEF_SRC));
		subscriptionContainer.agent().addFeedbackSource(new FeedbackEvent(LISTENING_CONTAINER));
		subscriptionContainer.agent().connect(new ConnectionEvent(SUBSCRIPTION_CONTAINER, LISTENING_CONTAINER, agent));
		semaphore.acquireUninterruptibly();
		return subscriptionContainer;
	}

}
