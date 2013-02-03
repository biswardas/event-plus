package com.biswa.ep.entities.predicate;

import java.util.ArrayList;

import com.biswa.ep.entities.ContainerEntry;


public class And extends Predicate{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7223393309266575143L;
	private ArrayList<Predicate> al = new ArrayList<Predicate>();
	public And chain(Predicate predicate){
		al.add(predicate);
		return this;
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		boolean result = true;
		for(Predicate predicate:al){
			result = result && predicate.visit(containerEntry);
			if(!result){
				break;//Already failed exit
			}
		}
		return result;
	}
	@Override
	public void prepare() {
		for(Predicate predicate:al){
			predicate.prepare();
		}
	}	
}