package com.biswa.ep.entities.store;

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.entities.ConcreteContainer;

public class ContainerStoreFactoryTest    extends TestCase{

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
	public void testRegular() {
		Properties props = new Properties();
		props.put("passivation_idle_period","0"); 
		props.put("passivation_wakeup","true");
		
		ConcreteContainer concreteContainer  = new ConcreteContainer("Temp", props);
		ContainerEntryStore contStore = ContainerStoreFactory.getContainerEntryStore(concreteContainer);
		Assert.assertEquals(ConcreteContainerEntryStore.class, contStore.getClass());
	}

	@Test
	public void testPassiveRegular() {
		Properties props = new Properties();
		props.put("passivation_idle_period","10"); 
		props.put("passivation_wakeup","true");
		
		ConcreteContainer concreteContainer  = new ConcreteContainer("Temp", props);
		ContainerEntryStore contStore = ContainerStoreFactory.getContainerEntryStore(concreteContainer);
		Assert.assertEquals(PersistableContainerEntryStore.class, contStore.getClass());
	}

}
