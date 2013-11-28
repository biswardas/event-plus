package com.biswa.ep.entities.store;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(ContainerStoreFactoryTest.class);
		suite.addTestSuite(TestContainerEntry.class);
		suite.addTestSuite(ConcreteContainerEntryStoreTest.class);	
		suite.addTestSuite(PersistableContainerEntryStoreTest.class);		
		suite.addTestSuite(WithUpdateTest.class);
		//$JUnit-END$
		return suite;
	}

}
