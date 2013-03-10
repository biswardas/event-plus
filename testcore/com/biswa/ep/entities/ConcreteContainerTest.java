package com.biswa.ep.entities;

import java.util.Properties;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.entities.transaction.TransactionEvent;

public class ConcreteContainerTest {
	private static final int TRANID = 9000000;
	static String SINK="SINK";
	static String SOURCEA="SOURCEA";
	static String SOURCEB="SOURCEB";
	static ConcreteContainer conc;
	static Semaphore semaphore = new Semaphore(1);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		semaphore.drainPermits();
		conc = new ConcreteContainer(SINK, new Properties());
	}

	@Test
	public void test() {
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
		Assert.assertEquals(null,conc.agent().getCurrentTransactionOrigin());
		
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
		Assert.assertEquals(null,conc.agent().getCurrentTransactionOrigin());
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
}
