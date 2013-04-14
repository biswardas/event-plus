package com.biswa.ep.entities.spec;

import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;

public class CollapseSpec implements Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8995227152478399256L;
	private int identity;
	private boolean collapse;
	
	public CollapseSpec(int identity,boolean collapse){
		this.identity=identity;
		this.collapse=collapse;
	}
	
	@Override
	public void apply(ContainerListener listener) {
		PivotContainer abs = (PivotContainer) listener;
		abs.applyCollapse(identity,collapse);
	}
}
