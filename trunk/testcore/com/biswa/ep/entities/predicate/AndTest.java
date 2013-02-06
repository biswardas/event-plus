package com.biswa.ep.entities.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public class AndTest {

	@Test
	public void testVisit1() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")));
		and.chain(new Eq(new LeafAttribute("Test1"),new ObjectSubstance("Biswax")));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("Biswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(and.visit(containerEntry));
	}
	@Test
	public void testVisit2() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")));
		and.chain(new Eq(new LeafAttribute("Test1"),new ObjectSubstance("Biswax")));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("Bisway"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(and.visit(containerEntry));
	}
	@Test
	public void testVisit3() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")))
		.chain(new Eq(new LeafAttribute("Test2"),new ObjectSubstance("Biswax")));
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("Biswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(and.visit(containerEntry));
	}
	@Test
	public void testVisit4() {
		And and = new And();
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("Biswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(and.visit(containerEntry));
	}
}
