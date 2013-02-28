package com.biswa.ep.entities.transaction;

import static org.junit.Assert.assertArrayEquals;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

import com.biswa.ep.EPEvent;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.PropertyConstants;

public class MultiGroupTransactionTrackerTest {

	private static final String CON_NAME = "F";
	private TransactionTracker getTransactionTracker(){
		Properties props = new Properties();
		props.put(PropertyConstants.TRAN_TIME_OUT, "10000");
		ConcreteContainer cs = new ConcreteContainer(CON_NAME,props){
			@Override
			public boolean isConnected() {
				return true;
			}	
		};
		TransactionAdapter transactionAdapter = cs.agent();
		transactionAdapter.transactionTracker.addSource("X",new String[]{"A","X"});
		transactionAdapter.transactionTracker.addSource("Y",new String[]{"A","B","Y"});
		return transactionAdapter.transactionTracker;
	}
	@Test
	public void testTransactionTracker() {
		TransactionTracker transactionTracker = getTransactionTracker();
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
	}
	@Test
	public void testTransactionTrackerWithEmptyEvent() {
		TransactionTracker transactionTracker = getTransactionTracker();
		
		transactionTracker.trackBeginTransaction(new TransactionEvent());
		assertArrayEquals(new Integer[]{0}, transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(EPEvent.DEF_SRC, transactionTracker.getCurrentTransactionOrigin());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent());
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());

		transactionTracker.trackBeginTransaction(new TransactionEvent());
		assertArrayEquals(new Integer[]{0}, transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(EPEvent.DEF_SRC, transactionTracker.getCurrentTransactionOrigin());
		
		transactionTracker.trackRollbackTransaction(new TransactionEvent());
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
	}
	@Test
	public void testSelfInitiatedTransactionWithEmptyEvent() {
		TransactionTracker transactionTracker = getTransactionTracker();
		
		transactionTracker.trackBeginTransaction(new TransactionEvent(CON_NAME));
		assertArrayEquals(new Integer[]{0}, transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(CON_NAME, transactionTracker.getCurrentTransactionOrigin());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent(CON_NAME));
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());

		transactionTracker.trackBeginTransaction(new TransactionEvent(CON_NAME));
		assertArrayEquals(new Integer[]{0}, transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(CON_NAME, transactionTracker.getCurrentTransactionOrigin());
		
		transactionTracker.trackRollbackTransaction(new TransactionEvent(CON_NAME));
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
	}
	@Test
	public void testOriginInitatedTransaction() {
		TransactionTracker transactionTracker = getTransactionTracker();
		
		transactionTracker.trackBeginTransaction(new TransactionEvent("X","A",100));
		assertArrayEquals(new Integer[]{100}, transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent("X","A",100));
		assertArrayEquals(new Integer[]{100}, transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());

		transactionTracker.trackBeginTransaction(new TransactionEvent("Y","A",100));
		Assert.assertEquals(100, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals("A", transactionTracker.getCurrentTransactionOrigin());
		assertArrayEquals(new Integer[]{100}, transactionTracker.transactionsInProgress());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent("Y","A",100));
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
	}
	@Test
	public void testPartialOriginInitatedTransaction() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackBeginTransaction(new TransactionEvent("Y","B",100));
		Assert.assertEquals(100, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals("B", transactionTracker.getCurrentTransactionOrigin());
		assertArrayEquals(new Integer[]{100}, transactionTracker.transactionsInProgress());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent("Y","B",100));
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
	}
	@Test
	public void testIntermediateOriginInitatedTransactionY() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackBeginTransaction(new TransactionEvent("Y","Y",100));
		Assert.assertEquals(100, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals("Y", transactionTracker.getCurrentTransactionOrigin());
		assertArrayEquals(new Integer[]{100}, transactionTracker.transactionsInProgress());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent("Y","Y",100));
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
	}
	@Test
	public void testIntermediateOriginInitatedTransactionX() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackBeginTransaction(new TransactionEvent("X","X",100));
		Assert.assertEquals(100, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals("X", transactionTracker.getCurrentTransactionOrigin());
		assertArrayEquals(new Integer[]{100}, transactionTracker.transactionsInProgress());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent("X","X",100));
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
	}
	@Test
	public void testSelfInitatedTransaction() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackBeginTransaction(new TransactionEvent(CON_NAME,CON_NAME,100));
		Assert.assertEquals(100, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(CON_NAME, transactionTracker.getCurrentTransactionOrigin());
		assertArrayEquals(new Integer[]{100}, transactionTracker.transactionsInProgress());
		
		transactionTracker.trackCommitTransaction(new TransactionEvent(CON_NAME,CON_NAME,100));
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		Assert.assertEquals(0, transactionTracker.getCurrentTransactionID());
		Assert.assertEquals(null, transactionTracker.getCurrentTransactionOrigin());
	}
	@Test(expected=TransactionException.class)
	public void testCommitBeforeBeginTransaction() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackCommitTransaction(new TransactionEvent(CON_NAME,CON_NAME,100));
	}
	@Test(expected=TransactionException.class)
	public void testTwiceBeginTransaction() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackBeginTransaction(new TransactionEvent(CON_NAME,CON_NAME,100));
		transactionTracker.trackBeginTransaction(new TransactionEvent(CON_NAME,CON_NAME,100));
	}
	@Test(expected=TransactionException.class)
	public void testTwiceBeginTransactionFromTwoSources() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackBeginTransaction(new TransactionEvent("X","X",0));
		transactionTracker.trackBeginTransaction(new TransactionEvent("Y","Y",0));
	}
	@Test(expected=TransactionException.class)
	public void testBeginTransactionFromUnknownSource() {
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.trackBeginTransaction(new TransactionEvent("BAD","BAD",0));
	}
}
