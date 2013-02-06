package com.biswa.ep.entities.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConcreteTransactionGeneratorTest {

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
	public void testGetNextTransactionID() {
		TransactionGenerator concTrans = new DefaultTransactionGenerator();
		int transNum = concTrans.getNextTransactionID(); 
		assertTrue(transNum>0);
		assertEquals(transNum+1,concTrans.getNextTransactionID()); 
	}

}
