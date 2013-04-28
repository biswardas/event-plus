package com.biswa.ep.entities.spec;

import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;

public class CollapseSpec extends Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8995227152478399256L;
	private int identity;
	private boolean collapse;
	
	public CollapseSpec(String sinkName,int identity,boolean collapse){
		super(sinkName);
		this.identity=identity;
		this.collapse=collapse;
	}
	
	@Override
	public void apply(ContainerListener listener) {
		PivotContainer pivotSchema = (PivotContainer) listener;
		pivotSchema.getFilterAgent(getSinkName()).applyCollapse(identity,collapse);
	}
}
