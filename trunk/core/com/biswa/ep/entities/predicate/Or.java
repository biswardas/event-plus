package com.biswa.ep.entities.predicate;

import java.util.ArrayList;

import com.biswa.ep.entities.ContainerEntry;


public class Or extends Predicate{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3273003054155362357L;
	private ArrayList<Predicate> al = new ArrayList<Predicate>();
	public Or chain(Predicate predicate){
		al.add(predicate);
		return this;
	}
	@Override
	public boolean visit(ContainerEntry containerEntry) {
		boolean result = false;
		for(Predicate predicate:al){
			result = result || predicate.visit(containerEntry);
			if(result){
				break;//Already success exit
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
