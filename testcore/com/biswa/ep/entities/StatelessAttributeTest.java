package com.biswa.ep.entities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StatelessAttributeTest{
	class StatelessAttributeX extends StatelessAttribute{
		protected StatelessAttributeX(String name) {
			super(name);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -5354521798137893539L;

		@Override
		protected Object evaluate(Attribute attribute,
				ContainerEntry containerEntry) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}		
	}
	@Test
	public void testPropagate() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		assertFalse(statelessAttribute.propagate());
	}

	@Test
	public void testDependsOn() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		assertArrayEquals(StatelessAttribute.ZERO_DEPENDENCY,statelessAttribute.dependsOn());
	}

	@Test
	public void testRequiresStorage() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		assertFalse(statelessAttribute.requiresStorage());
	}

	public void testSetPropagate() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		statelessAttribute.setPropagate(true);
	}

	@Test
	public void testIsSubscription() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		assertFalse(statelessAttribute.isSubscription());
	}

	@Test
	public void testIsStateless() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		assertTrue(statelessAttribute.isStateless());
	}

	@Test
	public void testIsStatic() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		assertFalse(statelessAttribute.isStatic());
	}
	@Test(expected=UnsupportedOperationException.class)
	public void testGetOrdinal() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		statelessAttribute.getOrdinal();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testSetOrdinal() {
		StatelessAttribute statelessAttribute = new StatelessAttributeX("statelessAttribute");
		statelessAttribute.setOrdinal(5);
	}
}
