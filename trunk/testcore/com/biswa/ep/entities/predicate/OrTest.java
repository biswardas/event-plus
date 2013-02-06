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

public class OrTest {

	@Test
	public void testVisit1() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")));

		And and1 = new And();
		and1.chain(new Eq(new LeafAttribute("Test1"),new ObjectSubstance("Biswax")));
		Or or = new Or();
		or.chain(and);
		or.chain(and1);
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("BiswaF"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("Biswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(or.visit(containerEntry));
	}

	@Test
	public void testVisit2() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")));

		And and1 = new And();
		and1.chain(new Eq(new LeafAttribute("Test1"),new ObjectSubstance("Biswax")));
		Or or = new Or();
		or.chain(and);
		or.chain(and1);
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("BiswaP"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(or.visit(containerEntry));
	}

	@Test
	public void testVisit3() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")));

		And and1 = new And();
		and1.chain(new Eq(new LeafAttribute("Test1"),new ObjectSubstance("Biswax")));
		Or or = new Or();
		or.chain(and);
		or.chain(and1);
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("Biswa"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("Biswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertTrue(or.visit(containerEntry));
	}

	@Test
	public void testVisit4() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")));

		And and1 = new And();
		and1.chain(new Eq(new LeafAttribute("Test1"),new ObjectSubstance("Biswax")));
		Or or = new Or();
		or.chain(and);
		or.chain(and1);
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				put(new LeafAttribute("Test"), new ObjectSubstance("NoBiswa"));
				put(new LeafAttribute("Test1"), new ObjectSubstance("NoBiswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(or.visit(containerEntry));
	}

	@Test
	public void testVisit5() {
		And and = new And();
		and.chain(new Eq(new LeafAttribute("Test"),new ObjectSubstance("Biswa")));

		And and1 = new And();
		and1.chain(new Eq(new LeafAttribute("Test1"),new ObjectSubstance("Biswax")));
		Or or = new Or();
		or.chain(and);
		or.chain(and1);
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				//put(new LeafAttribute("Test"), new ObjectSubstance("NoBiswa"));
				//put(new LeafAttribute("Test1"), new ObjectSubstance("NoBiswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(or.visit(containerEntry));
	}

	@Test
	public void testVisit6() {
		Or or = new Or();
		Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>() {
			{
				//put(new LeafAttribute("Test"), new ObjectSubstance("NoBiswa"));
				//put(new LeafAttribute("Test1"), new ObjectSubstance("NoBiswax"));
			}
		};
		ContainerEntry containerEntry = new MyContainerEntry(0,hm);
		assertFalse(or.visit(containerEntry));
	}

}
