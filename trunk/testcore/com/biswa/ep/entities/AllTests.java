package com.biswa.ep.entities;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.biswa.ep.subscription.SubscriptionAttributeTest;

@RunWith(Suite.class)
@SuiteClasses({
		LeafAttributeTest.class,
		PrivateAttributeTest.class,
		StatelessAttributeTest.class,
		StaticAttributeTest.class})
public class AllTests {

}
