package com.biswa.ep.entities.predicate.join;

import com.biswa.ep.entities.ContainerEntry;

abstract public class JoinPredicate{
	public abstract boolean visit(ContainerEntry o1, ContainerEntry o2);
}
