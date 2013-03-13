package com.biswa.ep.entities.transaction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		ConcreteTransactionGeneratorTest.class,
		TransactionTrackerTest.class,
		TimedTrackerTest.class,
		ThrottledTrackerTest.class,
		TransactionTrackerTimeOutTest.class,
		SubscriptionTrackerTest.class,
		SplitTrackerTest.class,
		PivotTrackerTest.class})
public class AllTests {

}
