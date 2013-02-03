package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;

public class Ge extends Mono{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1614031829287095939L;
	private Substance substance;
	public Ge(Attribute attribute,Substance substance){
		super(attribute);
		this.substance = substance;
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		Substance incomingSubstance = containerEntry.getSubstance(attribute);
		return incomingSubstance == null ?false :  incomingSubstance.compareTo(substance)>=0;
	}
}
