package com.biswa.ep.entities.store;

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.DecimalSubstance;
import com.biswa.ep.entities.substance.MultiSubstance;

public class D3ContainerEntryTest    extends TestCase{
	
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
	public void testSilentUpdateRemove() {

		D3ContainerEntry d3ContainerEntry = null;
		MultiSubstance multiSubstance = null;
		Attribute x = new LeafAttribute("x");
		Attribute x1 = new LeafAttribute("x",1);
		Attribute x2 = new LeafAttribute("x",2);
		ConcreteContainer conContainer = new ConcreteContainer("TempContainer", new Properties()); 
		ContainerContext.CONTAINER.set(conContainer);
		conContainer.attributeAdded(new ContainerStructureEvent("TempContainer", x));
		
		d3ContainerEntry = new D3ContainerEntry(1);
		multiSubstance = new MultiSubstance();
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(0d));
		Assert.assertNotNull(d3ContainerEntry.getSubstance(x));
		d3ContainerEntry.remove(x);
		d3ContainerEntry.remove(x);
		d3ContainerEntry.remove(x,1);
		d3ContainerEntry.remove(x,2);
		Assert.assertNull(d3ContainerEntry.getSubstance(x));
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(1d),1);
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(1d),1);
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(1d),1);
		Assert.assertNotNull(d3ContainerEntry.getSubstance(x));
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(2d),2);
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(2d),2);
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(2d),2);
		Assert.assertNotNull(d3ContainerEntry.getSubstance(x));
		d3ContainerEntry.remove(x,2);
		d3ContainerEntry.remove(x,1);
		Assert.assertNull(d3ContainerEntry.getSubstance(x));
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(1d),1);
		d3ContainerEntry.silentUpdate(x, new DecimalSubstance(2d),2);
		Assert.assertNotNull(d3ContainerEntry.getSubstance(x));
	}

}
