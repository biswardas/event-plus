package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.Attribute;


abstract public class Mono extends Predicate{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8565063448810236158L;
	protected Attribute attribute;
	public Mono(Attribute attribute){
		this.attribute = attribute;
	}
	@Override
	public void prepare(){
		Attribute temp = null;
		
		temp = attribute.getRegisteredAttribute();
		if(temp==null || temp.isStateless()){
			throw new RuntimeException("Invalid Filter applied with attribute "+attribute);
		}else{
			this.attribute = temp;
			temp = null;
		}
	}
}
