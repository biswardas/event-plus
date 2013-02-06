package com.biswa.ep.entities.substance;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiSubstanceTest {
	MultiSubstance multiSubstance = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		multiSubstance = new MultiSubstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsMultiValue() {
		Assert.assertTrue(multiSubstance.isMultiValue());
	}

	@Test
	public void testAddValue() {
		multiSubstance.addValue(1, new DecimalSubstance(1d));
		multiSubstance.addValue(2, new DecimalSubstance(2d));
	}

	@Test
	public void testRemoveValue() {
		multiSubstance.addValue(1, new DecimalSubstance(1d));
		multiSubstance.addValue(2, new DecimalSubstance(2d));
		Assert.assertEquals(2,multiSubstance.getValue().size());
	}
	
	@Test
	public void testRemoveSingleValue() {
		multiSubstance.addValue(1, new DecimalSubstance(1d));
		multiSubstance.addValue(2, new DecimalSubstance(2d));
		multiSubstance.removeValue(1);
		multiSubstance.removeValue(2);
		Assert.assertNotNull(multiSubstance.getValue());
	}
}
