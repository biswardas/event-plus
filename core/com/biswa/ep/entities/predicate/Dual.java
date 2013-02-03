package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.Attribute;


abstract public class Dual extends Predicate{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8565063448810236158L;
	protected Attribute attribute1;
	protected Attribute attribute2;
	public Dual(Attribute attribute1,Attribute attribute2){
		this.attribute1 = attribute1;
		this.attribute2 = attribute2;
	}
	@Override
	public void prepare(){
		Attribute temp = null;
		
		temp = attribute1.getRegisteredAttribute();
		if(temp==null || temp.isStateless()){
			throw new RuntimeException("Invalid Filter applied with attribute "+attribute1);
		}else{
			this.attribute1 = temp;
			temp = null;
		}
		

		temp = attribute2.getRegisteredAttribute();
		if(temp==null || temp.isStateless()){
			throw new RuntimeException("Invalid Filter applied with attribute "+attribute2);
		}else{
			this.attribute2 = temp;
			temp = null;
		}
	}
}
