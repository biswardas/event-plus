package com.biswa.ep.entities;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.biswa.ep.entities");
		//$JUnit-BEGIN$
		suite.addTestSuite(LeafAttributeTest.class);
		//$JUnit-END$
		return suite;
	}

}
