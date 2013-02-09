package com.biswa.ep;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ClientTokenTest.class,
	com.biswa.ep.entities.AllTests.class,
	com.biswa.ep.subscription.AllTests.class})
public class AllTests {

}
