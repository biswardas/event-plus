package com.biswa.ep.entities.transaction;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerInsertEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.PropertyConstants;
import com.biswa.ep.entities.TransportEntry;

public class TransactionTrackerTest {
	private static final String SRCC = "SRCC";
	private static final String SRCA = "SRCA";
	private static final String SRCB = "SRCB";
	public static final String CON = "CON";
	private static final String[] expectedList = {CON,SRCA,SRCB};

	@Test
	public void testWhileNotConnected() {
		AbstractContainer abs = newContainer(new Properties());
		TransactionTracker tracker = abs.agent().transactionTracker;		
		Assert.assertEquals(0,tracker.getCurrentTransactionID());
		Assert.assertEquals(CON,tracker.getCurrentTransactionOrigin());
		Assert.assertEquals(0,tracker.getTransactionReadyQueue().size());
		Assert.assertEquals(null,tracker.getNext());
		Assert.assertEquals(0,tracker.getOpsInTransactionQueue());
		Assert.assertEquals(true,tracker.isIdle());		
		assertArrayEquals(new Integer[0],tracker.transactionsInProgress());
		assertArrayEquals(new String[]{CON},tracker.getKnownTransactionOrigins());		
	}

	@Test
	public void testSelfConnected() {
		AbstractContainer abs = newContainer(new Properties());
		abs.agent().addSource(new ConnectionEvent(CON,CON));
		abs.agent().connected(new ConnectionEvent(CON,CON));
		abs.agent().waitForEventQueueToDrain();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		Assert.assertEquals(0,tracker.getCurrentTransactionID());
		Assert.assertEquals(CON,tracker.getCurrentTransactionOrigin());
		Assert.assertEquals(0,tracker.getTransactionReadyQueue().size());
		Assert.assertEquals(null,tracker.getNext());
		Assert.assertEquals(0,tracker.getOpsInTransactionQueue());
		Assert.assertEquals(true,tracker.isIdle());		
		assertArrayEquals(new Integer[0],tracker.transactionsInProgress());
		assertArrayEquals(new String[]{CON},tracker.getKnownTransactionOrigins());		
	}
	
	@Test
	public void testConnectedWithUpstream() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
	}

	@Test
	public void testBeginDefaultTran() {
		final AbstractContainer abs = getConnectedContainer();
		final ContainerTask beginCommit = new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -196177963922732735L;

			@Override
			protected void runtask() {
				abs.agent().beginDefaultTran();
				abs.agent().commitDefaultTran();
			}
		};
		final ContainerTask beginRollback = new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -196177963922732735L;

			@Override
			protected void runtask() {
				abs.agent().beginDefaultTran();
				abs.agent().rollbackDefaultTran();
			}
		};
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().invokeOperation(beginCommit);
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
		abs.agent().invokeOperation(beginRollback);
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
	}


	
	@Test
	public void testBeginDefaultTranTwice() {
		final AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		final ContainerTask beginCommit = new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -196177963922732735L;

			@Override
			protected void runtask() {
				abs.agent().beginDefaultTran();
				abs.agent().beginDefaultTran();
				s.release();
			}
		};
		abs.agent().waitForEventQueueToDrain();
		if(s.tryAcquire()){
			Assert.fail("Transaction can not be overridden only be rolledback.");
		}
	}

	@Test
	public void testTrackBeginCommitTransaction() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCB,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
	}

	@Test
	public void testTrackBeginZeroTransaction() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB, 0));
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
	}

	@Test
	public void testTrackAttemptCommitWithZeroTransaction() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCB,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB, 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCB,tracker);
	}
	
	@Test
	public void testTrackAttemptRollbackWithZeroTransaction() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCB,tracker);
		abs.agent().rollbackTran(new TransactionEvent(SRCB, 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCB,tracker);
	}
	
	@Test
	public void testTrackBeginRollbackTransaction() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCB,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
	}
	
	@Test
	public void testTrackBeginCommitTransactionMultiPath() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,1,true,new Integer[]{12345},expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,2,true,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,0,1,false,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,1,1,false,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,1,1,false,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12346,0,0,false,new Integer[]{12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12346,0,0,false,new Integer[]{12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
	}
	
	@Test
	public void testTrackBeginCommitWithOperations() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		//Invalid Operation will be discarded with NPE
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[]{},expectedList,tracker);
		
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,2,true,new Integer[]{12345},expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12346));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,4,true,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,0,4,false,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,1,4,false,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,1,6,false,new Integer[]{12345,12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12346,0,0,false,new Integer[]{12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12346,0,0,false,new Integer[]{12346},expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
	}	
	
	@Test
	public void testTrackBeginCommitWithOperationsWhileDisconnected() {
		AbstractContainer abs = getConnectedContainer();
		abs.agent().addSource(new ConnectionEvent(SRCC,CON));
		abs.agent().waitForEventQueueToDrain();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertFalse(abs.isConnected());
		checkInitialState(tracker);
		//Invalid Operation will be discarded with NPE
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));

		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12346));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().connected(new ConnectionEvent(SRCC,CON,new String[]{SRCA,SRCC}));
		abs.agent().waitForEventQueueToDrain();
		Assert.assertTrue(abs.isConnected());
		checkOneValidTransactionFrom(CON,0,0,4,true,new Integer[]{12345,12346},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		abs.agent().beginTran(new TransactionEvent(SRCC,SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCC,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,1,2,false,new Integer[]{12345,12346},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		abs.agent().commitTran(new TransactionEvent(SRCC,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12346,0,0,false,new Integer[]{12346},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		abs.agent().commitTran(new TransactionEvent(SRCC,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[]{},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		
	}	
	
	@Test
	public void testAbuse() {
		AbstractContainer abs = getConnectedContainer();
		abs.agent().addSource(new ConnectionEvent(SRCC,CON));
		abs.agent().waitForEventQueueToDrain();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertFalse(abs.isConnected());
		checkInitialState(tracker);
		//Invalid Operation will be discarded with NPE
		abs.agent().beginTran(new TransactionEvent("UNKNOWN", 12345));
		abs.agent().commitTran(new TransactionEvent(SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 1234566));
		
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12346));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCB,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[0],expectedList,tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkInitialState(tracker);
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().connected(new ConnectionEvent(SRCC,CON,new String[]{SRCA,SRCC}));
		abs.agent().waitForEventQueueToDrain();
		Assert.assertTrue(abs.isConnected());
		checkOneValidTransactionFrom(CON,0,0,4,true,new Integer[]{12345,12346},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		abs.agent().beginTran(new TransactionEvent(SRCC,SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCC,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,1,2,false,new Integer[]{12345,12346},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().commitTran(new TransactionEvent(SRCC,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12346,0,0,false,new Integer[]{12346},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		abs.agent().commitTran(new TransactionEvent(SRCC,SRCA, 12346));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[]{},new String[]{CON,SRCA,SRCB,SRCC},tracker);
		
	}

	@Test
	public void testOrphanOperationDiscard() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		//Invalid Operation will be discarded with NPE
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 12345));
	}

	@Test
	public void testDropSourceWhileTIP() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,1,true,new Integer[]{12345},expectedList,tracker);
		abs.agent().dropSource(new ConnectionEvent(SRCB, CON));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,0,0,false,new Integer[]{12345},new String[]{SRCA,CON},tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[]{},new String[]{SRCA,CON},tracker);
	}
	@Test
	public void testDropSourceWhileAwaitingCommit() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().beginTran(new TransactionEvent(SRCB,SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,0,0,false,new Integer[]{12345},expectedList,tracker);
		abs.agent().dropSource(new ConnectionEvent(SRCB, CON));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,0,0,false,new Integer[]{12345},new String[]{SRCA,CON},tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[]{},new String[]{SRCA,CON},tracker);
	}
	@Test
	public void testDropSource() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);		
		abs.agent().dropSource(new ConnectionEvent(SRCB, CON));
		abs.agent().beginTran(new TransactionEvent(SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCA,12345,0,0,false,new Integer[]{12345},new String[]{SRCA,CON},tracker);
		abs.agent().commitTran(new TransactionEvent(SRCA, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[]{},new String[]{SRCA,CON},tracker);
	}
	
	@Test(expected=IllegalStateException.class)
	public void canNotOverrideTransaction() {
		AbstractContainer abs = getConnectedContainer();
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(SRCB, 12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(SRCB,tracker);
		abs.agent().waitForEventQueueToDrain();
		tracker.beginDefaultTran();
	}
	
	@Test
	public void testNormalResumePostTimeout() throws InterruptedException {
		int timeoutInMillis = 2000;
		final AbstractContainer abs = getTimeoutContainer(String.valueOf(timeoutInMillis));
		TransactionTracker tracker = abs.agent().transactionTracker;
		Assert.assertTrue(abs.isConnected());
		checkInitialState(tracker);
		abs.agent().beginTran(new TransactionEvent(CON,12345));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,12345,0,0,false,new Integer[]{12345},expectedList,tracker);		
		//Invalid Operation will be discarded with NPE
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().entryAdded(new ContainerInsertEvent(SRCA,new TransportEntry(1), 0));
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,12345,0,2,false,new Integer[]{12345},expectedList,tracker);		
		abs.agent().waitForEventQueueToDrain();		
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		abs.agent().invokeOperation(new ContainerTask(){
			@Override
			protected void runtask() throws Throwable {
				s.release();
			}
		});
		//Check rollback just before 10 milli seconds
		int premature = timeoutInMillis-10;
		if(s.tryAcquire(premature, TimeUnit.MILLISECONDS)){
			Assert.fail("Timed Out too early than as specified:"+timeoutInMillis);
		}
		//Check rollback just after 10 milli seconds
		if(!s.tryAcquire(timeoutInMillis-premature+10, TimeUnit.MILLISECONDS)){
			Assert.fail("Post rollback queued operations did not fire timeout in millis:"+timeoutInMillis);
		}
		abs.agent().waitForEventQueueToDrain();
		checkOneValidTransactionFrom(CON,0,0,0,true,new Integer[]{},expectedList,tracker);
	}
	
	private AbstractContainer getConnectedContainer() {
		AbstractContainer abs = newContainer(new Properties());
		abs.agent().addSource(new ConnectionEvent(SRCA,CON));
		abs.agent().addSource(new ConnectionEvent(SRCB,CON));
		abs.agent().connected(new ConnectionEvent(SRCA,CON,new String[]{SRCA}));
		abs.agent().connected(new ConnectionEvent(SRCB,CON,new String[]{SRCA,SRCB}));
		abs.agent().waitForEventQueueToDrain();
		return abs;
	}

	protected ConcreteContainer newContainer(Properties props) {
		return new ConcreteContainer(CON, props);
	}
	
	private AbstractContainer getTimeoutContainer(String delay){
		Properties props = new Properties();
		props.put(PropertyConstants.TRAN_TIME_OUT, delay);
		AbstractContainer abs = new ConcreteContainer(CON,props);
		abs.agent().addSource(new ConnectionEvent(SRCA,CON));
		abs.agent().addSource(new ConnectionEvent(SRCB,CON));
		abs.agent().connected(new ConnectionEvent(SRCA,CON,new String[]{SRCA}));
		abs.agent().connected(new ConnectionEvent(SRCB,CON,new String[]{SRCA,SRCB}));
		abs.agent().waitForEventQueueToDrain();
		return abs;
	}
	
	private void checkInitialState(TransactionTracker tracker) {
		Integer[] tranInProgress = new Integer[0];
		checkOneValidTransactionFrom(CON,0,0,0,true,tranInProgress,expectedList,tracker);
	}
	
	private void checkOneValidTransactionFrom(String origin,TransactionTracker tracker){
		Integer[] tranInProgress = {tracker.getCurrentTransactionID()};
		checkOneValidTransactionFrom(origin,tracker.getCurrentTransactionID(),0,0,false,tranInProgress,expectedList,tracker);
	}
	
	private void checkOneValidTransactionFrom(String origin,int transactionId,int readyQueueSize,int opsInTransQueue,boolean idle,Integer[] tranInProgress,String[] expectedSources,TransactionTracker tracker) {
		Assert.assertEquals(transactionId,tracker.getCurrentTransactionID());
		Assert.assertEquals(origin,tracker.getCurrentTransactionOrigin());
		Assert.assertEquals(readyQueueSize,tracker.getTransactionReadyQueue().size());
		Assert.assertEquals(opsInTransQueue,tracker.getOpsInTransactionQueue());
		Assert.assertEquals(idle,tracker.isIdle());		
		Assert.assertEquals(tranInProgress.length,tracker.transactionsInProgress().length);
		Assert.assertTrue(orginListSame(Arrays.asList(tranInProgress),Arrays.asList(tracker.transactionsInProgress())));
		Assert.assertEquals(expectedSources.length,tracker.getKnownTransactionOrigins().length);
		Assert.assertTrue(orginListSame(Arrays.asList(expectedSources),Arrays.asList(tracker.getKnownTransactionOrigins())));
		Assert.assertEquals(null,tracker.getNext());
	}
	
	private boolean orginListSame(List<? extends Object> expected,List<? extends Object> incoming) {
		ArrayList<Object> al = new ArrayList<Object>(expected);
		al.removeAll(incoming);
		return al.isEmpty();
	}
}
