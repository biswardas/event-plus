package com.biswa.ep.entities.predicate.join;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;

public class NotEquals extends JoinPredicate {
	private Attribute left;
	private Attribute right;
	public NotEquals(Attribute left,Attribute right) {
		this.left = left;
		this.right = right;
	}
	@Override
	public boolean visit(ContainerEntry o1, ContainerEntry o2) {
		return o1.getSubstance(left).compareTo(o2.getSubstance(right))!=0;
	}
}
