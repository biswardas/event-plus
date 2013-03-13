package com.biswa.ep.entities.transaction;

import java.util.Properties;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.FeedbackAwareContainer;
import com.biswa.ep.entities.PivotContainer;

public class PivotTrackerTest extends TransactionTrackerTest {

	protected AbstractContainer newContainer() {
		return new PivotContainer(CON, new Properties());
	}
}
