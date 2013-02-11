package com.biswa.ep.subscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.subscription.SubscriptionRequest;

public class SubscriptionRequestTest {
	private static SubscriptionRequest subReq = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		subReq = new SubscriptionRequest("sink", 12345, new LeafAttribute("badAttr"));
		subReq.setAgent(new ConcreteContainer("container", new Properties()).agent());
	}

	@Test
	public void testGetId() {
		assertEquals(12345,subReq.getId());
	}

	@Test
	public void testGetAttribute() {
		assertEquals("badAttr",subReq.getAttribute().getName());
	}

	@Test
	public void testGetSink() {
		assertEquals("sink",subReq.getSink());
	}

	@Test
	public void testGetAgent() {
		assertNotNull(subReq.getAgent());
	}

}
