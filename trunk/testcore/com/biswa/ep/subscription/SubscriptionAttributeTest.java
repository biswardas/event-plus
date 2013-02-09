package com.biswa.ep.subscription;

import static org.junit.Assert.*;

import org.junit.Test;

import com.biswa.ep.subscription.SubscriptionAttribute;

public class SubscriptionAttributeTest{

	@Test
	public void testPropagate() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		assertFalse(subscriptionAttribute.propagate());
	}

	@Test
	public void testDependsOn() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		assertEquals(1,subscriptionAttribute.dependsOn().length);
	}

	@Test
	public void testRequiresStorage() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		assertTrue(subscriptionAttribute.requiresStorage());
	}

	@Test
	public void testShouldInitializeOnInsert() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		assertTrue(subscriptionAttribute.initializeOnInsert());
	}

	@Test(expected=RuntimeException.class)
	public void testSetPropagate() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		subscriptionAttribute.setPropagate(true);
	}

	@Test
	public void testIsSubscription() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		assertTrue(subscriptionAttribute.isSubscription());
	}

	@Test
	public void testIsStateless() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		assertFalse(subscriptionAttribute.isStateless());
	}

	@Test
	public void testIsStatic() {
		SubscriptionAttribute subscriptionAttribute = new SubscriptionAttribute("subscriptionAttribute",null,null,null,null);
		assertFalse(subscriptionAttribute.isStatic());
	}
}
