package com.biswa.ep.entities;


import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.biswa.ep.entities.substance.InvalidSubstance;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.FeedbackAgentImpl;
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
	public void testTransactionalCollapsing() throws InterruptedException{
		final AtomicInteger atom = new AtomicInteger();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		Agent agent = new Agent(new ConcreteContainer(SINK, new Properties())){
			@Override
			public void entryAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				atom.incrementAndGet();
			}
			@Override
			public void entryRemoved(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				atom.incrementAndGet();
			}
			@Override
			public void entryUpdated(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
				atom.incrementAndGet();
			}

			@Override
			public void commitTran(TransactionEvent te) {
				System.out.println("Received Transaction Completed:"+te);			
				s.release();
				super.commitTran(te);
			}
			
		};
		source.agent().connect(new ConnectionEvent(SOURCE,SINK,agent));
		agent.addFeedbackAgent(new FeedbackAgentImpl(SINK,source.agent()));
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
		source.agent().commitTran(new TransactionEvent(SOURCE,987654));
		s.acquireUninterruptibly();
		Assert.assertEquals("All above updates should have been collapsed to just one Insert.",1,atom.get());
		Thread.sleep(1000);
	}

	@Test
	public void testThrottleQueued() throws InterruptedException{
		final Semaphore tran = new Semaphore(1);
		final int lock=1000;
		final CountDownLatch s = new CountDownLatch(lock*2);
		ConcreteContainer sink = new ConcreteContainer(SINK, new Properties()){
			@Override
			protected void dispatchBeginTransaction(){
				System.out.println("Begining Transaction:"+agent().getCurrentTransactionID());
				super.dispatchBeginTransaction();
			}
			@Override
			protected void dispatchCommitTransaction(){
				super.dispatchCommitTransaction();
				System.out.println("Committed Transaction:"+agent().getCurrentTransactionID());
			}
			@Override
			protected void dispatchFeedback(){
				super.dispatchFeedback();
				System.out.println("Dispatched Feedback"+agent().getCurrentTransactionID());
			}
			@Override
			public void dispatchEntryAdded(ContainerEntry containerEntry){
				super.dispatchEntryAdded(containerEntry);
				s.countDown();
			}
		};
		source.agent().connect(new ConnectionEvent(SOURCE,SINK,sink.agent()));
		sink.agent().addFeedbackAgent(new FeedbackAgentImpl(SINK,source.agent()));
		Thread t= new Thread(){
			public void run(){
				for(int i=0;i<lock;i++){
					source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(5000000+i),0));
					insertDelay();
				}
			}
		};
		t.start();
		for(int i=0;i<lock;i++){
			source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(3000000+i),0));
			insertDelay();
		}
		s.await();
		tran.acquire();
		Thread.sleep(1000);
	}

	private void insertDelay() {
		try {
			Thread.sleep(0,100);
		} catch (InterruptedException e) {
		}
	}
}
