package com.biswa.ep.entities.spec;

import java.util.LinkedHashMap;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;

public class SortSpec extends Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1617921625061798475L;
	/**
	 * Sorting spec on the container. Attributes and the sorting direction as applied.
	 */
	private LinkedHashMap<Attribute,Boolean> sortorder = new LinkedHashMap<Attribute,Boolean>();
	/**
	 * 
	 * @param sinkName String
	 */
	public SortSpec(String sinkName){
		super(sinkName);
	}
	/**Add sorting spec on the container.
	 * 
	 * @param attribute Attribute 
	 * @param order Boolean true Ascending  / false Descending
	 */
	public void addSortOrder(Attribute attribute,Boolean order){
		sortorder.put(attribute,order);
	}
	
	@Override
	public void apply(ContainerListener listener) {
		PivotContainer abs = (PivotContainer) listener;
		abs.getFilterAgent(getSinkName()).applySort(sortorder);
	}
}
