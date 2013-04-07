package com.biswa.ep.entities;


import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.biswa.ep.entities.substance.InvalidSubstance;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.TransactionEvent;
public class TimedContainerTest {
	private static final int TRANID = 12345;
	int FLUSH_MILLI_SECS= 2000;
	private static final String SOURCE = "SOURCE";
	private static final String SINK = "SINK";
	private static final Attribute attr = new LeafAttribute("TEST");
	AbstractContainer source;
	
	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();
		props.put("timed.interval",String.valueOf(FLUSH_MILLI_SECS));
		source = new TimedContainer(SOURCE,props);
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
		final CountDownLatch insertCounter = new CountDownLatch(3);
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
				insertCounter.countDown();
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
				s.release();
			}
			
		};
		source.agent().connect(new ConnectionEvent(SOURCE,SINK,agent));
		s.acquireUninterruptibly();

		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10000),0));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10001),0));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10002),0));
		insertCounter.await();
		s.acquireUninterruptibly();
		
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10004),0));
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10004,attr,InvalidSubstance.INVALID_SUBSTANCE,0));
		source.agent().entryRemoved(new ContainerDeleteEvent(SOURCE,10004,0));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10004),0));
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10004,attr,InvalidSubstance.INVALID_SUBSTANCE,0));
		source.agent().entryRemoved(new ContainerDeleteEvent(SOURCE,10004,0));
		s.acquireUninterruptibly();
	}
	
	@Test
	public void testTransactionalOrigin() throws InterruptedException{
		final CountDownLatch insertCounter = new CountDownLatch(3);
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
				insertCounter.countDown();
			}
			
			@Override
			public void attributeAdded(ContainerEvent ce) {
				System.out.println("Received ContainerEvent:"+ce);
			}

			@Override
			public void commitTran(TransactionEvent te) {
				System.out.println("Received Transaction Completed:"+te);
				s.release();
			}
			
		};
		source.agent().connect(new ConnectionEvent(SOURCE,SINK,agent));
		s.acquireUninterruptibly();
		source.agent().beginTran(new TransactionEvent(SOURCE,TRANID));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10000),TRANID));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10001),TRANID));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10002),TRANID));
		source.agent().commitTran(new TransactionEvent(SOURCE,TRANID));
		insertCounter.await();
		s.acquireUninterruptibly();
		source.agent().beginTran(new TransactionEvent(SOURCE,TRANID));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10004),TRANID));
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10004,attr,InvalidSubstance.INVALID_SUBSTANCE,TRANID));
		source.agent().entryRemoved(new ContainerDeleteEvent(SOURCE,10004,TRANID));
		source.agent().entryAdded(new ContainerInsertEvent(SOURCE,new TransportEntry(10004),TRANID));
		source.agent().entryUpdated(new ContainerUpdateEvent(SOURCE,10004,attr,InvalidSubstance.INVALID_SUBSTANCE,TRANID));
		source.agent().entryRemoved(new ContainerDeleteEvent(SOURCE,10004,TRANID));
		source.agent().commitTran(new TransactionEvent(SOURCE,TRANID));
		s.acquireUninterruptibly();
	}
}
