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

	@Test
	public void testTrackBeginTransaction() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {100};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}


	@Test
	public void testMultiBeginTransaction() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		Integer[] currentlyinProgress = {100};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
		transactionTracker.trackCommitTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress1 = {};
		assertArrayEquals(currentlyinProgress1, transactionTracker.transactionsInProgress());
	}
	
	@Test
	public void testScenario() {
		TransactionTracker transactionTracker=getNoTimeOutTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.trackBeginTransaction(100, "A");
		transactionTracker.trackCommitTransaction(100, "A");
		transactionTracker.trackBeginTransaction(100, "B");
		transactionTracker.trackBeginTransaction(100, "C");
		transactionTracker.trackCommitTransaction(100, "B");
		transactionTracker.trackCommitTransaction(100, "C");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
	}
	
	@Test
	public void testMultiCommitTransaction() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		Integer[] currentlyinProgress = {100};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
		transactionTracker.trackCommitTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "A");
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress1 = {};
		assertArrayEquals(currentlyinProgress1, transactionTracker.transactionsInProgress());
	}
	
	@Test
	public void testTrackBeginTransaction1() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.addSource("X",1);
		transactionTracker.addSource("Y",1);
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		//Transaction 101 has begun before 100 has committed
		
		transactionTracker.trackBeginTransaction(101, "X");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "Y");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {100,101};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}
	
	@Test
	public void testTrackCommitTransaction() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.addSource("X",1);
		transactionTracker.addSource("Y",1);
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "X");
		transactionTracker.trackBeginTransaction(101, "Y");
		transactionTracker.trackCommitTransaction(100, "A");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "B");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "C");
		assertEquals(101, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(101, "X");
		assertEquals(101, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(101, "Y");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}

	@Test
	public void testTrackCommitTransactionNotConnected() {
		TransactionTracker transactionTracker=getNotConnectedTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.addSource("X",1);
		transactionTracker.addSource("Y",1);
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "X");
		transactionTracker.trackBeginTransaction(101, "Y");
		transactionTracker.trackCommitTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "C");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(101, "X");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(101, "Y");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {100,101};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}

	@Test
	public void testTrackRollbackTransaction() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.addSource("X",1);
		transactionTracker.addSource("Y",1);
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "X");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "Y");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(100, "A");
		assertEquals(101, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(100, "B");
		transactionTracker.trackRollbackTransaction(100, "C");
		transactionTracker.trackRollbackTransaction(101, "X");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(101, "Y");
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}

	@Test
	public void testTrackRollbackTransactionNotYetStarted() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.addSource("X",1);
		transactionTracker.addSource("Y",1);
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		
		//Begining transaction 101
		transactionTracker.trackBeginTransaction(101, "X");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "Y");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		
		//Transaction 100 in commit
		transactionTracker.trackCommitTransaction(100, "A");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "B");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		
		//Rolling back transaztion 101
		transactionTracker.trackRollbackTransaction(101, "X");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackRollbackTransaction(101, "Y");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		
		
		//Transaction 100 completing commit.
		transactionTracker.trackCommitTransaction(100, "C");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}

	@Test
	public void testCompleteTransaction() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.setCurrentTransactionID(100);
		transactionTracker.completeTransaction();
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}

	@Test
	public void testIsIdle() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.setCurrentTransactionID(100);
		transactionTracker.completeTransaction();
		assertTrue(transactionTracker.isIdle());
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}

	@Test
	public void testAddSource() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.addSource("X",1);
		transactionTracker.addSource("Y",1);
		assertNull(transactionTracker.transactionAdapter.getNext());
		Integer[] currentlyinProgress = {};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}
	@Test
	public void testAddOperationWithZeroTransactionID() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addOperation(0, new ContainerTask(){
			@Override
			public void runtask() {
				// TODO Auto-generated method stub
				
			}
		});
		transactionTracker.trackBeginTransaction(100, "A");
		transactionTracker.addOperation(0, new ContainerTask(){
			@Override
			public void runtask() {
				// TODO Auto-generated method stub
				
			}
		});
		transactionTracker.trackCommitTransaction(100, "A");
		assertNull(transactionTracker.getNext());
	}

	@Test
	public void testAddOperation() {
		TransactionTracker transactionTracker=getTransactionTracker();
		transactionTracker.addSource("A",2);
		transactionTracker.addSource("B",2);
		transactionTracker.addSource("C",2);
		transactionTracker.addSource("X",1);
		transactionTracker.addSource("Y",1);
		transactionTracker.trackBeginTransaction(100, "A");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "B");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(100, "C");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackBeginTransaction(101, "X");
		transactionTracker.trackBeginTransaction(101, "Y");
		final ArrayList<String> al = new ArrayList<String>();
		transactionTracker.addOperation(100, new ContainerTask(){
			@Override
			public void runtask() {
				al.add("a");
			}
		});
		assertTrue(al.contains("a"));

		transactionTracker.addOperation(101, new ContainerTask(){
			@Override
			public void runtask() {
				al.add("b");
				
			}
		});
		assertFalse(al.contains("b"));
		transactionTracker.trackCommitTransaction(100, "A");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "B");
		assertEquals(100, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(100, "C");
		assertTrue(al.contains("b"));
		assertEquals(101, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(101, "X");
		transactionTracker.addOperation(101, new ContainerTask(){
			@Override
			public void runtask() {
				al.add("c");
				
			}
		});
		assertTrue(al.contains("c"));
		assertEquals(101, transactionTracker.getCurrentTransactionID());
		transactionTracker.trackCommitTransaction(101, "Y");
		assertEquals(0, transactionTracker.getCurrentTransactionID());
		assertNull(transactionTracker.transactionAdapter.getNext());
		transactionTracker.addOperation(101, new ContainerTask(){
			@Override
			public void runtask() {
				al.clear();				
			}
		});
		assertFalse(al.isEmpty());
	}
	@Test
	public void testTransactionTrackerUglyCases() {
		final AtomicInteger testInteger = new AtomicInteger();
		ContainerTask transactionAwareOperation = new ContainerTask(){
			@Override
			public void runtask() {
				System.out.println("May your soul rest in peace");	
				testInteger.set(100);
			}
		};
		TransactionTracker transactionTracker = getTransactionTracker();
		transactionTracker.addSource(null, Integer.MIN_VALUE);
		transactionTracker.trackBeginTransaction(Integer.MIN_VALUE, null);
		transactionTracker.addOperation(Integer.MIN_VALUE, transactionAwareOperation);
		transactionTracker.trackCommitTransaction(Integer.MIN_VALUE, null);
		transactionTracker.trackBeginTransaction(Integer.MAX_VALUE, null);
		transactionTracker.trackRollbackTransaction(Integer.MAX_VALUE, null);
		assertArrayEquals(new Integer[0], transactionTracker.transactionsInProgress());
		assertEquals(100, testInteger.get());
	}
}
