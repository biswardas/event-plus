package com.biswa.ep.subscription;

import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.TransactionEvent;
import com.biswa.ep.subscription.ChannelContainer;
import com.biswa.ep.subscription.SubscriptionEvent;
import com.biswa.ep.subscription.SubscriptionRequest;

public class ChannelContainerTest {
	private static final String CHANNEL_CONTAINER = "channelContainer";

	private static final String LISTENING_CONTAINER = "ListeningContainer";
	SubscriptionRequest subRequest = new SubscriptionRequest(LISTENING_CONTAINER,100, new LeafAttribute("Result"));
	Substance substance = new ObjectSubstance("BISWA");
	SubscriptionEvent subscriptionEvent = new SubscriptionEvent(substance,CHANNEL_CONTAINER,subRequest);
	
	ChannelContainer channel = null;	
	Properties props = new Properties();
	Semaphore semaphore = new Semaphore(1);
	private ConcreteContainer listeningContainer = new ConcreteContainer(LISTENING_CONTAINER, new Properties());
	
	Agent agent = new Agent(listeningContainer){//Fictious agent 

		@Override
		public void beginTran(TransactionEvent te) {
			System.out.println("TransactionEvent BeginTran:"+te);
			semaphore.release();
		}

		@Override
		public void commitTran(TransactionEvent te) {
			System.out.println("TransactionEvent CommitTran:"+te);
			semaphore.release();
			channel.agent().completionFeedback(te.getTransactionId());
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
		buildChannelContainer();
		channel.agent().attributeAdded(new ContainerStructureEvent(channel.getName(),new DummySubscriptionProcessor()));
		
		
		semaphore.drainPermits();
		channel.agent().subscribe(subscriptionEvent);
		channel.agent().substitute(subscriptionEvent);
		channel.agent().unsubscribe(subscriptionEvent);
		channel.agent().subscribe(subscriptionEvent);
		channel.agent().substitute(subscriptionEvent);
		channel.agent().unsubscribe(subscriptionEvent);
		channel.agent().subscribe(subscriptionEvent);
		channel.agent().substitute(subscriptionEvent);
		channel.agent().unsubscribe(subscriptionEvent);
		channel.agent().subscribe(subscriptionEvent);
		channel.agent().substitute(subscriptionEvent);
		channel.agent().unsubscribe(subscriptionEvent);
		channel.agent().subscribe(subscriptionEvent);
		channel.agent().substitute(subscriptionEvent);
		channel.agent().unsubscribe(subscriptionEvent);
		channel.agent().subscribe(subscriptionEvent);
		channel.agent().substitute(subscriptionEvent);
		channel.agent().unsubscribe(subscriptionEvent);
		//-------------------------------------------------------
		channel.agent().subscribe(subscriptionEvent);
		semaphore.acquireUninterruptibly();
		semaphore.acquireUninterruptibly(3);
		semaphore.acquireUninterruptibly(3);
		semaphore.acquireUninterruptibly(3);
	}
	protected ChannelContainer buildChannelContainer() {
		semaphore.drainPermits();
		channel = new ChannelContainer(CHANNEL_CONTAINER,props);
		channel.agent().addSource(new ConnectionEvent("ANONYMOUS", "ANONYMOUS"));
		channel.agent().connected(new ConnectionEvent("ANONYMOUS", "ANONYMOUS"));
		channel.agent().addFeedbackSource(new TransactionEvent(LISTENING_CONTAINER));
		channel.agent().connect(new ConnectionEvent(CHANNEL_CONTAINER, LISTENING_CONTAINER, agent));
		semaphore.acquireUninterruptibly();
		return channel;
	}

}
