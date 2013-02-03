package com.biswa.ep.entities.predicate;

import java.io.Serializable;

import com.biswa.ep.entities.ContainerEntry;


public abstract class Predicate implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4601421301686976655L;
	public Predicate chain(Predicate predicate){return this;}
	public abstract boolean visit(ContainerEntry containerEntry);
	public void prepare(){}
}