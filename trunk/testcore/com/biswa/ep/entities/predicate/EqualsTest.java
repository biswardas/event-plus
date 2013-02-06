package com.biswa.ep.entities.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public class EqualsTest {

	@Test
	public void testVisit1() {
		Eq equals1 = new Eq(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
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
		Eq equals1 = new Eq(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(""));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(equals1.visit(containerEntry));
	}

	@Test(expected=NullPointerException.class)
	public void tesVisit3() {

		Eq equals1 = new Eq(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(equals1.visit(containerEntry));
	}

	@Test(expected=NullPointerException.class)
	public void tesVisit4() {

		Eq equals1 = new Eq(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa")); 
			assertFalse(equals1.visit(null));
	}

	@Test(expected=NullPointerException.class)
	public void testVisit5() {
		Eq equals2 = new Eq(new LeafAttribute("Test"),
				new ObjectSubstance(null));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(equals2.visit(containerEntry));
	}

	@Test
	public void testVisit6() {
		Eq equals2 = new Eq(new LeafAttribute("Test"),
				new ObjectSubstance(null));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);

		Eq equals3 = new Eq(new LeafAttribute("Testx"),
				new ObjectSubstance("Biswa"));
		assertFalse(equals3.visit(containerEntry));
	}

	@Test
	public void testEquals1() {
		Eq equals1 = new Eq(new LeafAttribute("Test"),
				new ObjectSubstance("Biswa"));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(equals1.visit(containerEntry));
	}

	
	public void testEquals2() {
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		Eq equals2 = new Eq(null, new ObjectSubstance("Biswa"));
		assertFalse(equals2.visit(containerEntry));
	}

	@Test(expected=NullPointerException.class)
	public void testEquals3() {
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		Eq equals3 = new Eq(new LeafAttribute("Test"), null);
		assertTrue(equals3.visit(containerEntry));
	}

	public void testEquals4() {
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();
		hm.put(new LeafAttribute("Test"), new ObjectSubstance(null));
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		Eq equals4 = new Eq(null, null);
		assertFalse(equals4.visit(containerEntry));
	}

}
