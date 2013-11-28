package com.biswa.ep.entities.spec;

import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;
/**
 * Spec used to collapse/expand a pivoted entry.
 * 
 * @author Biswa
 *
 */
public class CollapseSpec extends Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8995227152478399256L;
	/**
	 * Row identity which will be collapsed
	 */
	private int identity;
	/**
	 * true to collapse / false to explode
	 */
	private boolean collapse;
	/**
	 * 
	 * @param sinkName String Name of the container on which this spec will be applied.
	 * @param identity int Row identity which will be collapsed
	 * @param collapse boolean true to collapse / false to explode
	 */
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
