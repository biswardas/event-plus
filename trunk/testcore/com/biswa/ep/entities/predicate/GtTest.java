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

public class GtTest {

	@Test
	public void testVisit1() {
		Gt equals1 = new Gt(new LeafAttribute("Test"),
				new ObjectSubstance("Aiswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(equals1.visit(containerEntry));
	}

	@Test
	public void testVisit2() {
		Gt equals1 = new Gt(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance("Aiswa"));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(equals1.visit(containerEntry));
	}


	@Test(expected=NullPointerException.class)
	public void tesVisit3() {

		Gt equals1 = new Gt(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(equals1.visit(containerEntry));
	}


	@Test(expected=NullPointerException.class)
	public void tesVisit4() {

		Gt equals1 = new Gt(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		assertFalse(equals1.visit(null));
	}


	@Test(expected=NullPointerException.class)
	public void testVisit5() {
		Gt equals2 = new Gt(new LeafAttribute("Test"),
				new ObjectSubstance(null));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(equals2.visit(containerEntry));

	}

	@Test
	public void testVisit6() {
		Gt equals2 = new Gt(new LeafAttribute("Test"),
				new ObjectSubstance(null));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);

		Gt equals3 = new Gt(new LeafAttribute("Testx"),
				new ObjectSubstance("Biswa"));
		assertFalse(equals3.visit(containerEntry));
	}
}
