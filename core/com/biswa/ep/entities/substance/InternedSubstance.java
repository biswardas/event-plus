package com.biswa.ep.entities.substance;

import java.io.ObjectStreamException;

import com.biswa.ep.InternedStorage;

public class InternedSubstance extends IndirectedSubstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6206292920769446565L;
	private static final InternedStorage internedStorage = new InternedStorage();
	private Substance subject;
	public InternedSubstance(Substance subject) {
		this(subject,null);
	}	
	public InternedSubstance(Substance subject,Substance actualSubstance) {
		super(actualSubstance);
		this.subject=subject;
	}
	
	private Object readResolve() throws ObjectStreamException{
		Substance resolvedSubstance = null;
		if(this.actualSubstance==null){
			resolvedSubstance = internedStorage.get(subject);
		}else{
			resolvedSubstance = actualSubstance;
			internedStorage.put(subject, resolvedSubstance);
		}
		return resolvedSubstance;
	}
}
