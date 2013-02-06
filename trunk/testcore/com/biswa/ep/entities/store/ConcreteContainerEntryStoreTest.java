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

public class ConcreteContainerEntryStoreTest extends TestCase {
	ConcreteContainerEntryStore concStore = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();	
		ConcreteContainer concreteContainer  = new ConcreteContainer("Temp", props);
		concStore = new ConcreteContainerEntryStore(concreteContainer);
		ContainerContext.CONTAINER.set(concreteContainer);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() {
		PhysicalEntry oneEntry = concStore.create(1);
		Assert.assertEquals(ConcreteContainerEntry.class, oneEntry.getClass());
	}

	@Test
	public void testRemove() {
		PhysicalEntry oneEntry = concStore.create(1);
		concStore.save(oneEntry);
		concStore.remove(1);
		Assert.assertNull(concStore.getEntry(1));		
	}

	@Test
	public void testClear() {
		PhysicalEntry oneEntry = concStore.create(1);
		concStore.save(oneEntry);
		oneEntry = concStore.create(2);
		concStore.save(oneEntry);
		concStore.clear();
		Assert.assertEquals(0,concStore.getEntries().length);
	}

	@Test
	public void testSave() {
		PhysicalEntry oneEntry = concStore.create(1);
		concStore.save(oneEntry);
		Assert.assertNotNull(concStore.getEntry(1));
	}

	@Test
	public void testGetEntries() {
		PhysicalEntry oneEntry = concStore.create(1);
		concStore.save(oneEntry);
		oneEntry = concStore.create(2);
		concStore.save(oneEntry);
		Assert.assertEquals(2,concStore.getEntries().length);
	}

	@Test
	public void testGetEntry() {
		PhysicalEntry oneEntry = concStore.create(1);
		concStore.save(oneEntry);
		Assert.assertNotNull(concStore.getEntry(1));
	}

}
