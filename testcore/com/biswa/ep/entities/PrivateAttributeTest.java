package com.biswa.ep.entities;

import static org.junit.Assert.*;

import org.junit.Test;

public class PrivateAttributeTest{

	@Test
	public void testPropagate() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		assertFalse(privateAttribute.propagate());
	}

	@Test
	public void testDependsOn() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		assertArrayEquals(PrivateAttribute.ZERO_DEPENDENCY,privateAttribute.dependsOn());
	}

	@Test
	public void testRequiresStorage() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		assertTrue(privateAttribute.requiresStorage());
	}

	@Test
	public void testShouldInitializeOnInsert() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		assertTrue(privateAttribute.initializeOnInsert());
	}

	@Test
	public void testSetPropagate() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		privateAttribute.setPropagate(true);
	}

	@Test
	public void testIsSubscription() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		assertFalse(privateAttribute.isSubscription());
	}

	@Test
	public void testIsStateless() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		assertFalse(privateAttribute.isStateless());
	}

	@Test
	public void testIsStatic() {
		PrivateAttribute privateAttribute = new PrivateAttribute("privateAttribute");
		assertFalse(privateAttribute.isStatic());
	}
}
