package com.biswa.ep.entities;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		ConcreteContainerTest.class,
		LeafAttributeTest.class,
		PrivateAttributeTest.class,
		StatelessAttributeTest.class,
		StaticAttributeTest.class,
		com.biswa.ep.entities.predicate.AllTests.class,
		com.biswa.ep.entities.store.AllTests.class,
		com.biswa.ep.entities.substance.AllTests.class,
		com.biswa.ep.entities.transaction.AllTests.class})
public class AllTests {

}
