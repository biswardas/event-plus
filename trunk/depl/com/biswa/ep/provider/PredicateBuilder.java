package com.biswa.ep.provider;

import com.biswa.ep.entities.Predicate;

public class PredicateBuilder {

	public static Predicate buildPredicate(String predicate) {
		return new Predicate("filter="+predicate);
	}
}
