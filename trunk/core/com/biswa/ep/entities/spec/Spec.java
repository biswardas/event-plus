package com.biswa.ep.entities.spec;

import java.io.Serializable;

import com.biswa.ep.entities.ContainerListener;

public abstract class Spec implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 128447626953091459L;
	private final String sinkName;
	public Spec(String sinkName){
		this.sinkName=sinkName;
	}
	public String getSinkName() {
		return sinkName;
	}
	public abstract void apply(ContainerListener listener);
}
