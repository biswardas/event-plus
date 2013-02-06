package com.biswa.ep.entities;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JoinContainerTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(JoinContainerTestSuite.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(InnerJoinContainerTest.class);
		suite.addTestSuite(OuterJoinContainerTest.class);
		suite.addTestSuite(RightOuterJoinContainerTest.class);
		suite.addTestSuite(LeftOuterJoinContainerTest.class);
		suite.addTestSuite(MultiMatchJoinContainerTest.class);	
		suite.addTestSuite(MultiMatchLeftJoinContainerTest.class);		
		//$JUnit-END$
		return suite;
	}

}
