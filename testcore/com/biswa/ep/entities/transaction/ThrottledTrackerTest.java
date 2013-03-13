package com.biswa.ep.entities.transaction;

import java.util.Properties;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.TimedContainer;

public class ThrottledTrackerTest extends TransactionTrackerTest {

	protected AbstractContainer newContainer() {
		return new TimedContainer(CON, new Properties());
	}
}
