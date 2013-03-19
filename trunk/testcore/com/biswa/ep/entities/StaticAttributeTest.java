package com.biswa.ep.entities;

import static org.junit.Assert.*;

import org.junit.Test;

import com.biswa.ep.entities.substance.Substance;

public class StaticAttributeTest{
	class StaticAttributeX extends StaticAttribute{
		protected StaticAttributeX(String name) {
			super(name);
		}
		@Override
		protected Substance evaluate(Attribute attribute) throws Exception {
			return null;
		}
	}
	@Test
	public void testPropagate() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		assertFalse(staticAttribute.propagate());
	}

	@Test
	public void testDependsOn() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		assertArrayEquals(StaticAttribute.ZERO_DEPENDENCY,staticAttribute.dependsOn());
	}

	@Test
	public void testRequiresStorage() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		assertFalse(staticAttribute.requiresStorage());
	}

	@Test
	public void testSetPropagate() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		staticAttribute.setPropagate(true);
	}

	@Test
	public void testIsSubscription() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		assertFalse(staticAttribute.isSubscription());
	}

	@Test
	public void testIsStateless() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		assertFalse(staticAttribute.isStateless());
	}

	@Test
	public void testIsStatic() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		assertTrue(staticAttribute.isStatic());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetOrdinal() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		staticAttribute.getOrdinal();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSetOrdinal() {
		StaticAttribute staticAttribute = new StaticAttributeX("staticAttribute");
		staticAttribute.setOrdinal(5);
	}
}
