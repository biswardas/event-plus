package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;


public class ALt extends Dual{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6077369302086251978L;
	public ALt(Attribute attribute1,Attribute attribute2){
		super(attribute1,attribute2);
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		Substance incomingSubstance1 = containerEntry.getSubstance(attribute1);
		Substance incomingSubstance2 = containerEntry.getSubstance(attribute2);
		return incomingSubstance1 == null ?false : incomingSubstance1.compareTo(incomingSubstance2)<0;
	}
}