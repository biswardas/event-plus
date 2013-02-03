package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;

public class Gt extends Mono{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8376025527838463759L;
	private Substance substance;
	public Gt(Attribute attribute,Substance substance){
		super(attribute);
		this.substance = substance;
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		Substance incomingSubstance = containerEntry.getSubstance(attribute);
		return incomingSubstance == null ?false :  incomingSubstance.compareTo(substance)>0;
	}
}
