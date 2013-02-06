package com.biswa.ep.util.parser.predicate;

import static org.junit.Assert.*;

import org.junit.Test;

import com.biswa.ep.entities.predicate.Predicate;
import com.biswa.ep.util.parser.predicate.PredicateBuilder;

public class PredicateBuilderTest {
	
	

	@Test
	public void testAEqPredicate() {
		Predicate p = PredicateBuilder.buildPredicate("${a}==${b};");
		System.out.println(p);
	}
	@Test
	public void testAGePredicate() {
		Predicate p = PredicateBuilder.buildPredicate("${a}>=${b};");
		System.out.println(p);
	}
	@Test
	public void testALePredicate() {
		Predicate p = PredicateBuilder.buildPredicate("${a}<=${b};");
		System.out.println(p);
	}

	@Test
	public void testAGtPredicate() {
		Predicate p = PredicateBuilder.buildPredicate("${a}>${b};");
		System.out.println(p);
	}

	@Test
	public void testALtPredicate() {
		Predicate p = PredicateBuilder.buildPredicate("${a}<${b};");
		System.out.println(p);
	}

	@Test
	public void testANePredicate() {
		Predicate p = PredicateBuilder.buildPredicate("${a}!=${b};");
		System.out.println(p);
	}
	
	@Test
	public void testBuildPredicate() {
		Predicate p = PredicateBuilder.buildPredicate("${x} in (5,6,7,8) || (((${a}==5 && ${b}>4 && ${c}<7.7) || (${d}==9 && ${e}!=\"Biswa\")) && ((${a}==5 && ${b}>4 && ${c}<7.7) || (${d}==9 && ${e}!=\"Biswa\")));");
		System.out.println(p);
	}

	@Test
	public void testTrue() {
		Predicate p = PredicateBuilder.buildPredicate("true");
		System.out.println(p);
	}

	@Test
	public void testTrue1() {
		Predicate p = PredicateBuilder.buildPredicate("(true)");
		System.out.println(p);
	}

	@Test
	public void testTrue2() {
		Predicate p = PredicateBuilder.buildPredicate("");
		System.out.println(p);
	}
	
	@Test
	public void testTrue3() {
		Predicate p = PredicateBuilder.buildPredicate("()");
		System.out.println(p);
	}

	@Test
	public void testTrue4() {
		Predicate p = PredicateBuilder.buildPredicate("(())");
		System.out.println(p);
	}

	@Test
	public void testTrue5() {
		Predicate p = PredicateBuilder.buildPredicate(null);
		System.out.println(p);
	}

	@Test
	public void testFalse() {
		Predicate p = PredicateBuilder.buildPredicate("false");
		System.out.println(p);
	}

	@Test
	public void testFalse1() {
		Predicate p = PredicateBuilder.buildPredicate("(false)");
		System.out.println(p);
	}

	@Test
	public void testFalse2() {
		Predicate p = PredicateBuilder.buildPredicate("((false))");
		System.out.println(p);
	}

	@Test
	public void testFalse3() {
		Predicate p = PredicateBuilder.buildPredicate("false && false");
		System.out.println(p);
	}

	@Test
	public void testFalse4() {
		Predicate p = PredicateBuilder.buildPredicate("false && true");
		System.out.println(p);
	}

	@Test
	public void testFalse5() {
		Predicate p = PredicateBuilder.buildPredicate("false || false");
		System.out.println(p);
	}
}
