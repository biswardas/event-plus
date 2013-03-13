package com.biswa.ep.entities.transaction;

import java.util.Properties;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.SplitContainer;
import com.biswa.ep.entities.TimedContainer;
import com.biswa.ep.subscription.SubscriptionContainer;

public class SplitTrackerTest extends TransactionTrackerTest {

	protected AbstractContainer newContainer() {
		return new SplitContainer(CON, new Properties());
	}
}
