package com.biswa.ep.entities;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConcurrencySupportTest {

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
	public void testGetConcurrencysupport() {
		assertEquals(1, ConcurrencySupport.INSERT.CONCURRENCY_SUPPORT);
		assertEquals(2, ConcurrencySupport.DELETE.CONCURRENCY_SUPPORT);
		assertEquals(4, ConcurrencySupport.UPDATE.CONCURRENCY_SUPPORT);
		assertEquals(7, ConcurrencySupport.INSERT.CONCURRENCY_SUPPORT|ConcurrencySupport.DELETE.CONCURRENCY_SUPPORT|ConcurrencySupport.UPDATE.CONCURRENCY_SUPPORT);
	}

}
