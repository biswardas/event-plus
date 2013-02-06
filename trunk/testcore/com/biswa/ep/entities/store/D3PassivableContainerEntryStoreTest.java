package com.biswa.ep.entities.store;

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.ConcreteContainer;

public class D3PassivableContainerEntryStoreTest extends TestCase{

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
	public void testCreateEager() {
		Properties props = new Properties();	
		ConcreteContainer concreteContainer  = new ConcreteContainer("Temp", props);
		PassivableContainerEntryStore concStore = new D3PassivableContainerEntryStore(concreteContainer,10,true);
		ContainerContext.CONTAINER.set(concreteContainer);
		PhysicalEntry oneEntry = concStore.create(1);
		Assert.assertEquals(EagerContainerEntry.class, oneEntry.getClass());
	}

	@Test
	public void testCreateLazy() {
		Properties props = new Properties();	
		ConcreteContainer concreteContainer  = new ConcreteContainer("Temp", props);
		PassivableContainerEntryStore concStore = new D3PassivableContainerEntryStore(concreteContainer,10,false);
		ContainerContext.CONTAINER.set(concreteContainer);
		PhysicalEntry oneEntry = concStore.create(1);
		Assert.assertEquals(LazyContainerEntry.class, oneEntry.getClass());
	}
}
