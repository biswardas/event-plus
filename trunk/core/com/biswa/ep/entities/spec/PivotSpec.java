package com.biswa.ep.entities.spec;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;

public class PivotSpec extends Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1242563300597959342L;
	private Attribute[] pivotArray = new Attribute[0];
	/**Constructor to pivot containers.
	 * 
	 * @param sinkName String Name of the container on which this pivoting will be applied.
	 * @param pivotArray Sequence of pivoting of the Attributes.
	 */
	public PivotSpec(String sinkName,Attribute ... pivotArray){
		super(sinkName);
		this.pivotArray=pivotArray;
	}
	@Override
	public void apply(ContainerListener listener) {
		PivotContainer pivotSchema = (PivotContainer)listener;
		pivotSchema.getFilterAgent(getSinkName()).applyPivot(pivotArray);
	}
}
