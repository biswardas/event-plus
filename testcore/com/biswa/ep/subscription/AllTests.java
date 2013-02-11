package com.biswa.ep.subscription;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.biswa.ep.subscription.SubscriptionAttributeTest;

@RunWith(Suite.class)
@SuiteClasses({
		SubscriptionAttributeTest.class,
		SubscriptionContainerTest.class})
public class AllTests {

}
