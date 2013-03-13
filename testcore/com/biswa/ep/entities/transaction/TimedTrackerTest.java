package com.biswa.ep.entities.transaction;

import java.util.Properties;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.FeedbackAwareContainer;

public class TimedTrackerTest extends TransactionTrackerTest {

	protected AbstractContainer newContainer() {
		return new FeedbackAwareContainer(CON, new Properties());
	}
}
