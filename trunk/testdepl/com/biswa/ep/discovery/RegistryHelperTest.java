package com.biswa.ep.discovery;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.entities.ConcreteContainer;

public class RegistryHelperTest {

	@BeforeClass
	public static void registerContainer(){
		final Accepter accepter = new RMIAccepterImpl(new ContainerManager());
		accepter.publish(new ConcreteContainer("foo",new Properties()));
	}
	@Test
	public void testGetRMIListener() {
		assertTrue(RegistryHelper.getRMIListener("foo") instanceof RMIListener);
	}

	@Test
	public void testGetConnecter() {
		assertTrue(RegistryHelper.getConnecter("foo") instanceof Connector);
	}

	@Test
	public void testGetEntryReader() {
		assertTrue(RegistryHelper.getEntryReader("foo") instanceof EntryReader);
	}

	@Test
	public void testGetRegistry() {
		Assert.assertNotNull(RegistryHelper.getRegistry());
	}

	@Test
	public void testGetBinder() {
		Assert.assertNotNull(RegistryHelper.getBinder());
	}
	@AfterClass
	public static void cleanUp() throws Exception {
		RegistryHelper.getRegistry().unbind("foo");
	}
}
