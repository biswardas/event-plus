package com.biswa.ep.entities;


import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import org.junit.Test;

import com.biswa.ep.entities.substance.InvalidSubstance;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.FeedbackEvent;
import com.biswa.ep.entities.transaction.TransactionEvent;
public class FeedbackContainerTest {
	private static final String SOURCE = "SOURCE";
	private static final String SINK = "SINK";
	private static final Attribute attr = new LeafAttribute("TEST");
	AbstractContainer source;
	
	@Before
	public void setUp() throws Exception {
		source = new FeedbackAwareContainer(SOURCE,new Properties());
		source.agent().addSource(new ConnectionEvent(SOURCE,SOURCE));
		source.agent().attributeAdded(new ContainerStructureEvent(SOURCE,attr));
		source.agent().connected(new ConnectionEvent(SOURCE,SOURCE));
		source.agent().waitForEventQueueToDrain();
		Assert.assertTrue(source.agent().isConnected());
	}

	@After
	public void tearDown() throws Exception {
		source.agent().destroy();
	}
	
	@Test
	public void testNonTransactionalOrigin() throws InterruptedException{
		final AtomicInteger keeptranID = new AtomicInteger();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		Agent agent = new Agent(new StaticContainer(SINK, new Properties())){
			@Override
			public void connected(ConnectionEvent ce) {
				System.out.println("Received ConnectedEvent:"+ce);
				s.release();
			}

			@Override
			public void entryAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);				
				s.release();
			}

			@Override
			public void entryRemoved(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				s.release();
			}

			@Override
			public void entryUpdated(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				s.release();
			}
			
			@Override
			public void attributeAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
			}

			@Override
			public void beginTran(TransactionEvent te) {
				System.out.println("Received Transaction Started:"+te);
			}

			@Override
			public void commitTran(TransactionEvent te) {
				System.out.println("Received Transaction Completed:"+te);
			}
			
		};
		source.agent().connect(new ConnectionEvent(SOURCE,SINK,agent));
		source.agent().addFeedbackSource(new FeedbackEvent(SINK));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Connected Event is not received");
		}
		
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10000),0));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Entry Added Event is not received");
		}
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10001),0));
		if(s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Entry Added Event should not have been received as sink not yet given feedback");
		}
		source.agent().receiveFeedback(new FeedbackEvent(SINK,keeptranID.get()));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Now I have given feedback entitled to receive another Entry Added event");
		}
		source.agent().entryRemoved(new ContainerDeleteEvent(SOURCE,10001,0));
		if(s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Entry Removed Event should not have been received as sink not yet given feedback");
		}
		source.agent().receiveFeedback(new FeedbackEvent(SINK,keeptranID.get()));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Now I have given feedback entitled to receive above Entry Removed event");
		}
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,0));
		if(s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Entry Removed Event should not have been received as sink not yet given feedback");
		}
		source.agent().receiveFeedback(new FeedbackEvent(SINK,keeptranID.get()));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Now I have given feedback entitled to receive above Entry Updated event");
		}
	}

	@Test
	public void testTransactionalOrigin() throws InterruptedException{
		final AtomicInteger keeptranID = new AtomicInteger();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		Agent agent = new Agent(new StaticContainer(SINK, new Properties())){
			@Override
			public void connected(ConnectionEvent ce) {
				System.out.println("Received ConnectedEvent:"+ce);
				s.release();
			}

			@Override
			public void entryAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);				
				s.release();
			}

			@Override
			public void entryRemoved(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				s.release();
			}

			@Override
			public void entryUpdated(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				s.release();
			}
			
			@Override
			public void attributeAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
			}

			@Override
			public void beginTran(TransactionEvent te) {
				System.out.println("Received Transaction Started:"+te);
			}

			@Override
			public void commitTran(TransactionEvent te) {
				System.out.println("Received Transaction Completed:"+te);
			}
			
		};
		source.agent().connect(new ConnectionEvent(SOURCE,SINK,agent));
		source.agent().addFeedbackSource(new FeedbackEvent(SINK));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Connected Event is not received");
		}
		source.agent().beginTran(new TransactionEvent(SOURCE,987654));
		//Create
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10000),987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Delete
		source.agent().entryRemoved(new ContainerDeleteEvent(SOURCE,10000,987654));
		if(s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Entry Removed Event should not have been received as sink not yet given feedback");
		}
		source.agent().commitTran(new TransactionEvent(SOURCE,987654));
		if(s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("There is really nothing to be received as CRUD wiped everything");
		}
	}
	
	@Test
	public void testCollapsing() throws InterruptedException{
		final AtomicInteger keeptranID = new AtomicInteger();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		Agent agent = new Agent(new StaticContainer(SINK, new Properties())){
			@Override
			public void connected(ConnectionEvent ce) {
				System.out.println("Received ConnectedEvent:"+ce);
				s.release();
			}

			@Override
			public void entryAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);				
				s.release();
			}

			@Override
			public void entryRemoved(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				s.release();
			}

			@Override
			public void entryUpdated(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				s.release();
			}
			
			@Override
			public void attributeAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
			}

			@Override
			public void beginTran(TransactionEvent te) {
				System.out.println("Received Transaction Started:"+te);
			}

			@Override
			public void commitTran(TransactionEvent te) {
				System.out.println("Received Transaction Completed:"+te);
			}
			
		};
		source.agent().connect(new ConnectionEvent(SOURCE,SINK,agent));
		source.agent().addFeedbackSource(new FeedbackEvent(SINK));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Connected Event is not received");
		}
		source.agent().beginTran(new TransactionEvent(SOURCE,987654));
		//Create
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10000),987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		//Update
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10000,attr,InvalidSubstance.INVALID_SUBSTANCE,987654));
		if(s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("Entry Removed Event should not have been received as sink not yet given feedback");
		}
		source.agent().commitTran(new TransactionEvent(SOURCE,987654));
		if(!s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("I was entitled to receive at least one Insert");
		}
		if(s.tryAcquire(1,TimeUnit.SECONDS)){
			Assert.fail("All above should have been collapsed to Just one insert.");
		}
	}
}
