package com.biswa.ep.entities;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.entities.JoinContainer.JoinPolicy;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.Agent;

public class MultiMatchJoinContainerTest   extends TestCase{
	private volatile int rowCount=0;
	private static final String LISTENING_CONTAINER = "ListeningContainer";
	private static final String currentContainer = "JoinContainer";
	Semaphore semaphore = new Semaphore(1);
	private String leftContainerName="Left";
	private String rightContainerName="Right";
	private JoinPolicy joinPolicy = JoinPolicy.INNER_JOIN;
	private ConcreteContainer listeningContainer = new ConcreteContainer(LISTENING_CONTAINER, new Properties());
	Agent agent = new Agent(listeningContainer){

		@Override
		public void entryAdded(ContainerEvent ce) {
			System.out.println("Added:"+ce);
			rowCount++;
			semaphore.release();
		}
		
		@Override
		public void connected(final ConnectionEvent ce) {
			System.out.println("Received ConnectionEvent:"+ce);
		}

		@Override
		public void entryRemoved(ContainerEvent ce) {
			System.out.println("Removed:"+ce);
			rowCount--;
			semaphore.release();
		}

		@Override
		public void entryUpdated(ContainerEvent ce) {
			System.out.println("Updated:"+ce);
		}		
	};
	Comparator<ContainerEntry> predicate = new Comparator<ContainerEntry>() {
		@Override
		public int compare(ContainerEntry o1, ContainerEntry o2) {
			return o1.getIdentitySequence()/o2.getIdentitySequence()>=2?0:1;
		}
	};
	Properties props = new Properties();
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test() throws Exception{
		JoinContainer joinContainer = getJoinContainer();
		semaphore.drainPermits();
		
		addLeftEntry(getLeftEntry(5), joinContainer);
		Assert.assertEquals(0, rowCount);
		addLeftEntry(getLeftEntry(6), joinContainer);
		Assert.assertEquals(0, rowCount);
		
		addRightEntry(getRightEntry(1), joinContainer);	
		semaphore.acquire();	
		semaphore.acquire();
		Assert.assertEquals(2, rowCount);
		addRightEntry(getRightEntry(2), joinContainer);	
		semaphore.acquire();	
		semaphore.acquire();
		Assert.assertEquals(4, rowCount);		
		
		removeRightEntry(getRightEntry(1), joinContainer);
		semaphore.acquire();
		semaphore.acquire();
		Assert.assertEquals(2, rowCount);
		removeRightEntry(getRightEntry(2), joinContainer);
		semaphore.acquire();
		semaphore.acquire();		
		Assert.assertEquals(0, rowCount);
		
		removeLeftEntry(getLeftEntry(5), joinContainer);
		Assert.assertEquals(0, rowCount);
		removeLeftEntry(getLeftEntry(6), joinContainer);
		Assert.assertEquals(0, rowCount);
	}
	
	protected JoinContainer getJoinContainer() {
		JoinContainer joinContainer = new JoinContainer(currentContainer,joinPolicy,predicate,leftContainerName,rightContainerName,props);
		joinContainer.agent().addSource(new ConnectionEvent(leftContainerName, currentContainer));
		joinContainer.agent().addSource(new ConnectionEvent(rightContainerName, currentContainer));
		joinContainer.agent().connected(new ConnectionEvent(leftContainerName, currentContainer));
		joinContainer.agent().connected(new ConnectionEvent(rightContainerName, currentContainer));
		joinContainer.agent().connect(new ConnectionEvent(currentContainer, LISTENING_CONTAINER, agent));
		return joinContainer;
	}
	
	private void addLeftEntry(TransportEntry ce, ConcreteContainer cs) {
		cs.agent().entryAdded(
				new ContainerInsertEvent(leftContainerName, ce,0));
	}
	private void addRightEntry(TransportEntry ce, ConcreteContainer cs) {
		cs.agent().entryAdded(
				new ContainerInsertEvent(rightContainerName, ce,0));
	}

	private void removeLeftEntry(TransportEntry ce, ConcreteContainer cs) {
		cs.agent().entryRemoved(
				new ContainerDeleteEvent(leftContainerName, ce.getIdentitySequence(),0));
	}

	private void removeRightEntry(TransportEntry ce, ConcreteContainer cs) {
		cs.agent().entryRemoved(
				new ContainerDeleteEvent(rightContainerName, ce.getIdentitySequence(),0));
	}

	protected TransportEntry getLeftEntry(int i) {
		Map<Attribute, Substance> map2 = new HashMap<Attribute, Substance>();
		TransportEntry ce2 = new TransportEntry(i, map2);
		return ce2;
	}

	protected TransportEntry getRightEntry(int i) {
		Map<Attribute, Substance> map = new HashMap<Attribute, Substance>();
		TransportEntry ce = new TransportEntry(i, map);
		return ce;
	}
}
