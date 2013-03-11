package com.biswa.ep.entities.transaction;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import org.junit.Test;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.PropertyConstants;

public class TransactionTrackerTimeOutTest {

	private ConcreteContainer getTransactionTracker(String delay){
		Properties props = new Properties();
		props.put(PropertyConstants.TRAN_TIME_OUT, delay);
		ConcreteContainer cs = new ConcreteContainer("TestSchema",props);
		cs.agent().addSource(new ConnectionEvent(cs.getName(), cs.getName()));
		cs.agent().connected(new ConnectionEvent(cs.getName(), cs.getName()));
		cs.agent().waitForEventQueueToDrain();
		return cs; 
	}

	@Test
	public void testTimeout() throws Exception{
		final ConcreteContainer cs=getTransactionTracker("2000");
		Assert.assertEquals(2000, cs.getTimeOutPeriodInMillis());
		long startTime = System.currentTimeMillis();
		cs.agent().beginTran(new TransactionEvent("TestSchema", 100));
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		cs.agent().invokeOperation(new ContainerTask() {
			@Override
			protected void runtask() throws Throwable {
				s.release();
			}
		});
		s.acquireUninterruptibly();
		long endTime = System.currentTimeMillis();
		Assert.assertEquals(0, cs.agent().getCurrentTransactionID());
		Assert.assertTrue((endTime-startTime)>2000);
	}
	
	@Test
	public void testNoTimeout() throws Exception{
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		final ConcreteContainer cs=getTransactionTracker("0");
		Assert.assertEquals(0, cs.getTimeOutPeriodInMillis());
	}
}
