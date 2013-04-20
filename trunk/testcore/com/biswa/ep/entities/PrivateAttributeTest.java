package com.biswa.ep.entities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
class XPrivate extends PrivateAttribute{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4936361919515510979L;

	public XPrivate(String name) {
		super(name);
	}

	@Override
	protected Object evaluate(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		return null;
	}
	
}
public class PrivateAttributeTest{

	@Test
	public void testPropagate() {
		PrivateAttribute privateAttribute = new XPrivate("privateAttribute");
		assertFalse(privateAttribute.propagate());
	}

	@Test
	public void testDependsOn() {
		PrivateAttribute privateAttribute = new XPrivate("privateAttribute");
		assertArrayEquals(PrivateAttribute.ZERO_DEPENDENCY,privateAttribute.dependsOn());
	}

	@Test
	public void testRequiresStorage() {
		PrivateAttribute privateAttribute = new XPrivate("privateAttribute");
		assertTrue(privateAttribute.requiresStorage());
	}

	@Test
	public void testSetPropagate() {
		PrivateAttribute privateAttribute = new XPrivate("privateAttribute");
		privateAttribute.setPropagate(true);
	}

	@Test
	public void testIsSubscription() {
		PrivateAttribute privateAttribute = new XPrivate("privateAttribute");
		assertFalse(privateAttribute.isSubscription());
	}

	@Test
	public void testIsStateless() {
		PrivateAttribute privateAttribute = new XPrivate("privateAttribute");
		assertFalse(privateAttribute.isStateless());
	}

	@Test
	public void testIsStatic() {
		PrivateAttribute privateAttribute = new XPrivate("privateAttribute");
		assertFalse(privateAttribute.isStatic());
	}
}