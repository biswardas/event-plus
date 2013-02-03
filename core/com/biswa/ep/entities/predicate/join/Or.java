package com.biswa.ep.entities.predicate.join;

import java.util.ArrayList;

import com.biswa.ep.entities.ContainerEntry;

public class Or extends JoinPredicate {

	private ArrayList<JoinPredicate> al = new ArrayList<JoinPredicate>();
	public void and(JoinPredicate predicate){
		al.add(predicate);
	}
	@Override
	public boolean visit(ContainerEntry o1, ContainerEntry o2) {
		boolean result = true;
		for(JoinPredicate predicate:al){
			result = result && predicate.visit(o1,o2);
			if(!result){
				break;//Already failed exit
			}
		}
		return result;
	}
}
