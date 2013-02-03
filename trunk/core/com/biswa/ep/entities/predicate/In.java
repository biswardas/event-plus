package com.biswa.ep.entities.predicate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;

public class In extends Mono{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7384525477542719553L;
	private Set<Substance> substanceSet = new HashSet<Substance>();
	public In(Attribute attribute,Substance ... substances){
		super(attribute);
		this.substanceSet.addAll(Arrays.asList(substances));
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		Substance incomingSubstance = containerEntry.getSubstance(attribute);
		return incomingSubstance == null ?false :  substanceSet.contains(incomingSubstance);
	}
}