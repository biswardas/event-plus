package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;


public class Le extends Mono{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1258981317183265433L;
	private Substance substance;
	public Le(Attribute attribute,Substance substance){
		super(attribute);
		this.substance = substance;
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		Substance incomingSubstance = containerEntry.getSubstance(attribute);
		return incomingSubstance == null ?false :  incomingSubstance.compareTo(substance)<=0;
	}
}