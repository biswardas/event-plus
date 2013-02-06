package com.biswa.ep;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientTokenTest {

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
	public void testGetToken() {
		ClientToken ct = new ClientToken();
		assertEquals(ct.getToken(), 1);
		assertEquals(ct.getToken(), 2);
		assertEquals(ct.getToken(), 4);
		assertEquals(ct.getToken(), 8);
		ct.releaseToken(8);
		assertEquals(ct.getToken(), 8);
		assertEquals(ct.getToken(), 16);
		assertEquals(ct.getToken(), 32);
		ct.releaseToken(4);
		assertEquals(ct.getToken(), 4);
		ct.releaseToken(2);
		assertEquals(ct.getToken(), 2);
		ct.releaseToken(1);
		assertEquals(ct.getToken(), 1);
		assertEquals(ct.getToken(), 64);
	}

	@Test
	public void testReleaseToken() {
		ClientToken ct = new ClientToken();
		int i=0;
		int expected=1;
		while((i=ct.getToken())!=0){
			assertEquals(expected, i);
			expected=expected*2;
		}
	}

}
