package com.biswa.ep.entities.spec;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;

public class SortSpec implements Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1617921625061798475L;

	public static class SortOrder{
		public SortOrder(Attribute attribute,boolean descending){
			this.attribute=attribute;
			this.descending = descending;
		}
		private Attribute attribute;
		private boolean descending;
		public Attribute getAttribute() {
			return attribute;
		}
		public boolean isDescending() {
			return descending;
		}		
	}
	private SortOrder[] sortorder;

	public SortSpec(SortOrder ... sortOrder){
		this.sortorder=sortOrder;
	}
	
	@Override
	public void apply(ContainerListener listener) {
		PivotContainer abs = (PivotContainer) listener;
		abs.applySort(sortorder);
	}

}
