package com.biswa.ep.entities.spec;

import java.io.Serializable;

import com.biswa.ep.entities.ContainerListener;
/**
 * Framework for applying operations such as aggregation,filtering,
 * sorting,collapsing in the container.
 * 
 * @author Biswa
 *
 */
public abstract class Spec implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 128447626953091459L;
	
	private final String sinkName;
	/**
	 * Name of the container on which this spec will be applied.
	 * @param sinkName String
	 */
	public Spec(String sinkName){
		this.sinkName=sinkName;
	}
	
	/**Returns name of the container.
	 * 
	 * @return String
	 */
	public final String getSinkName() {
		return sinkName;
	}
	
	/**Applies the spec on the passed container. This method is idempotent in nature.
	 * 
	 * @param listener ContainerListener
	 */
	public abstract void apply(ContainerListener listener);
}
