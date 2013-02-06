package com.biswa.ep.discovery;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RMITransactionGeneratorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("pp.registryHost","192.168.1.102");
		System.setProperty("pp.registryPort","1099");
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetNextTransactionID() {
		Assert.assertNotNull(RegistryHelper.getRegistry());
		RMITransactionGenerator rmitranGen = new RMITransactionGenerator();
		int current = rmitranGen.getNextTransactionID();
		System.out.println(current);
		Assert.assertTrue(current+1<= rmitranGen.getNextTransactionID());
	}

}
