package com.biswa.ep.entities.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public class LtTest {

	@Test
	public void testVisit1() {
		Lt equals1 = new Lt(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Aiswa"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(equals1.visit(containerEntry));
	}

	@Test
	public void testVisit2() {
		Lt equals1 = new Lt(new LeafAttribute("Test"),
				new ObjectSubstance("Aiswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(equals1.visit(containerEntry));
	}

	@Test
	public void tesVisit3() {

		Lt equals1 = new Lt(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		try {
			assertFalse(equals1.visit(containerEntry));
			fail("Should throw null pointer exception");
		} catch (NullPointerException npe) {

		}
	}

	@Test
	public void tesVisit4() {

		Lt equals1 = new Lt(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));

		try {
			assertFalse(equals1.visit(null));
			fail("Should throw null pointer exception");
		} catch (NullPointerException npe) {

		}
	}

	@Test
	public void testVisit5() {
		Lt equals2 = new Lt(new LeafAttribute("Test"),
				new ObjectSubstance(null));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);

		try {
			assertFalse(equals2.visit(containerEntry));
			fail("Should throw null pointer exception");
		} catch (NullPointerException npe) {
		}

	}

	@Test
	public void testVisit6() {
		Lt equals2 = new Lt(new LeafAttribute("Test"),
				new ObjectSubstance(null));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);

		Lt equals3 = new Lt(new LeafAttribute("Testx"),
				new ObjectSubstance("Biswa"));
		assertFalse(equals3.visit(containerEntry));
	}
}
