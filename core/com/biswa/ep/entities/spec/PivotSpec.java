package com.biswa.ep.entities.spec;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;

public class PivotSpec extends Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1242563300597959342L;
	Attribute[] pivotArray = new Attribute[0];
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
