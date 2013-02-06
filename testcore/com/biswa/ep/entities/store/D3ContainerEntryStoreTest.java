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

public class D3ContainerEntryStoreTest extends TestCase{
	D3ContainerEntryStore concStore = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();	
		ConcreteContainer concreteContainer  = new ConcreteContainer("Temp", props);
		concStore = new D3ContainerEntryStore(concreteContainer);
		ContainerContext.CONTAINER.set(concreteContainer);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() {
		PhysicalEntry oneEntry = concStore.create(1);
		Assert.assertEquals(D3ContainerEntry.class, oneEntry.getClass());
	}

}
