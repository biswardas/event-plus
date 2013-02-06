package com.biswa.ep.entities.predicate;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AndTest.class, EqualsTest.class, GtTest.class, InTest.class,
		LtTest.class, NotEqualsTest.class, OrTest.class })
public class AllTests {

}
