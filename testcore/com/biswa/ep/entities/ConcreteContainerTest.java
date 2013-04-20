package com.biswa.ep.entities;

import java.util.Properties;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.biswa.ep.entities.transaction.TransactionEvent;

public class ConcreteContainerTest {
	private static final Object SC = "HELLO";
	private static final LeafAttribute ATTRIBUTE = new LeafAttribute("HELLO");
	private static final int TRANID = 9000000;
	private static final String SINK="SINK";
	private static final String SOURCEA="SOURCEA";
	private static final String SOURCEB="SOURCEB";
	ConcreteContainer conc;
	Semaphore semaphore = new Semaphore(1);
	
	@Before
	public void setUp() throws Exception {
		semaphore.drainPermits();
		conc = new ConcreteContainer(SINK, new Properties());
	}

	@After
	public void cleanUp() throws Exception {
		conc.agent().destroy();
	}

	/**
	 * Operation happening in Container thread this ensures when assertion is performed
	 * container thread is no doing anything relevant.
	 */
	private void pipeClean() {
		conc.agent().getEventCollector().execute(new Runnable() {
			@Override
			public void run(){				
				semaphore.release();
			}
		});
		semaphore.acquireUninterruptibly();
	}
	
	@Test
	public void holdTransactionTillConnected() {
		conc.agent().addSource(new ConnectionEvent(SOURCEA, SINK));
		conc.agent().addSource(new ConnectionEvent(SOURCEB, SINK));
		
		conc.agent().connected(new ConnectionEvent(SOURCEA, SINK,new String[]{SOURCEA}));
		conc.agent().beginTran(new TransactionEvent(SOURCEA, SOURCEA,TRANID));
		
		pipeClean();
		
		Assert.assertFalse(conc.isConnected());
		
		conc.agent().connected(new ConnectionEvent(SOURCEB, SINK,new String[]{SOURCEA,SOURCEB}));

		pipeClean();
		
		Assert.assertTrue(conc.isConnected());
		
		Assert.assertEquals(0,conc.agent().getCurrentTransactionID());
		Assert.assertEquals(SINK,conc.agent().getCurrentTransactionOrigin());
		
		conc.agent().beginTran(new TransactionEvent(SOURCEB, SOURCEA,TRANID));
		
		pipeClean();
		
		Assert.assertTrue(conc.isConnected());
		
		Assert.assertEquals(TRANID,conc.agent().getCurrentTransactionID());
		Assert.assertEquals(SOURCEA,conc.agent().getCurrentTransactionOrigin());

		conc.agent().commitTran(new TransactionEvent(SOURCEA, SOURCEA,TRANID));
		
		pipeClean();
		
		Assert.assertEquals(TRANID,conc.agent().getCurrentTransactionID());
		Assert.assertEquals(SOURCEA,conc.agent().getCurrentTransactionOrigin());
		conc.agent().commitTran(new TransactionEvent(SOURCEB, SOURCEA,TRANID));
		
		pipeClean();
		
		Assert.assertEquals(0,conc.agent().getCurrentTransactionID());
		Assert.assertEquals(SINK,conc.agent().getCurrentTransactionOrigin());
	}

	@Test
	public void holdOperationsTillConnected() {
		conc.agent().addSource(new ConnectionEvent(SOURCEA, SINK));
		conc.agent().addSource(new ConnectionEvent(SOURCEB, SINK));
		conc.agent().attributeAdded(new ContainerStructureEvent(SINK, ATTRIBUTE));
		conc.agent().waitForEventQueueToDrain();
		Assert.assertEquals(1, conc.agent().getPreConnectedQueueSize());
		conc.agent().connected(new ConnectionEvent(SOURCEA, SINK,new String[]{SOURCEA}));
		conc.agent().beginTran(new TransactionEvent(SOURCEA, SOURCEA,TRANID));
		conc.agent().entryAdded(new ContainerInsertEvent(SOURCEA,new TransportEntry(100),TRANID));
		conc.agent().entryAdded(new ContainerUpdateEvent(SOURCEA,100,ATTRIBUTE,SC,TRANID));
		conc.agent().entryAdded(new ContainerDeleteEvent(SOURCEA,100,TRANID));
		conc.agent().waitForEventQueueToDrain();
		Assert.assertEquals(4, conc.agent().getPostConnectedQueueSize());
		conc.agent().connected(new ConnectionEvent(SOURCEB, SINK,new String[]{SOURCEA,SOURCEB}));
		conc.agent().waitForEventQueueToDrain();
		Assert.assertEquals(0, conc.agent().getPreConnectedQueueSize());
	}
	
	@Test
	public void testUnInvitedGuest() {
		conc.agent().addSource(new ConnectionEvent(SINK, SINK));
		conc.agent().connected(new ConnectionEvent(SINK, SINK));
		conc.agent().beginTran(new TransactionEvent("X", "X",TRANID));
		pipeClean();
		Assert.assertEquals(0,conc.agent().getCurrentTransactionID());
		Assert.assertEquals(SINK,conc.agent().getCurrentTransactionOrigin());

		conc.agent().commitTran(new TransactionEvent("X", "X",TRANID));
		pipeClean();
		Assert.assertEquals(0,conc.agent().getCurrentTransactionID());
		Assert.assertEquals(SINK,conc.agent().getCurrentTransactionOrigin());
		
		
		conc.agent().beginTran(new TransactionEvent(SINK, SINK,TRANID));
		pipeClean();
		Assert.assertEquals(TRANID,conc.agent().getCurrentTransactionID());
		Assert.assertEquals(SINK,conc.agent().getCurrentTransactionOrigin());
	}

}
