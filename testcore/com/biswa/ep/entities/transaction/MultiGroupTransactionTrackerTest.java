package com.biswa.ep.entities.transaction;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.PropertyConstants;

public class MultiGroupTransactionTrackerTest {

	private TransactionTracker getTransactionTracker(){
		Properties props = new Properties();
		props.put(PropertyConstants.TRAN_TIME_OUT, "10000");
		ConcreteContainer cs = new ConcreteContainer("TestSchema",props){
			@Override
			public boolean isConnected() {
				return true;
			}	
		};
		TransactionAdapter transactionAdapter = cs.agent();
		transactionAdapter.transactionTracker.addSource("A",21);
		transactionAdapter.transactionTracker.addSource("B",10);
		transactionAdapter.transactionTracker.addSource("C",13);
		transactionAdapter.transactionTracker.addSource("D",24);
		transactionAdapter.transactionTracker.addSource("E",18);
		return transactionAdapter.transactionTracker;
	}
	@Test
	public void testMultiBeginTransactionAC() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
	}
	@Test
	public void testMultiBeginTransactionBCD() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(101, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "C");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "D");
		assertEquals(101, transactionTracker.getCurrentTransactionID());
	}
	@Test
	public void testMultiBeginTransactionADE() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(102, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(102, "D");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(102, "E");
		assertEquals(102, transactionTracker.getCurrentTransactionID());
	}
	@Test
	public void testMultiBeginTransactionBE() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(103, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(103, "E");
		assertEquals(103, transactionTracker.getCurrentTransactionID());
	}
	@Test
	public void testMultiBeginTransactionABD_NO() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(104, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(104, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(104, "D");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
	}
	@Test
	public void testMultiBeginTransaction() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(105, "AB");
		assertEquals(105, transactionTracker.getCurrentTransactionID());
	}
	@Test
	public void testMultiBeginTransactionCombined() {

		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(100,"A");
		transactionTracker.trackBeginTransaction(101, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "C");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "D");
		assertEquals(101, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(101,"B");
		transactionTracker.trackBeginTransaction(102, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(102, "D");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(102, "E");
		assertEquals(102, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(102,"A");
		transactionTracker.trackBeginTransaction(103, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(103, "E");
		assertEquals(103, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(103,"B");
		transactionTracker.trackBeginTransaction(104, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(104, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(104, "D");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(105, "AB");
		assertEquals(105, transactionTracker.getCurrentTransactionID());

	}
}
