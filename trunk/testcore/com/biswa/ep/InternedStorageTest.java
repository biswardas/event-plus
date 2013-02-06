package com.biswa.ep;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.entities.substance.DecimalSubstance;

public class InternedStorageTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
	private InternedStorage myhashMap = new InternedStorage();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testMem(){
		while(true){
			DecimalSubstance k1 = new DecimalSubstance(Math.random());
			DecimalSubstance v1 = new DecimalSubstance(Math.random());
			myhashMap .put(k1,v1);
		}
	}
}
