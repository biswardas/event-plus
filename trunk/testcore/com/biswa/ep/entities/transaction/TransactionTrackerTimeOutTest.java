package com.biswa.ep.entities.transaction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.PropertyConstants;

public class TransactionTrackerTimeOutTest {

	private TransactionTracker getTransactionTracker(String delay){
		Properties props = new Properties();
		props.put(PropertyConstants.TRAN_TIME_OUT, delay);
		ConcreteContainer cs = new ConcreteContainer("TestSchema",props){
			@Override
			public boolean isConnected() {
				return true;
			}	
		};
		TransactionAdapter transactionAdapter = cs.agent(); 
		return transactionAdapter.transactionTracker;
	}

	@Test
	public void testTimeout() throws Exception{
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		final TransactionTracker transactionTracker=getTransactionTracker("5000");
		final ArrayList<String> al = new ArrayList<String>();
		transactionTracker.transactionAdapter.getEventCollector().execute(new Runnable(){
			@Override
			public void run() {
//				transactionTracker.addSource("A",2);
//				transactionTracker.addSource("X",1);
//				transactionTracker.trackBeginTransaction(100, "A");
//				assertEquals(100, transactionTracker.getCurrentTransactionID());
//				transactionTracker.trackBeginTransaction(101, "X");
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
				transactionTracker.addOperation(101, new ContainerTask(){
					@Override
					public void runtask() {
						al.clear();
						s.release();
					}
				});
				//transactionTracker.trackCommitTransaction(101, "X");
				try {
					Thread.sleep(transactionTracker.transactionAdapter.getTimeOutPeriodInMillis());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}		
			}			
		});
		s.acquire();
		assertTrue(al.isEmpty());
	}


	@Test
	public void testNoTimeout() throws Exception{
		final TransactionTracker transactionTracker=getTransactionTracker("15000");
		final ArrayList<String> al = new ArrayList<String>();
		transactionTracker.transactionAdapter.getEventCollector().execute(new Runnable(){
			@Override
			public void run() {
//				transactionTracker.addSource("A",2);
//				transactionTracker.addSource("X",1);
//				transactionTracker.trackBeginTransaction(100, "A");
//				transactionTracker.trackBeginTransaction(101, "X");
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
				//transactionTracker.trackCommitTransaction(101, "X");
				transactionTracker.addOperation(101, new ContainerTask(){
					@Override
					public void runtask() {
						al.clear();						
					}
				});

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}		
			}
		});
		Thread.sleep(12000);
		assertFalse(al.isEmpty());
		Integer[] currentlyinProgress = {100,101};
		assertArrayEquals(currentlyinProgress, transactionTracker.transactionsInProgress());
	}
}
