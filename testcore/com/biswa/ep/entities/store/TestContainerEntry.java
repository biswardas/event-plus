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
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.TransportEntry;

public class TestContainerEntry    extends TestCase{
	ContainerEntry conEntry = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ContainerContext.CONTAINER.set(new ConcreteContainer("TempContainer", new Properties()));
		conEntry = new ConcreteContainerEntry(0);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHashCode() {
		Assert.assertTrue(conEntry.hashCode()>0);
	}

	@Test
	public void testGetContainer() {
		Assert.assertNotNull(conEntry.getContainer());
		ContainerContext.CONTAINER.set(null);
		Assert.assertNull(conEntry.getContainer());
	}	

	@Test
	public void testIsFiltered() {
		conEntry.setFiltered(1,true);
		conEntry.setFiltered(2,true);
		Assert.assertTrue(conEntry.isFiltered(1));
		Assert.assertTrue(conEntry.isFiltered(2));
	}
	
	@Test
	public void testIsLeftTrueRightFalse() {
		conEntry.setFiltered(1,true);
		conEntry.setFiltered(2,true);
		Assert.assertTrue(conEntry.isFiltered(1));
		Assert.assertTrue(conEntry.isFiltered(2));
		conEntry.setLeftTrueRightFalse(false);
		Assert.assertFalse(conEntry.isLeftTrueRightFalse());
		conEntry.setLeftTrueRightFalse(true);
		Assert.assertTrue(conEntry.isLeftTrueRightFalse());
		conEntry.setLeftTrueRightFalse(false);
		Assert.assertFalse(conEntry.isLeftTrueRightFalse());
		Assert.assertTrue(conEntry.isFiltered(1));
		Assert.assertTrue(conEntry.isFiltered(2));
	}

	@Test
	public void testGetIdentitySequence() {
		Assert.assertEquals(conEntry.getIdentitySequence(), 0);
	}

	@Test
	public void testToString() {
		Assert.assertNotNull(conEntry.toString());
	}

	@Test
	public void testEqualsObject() {
		TransportEntry conEntry = new TransportEntry(0);
		Assert.assertNotSame(conEntry, this.conEntry);
	}

	@Test
	public void testCloneConcrete() {
		Assert.assertNotNull(conEntry.cloneConcrete());
	}
	
	@Test
	public void testReset() {
		Assert.assertFalse(conEntry.markedAdded());
		Assert.assertFalse(conEntry.markedRemoved());
		Assert.assertFalse(conEntry.markedDirty());
		conEntry.markAdded(true);
		Assert.assertEquals(conEntry.touchMode(), ContainerEntry.MARKED_ADDED);
		Assert.assertTrue(conEntry.markedAdded());
		Assert.assertFalse(conEntry.markedRemoved());
		Assert.assertFalse(conEntry.markedDirty());
		conEntry.markAdded(false);
		Assert.assertFalse(conEntry.markedAdded());
		Assert.assertFalse(conEntry.markedRemoved());
		Assert.assertFalse(conEntry.markedDirty());

		conEntry.markAdded(true);
		conEntry.markRemoved(true);
		Assert.assertFalse(conEntry.markedAdded());
		Assert.assertEquals(conEntry.touchMode(), ContainerEntry.MARKED_REMOVED);
		conEntry.reset();		
		conEntry.markDirty(true);
		Assert.assertEquals(conEntry.touchMode(), ContainerEntry.MARKED_DIRTY);
		Assert.assertNotSame(conEntry.touchMode(), ContainerEntry.MARKED_REMOVED);
		Assert.assertNotSame(conEntry.touchMode(), ContainerEntry.MARKED_ADDED);
		
		conEntry.reset();		
		conEntry.markAdded(true);
		conEntry.markDirty(true);
		Assert.assertFalse(conEntry.markedDirty());
		
		conEntry.reset();
		conEntry.markRemoved(true);
		conEntry.markDirty(true);
		Assert.assertFalse(conEntry.markedDirty());
		
		conEntry.reset();
		conEntry.markDirty(true);
		conEntry.markRemoved(true);
		Assert.assertFalse(conEntry.markedDirty());
	}

}
