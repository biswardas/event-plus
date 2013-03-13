package com.biswa.ep.entities.substance;

import java.util.HashMap;
import java.util.Map;

public class MultiSubstance extends AbstractSubstance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6592962975592759093L;
	
	/**
	 *The value holder for the multi value. 
	 */
	private final Map<Integer,Object> multivalue = new HashMap<Integer,Object>(4,1);
	/**Adds a value to the multi value entry
	 * 
	 * @param minor
	 * @param substance
	 */
	public void addValue(Integer minor,Substance substance){
		multivalue.put(minor,substance==null?null:substance.getValue());
	}
	
	/**Removes a value from the multi value entry.
	 * 
	 * @param minor
	 */
	public void removeValue(Integer minor){
		multivalue.remove(minor);
	}
	
	/** Indicates whether this multivalue is empty?
	 * 
	 * @return boolean
	 */
	public boolean isEmpty(){
		return multivalue.isEmpty();
	}
	
	@Override
	public Map<Integer,Object> getValue() {
		return multivalue;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public String toString() {
		return "MultiSubstance [multivalue=" + multivalue + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj){
			return true;
		}else{
			return false;
		}
	}
}
