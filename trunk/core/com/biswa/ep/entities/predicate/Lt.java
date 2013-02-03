package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;


public class Lt extends Mono{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6798409367546962128L;
	private Substance substance;
	public Lt(Attribute attribute,Substance substance){
		super(attribute);
		this.substance = substance;
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		Substance incomingSubstance = containerEntry.getSubstance(attribute);
		return incomingSubstance == null ?false :  incomingSubstance.compareTo(substance)<0;
	}
}