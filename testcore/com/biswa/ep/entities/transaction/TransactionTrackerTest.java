package com.biswa.ep.entities.transaction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.PropertyConstants;

public class TransactionTrackerTest {

	private TransactionTracker getTransactionTracker(){
		Properties props = new Properties();
		props.put(PropertyConstants.TRAN_TIME_OUT, "10000");
		ConcreteContainer cs = new ConcreteContainer("TestSchema",props){
			@Override
			public boolean isConnected() {
				return true;
			}
			@Override
			public boolean ensureExecutingInRightThread() {
				return true;
			}
		};
		TransactionAdapter transactionAdapter = cs.agent();
		return transactionAdapter.transactionTracker;
	}

	private TransactionTracker getNoTimeOutTransactionTracker(){
		ConcreteContainer cs = new ConcreteContainer("TestSchema",new Properties()){
			@Override
			public boolean isConnected() {
				return true;
			}
			@Override
			public boolean ensureExecutingInRightThread() {
				return true;
			}	
		};
		TransactionAdapter transactionAdapter = cs.agent();
		return transactionAdapter.transactionTracker;
	}
	private TransactionTracker getNotConnectedTransactionTracker(){
		Properties props = new Properties();
		props.put(PropertyConstants.TRAN_TIME_OUT, "10000");
		ConcreteContainer cs = new ConcreteContainer("TestSchema",props){
			@Override
			public boolean isConnected() {
				return false;
			}
			@Override
			public boolean ensureExecutingInRightThread() {
				return true;
			}	
		};
		TransactionAdapter transactionAdapter = cs.agent(); 
		return transactionAdapter.transactionTracker;
	}

	@Test
	public void testTransactionTracker() {
		TransactionTracker transactionTracker = getTransactionTracker();
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
	}
}
