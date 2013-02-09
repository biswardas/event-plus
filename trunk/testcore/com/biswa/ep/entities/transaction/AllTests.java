package com.biswa.ep.entities.transaction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		ConcreteTransactionGeneratorTest.class,
		MultiGroupTransactionTrackerTest.class,
		TransactionTrackerTest.class,
		TransactionTrackerTimeOutTest.class})
public class AllTests {

}
