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
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LeafAttribute;

public class WithUpdateTest    extends TestCase{
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
	public void testGetSubstance() {
		ContainerEntry conEntry = null;
		LeafAttribute ATTRIBUTEM1 = new LeafAttribute("m",1);
		LeafAttribute ATTRIBUTEM0 = new LeafAttribute("m",0);
		LeafAttribute ATTRIBUTEX = new LeafAttribute("x");
		ConcreteContainer conContainer = new ConcreteContainer("TempContainer", new Properties()); 
		ContainerContext.CONTAINER.set(conContainer);
		conContainer.attributeAdded(new ContainerStructureEvent("TempContainer", ATTRIBUTEX));
		conContainer.attributeAdded(new ContainerStructureEvent("TempContainer", ATTRIBUTEM0));
		conEntry = new ConcreteContainerEntry(0);
		Assert.assertNull(conEntry.getSubstance(ATTRIBUTEX)); 
		conEntry.silentUpdate(ATTRIBUTEX,0d);
		Assert.assertNotNull(conEntry.getSubstance(ATTRIBUTEX));
		conEntry.silentUpdate(ATTRIBUTEM0,0d);
		Assert.assertEquals(conEntry.getSubstance(ATTRIBUTEM0),0d);
		conEntry.remove(ATTRIBUTEX);
	}
}
