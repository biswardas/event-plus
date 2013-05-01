package com.biswa.ep.entities.spec;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.Predicate;

/**Filter Spec which is applied on the container to filter 
 * the container entries.
 * 
 * @author biswa
 *
 */
public class FilterSpec extends Spec {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5748229391329289278L;
	public static final FilterSpec TRUE = new FilterSpec(null,null){
		/**
		 * 
		 */
		private static final long serialVersionUID = 8125714682801993456L;
		/**
		 * Over ridden filter method for efficiency.
		 */
		final public boolean filter(ContainerEntry containerEntry){
			return true;
		}
	};
	private Predicate predicate;
	private enum ChainMode{AND,OR,NONE};
	private FilterSpec chainFilter;
	private ChainMode chainMode = ChainMode.NONE;
	
	/** Default Constructor 
	 * 
	 * @param predicate Predicate
	 */
	public FilterSpec(String sinkName,Predicate predicate){
		super(sinkName);
		this.predicate = predicate;
	}
	
	/** Constructor to create a feedback 
	 * 
	 * @param predicate Predicate
	 * @param chainMode String
	 */
	public FilterSpec(String sinkName,Predicate predicate,String chainMode){
		super(sinkName);
		this.predicate = predicate;		
		if(ChainMode.valueOf(chainMode)!=null){
			this.chainMode=ChainMode.valueOf(chainMode);	
		}
	}

	@Override
	public void apply(ContainerListener listener) {
		AbstractContainer abstractSchema = (AbstractContainer)listener;
		abstractSchema.applyFilter(this);
	}

	public FilterSpec prepare(AbstractContainer appliedContainer) {
		predicate.prepare(appliedContainer.getTypeMap());
		return this;
	}
	
	/** Business method where filter evaluates the container entry.
	 * 
	 * @param containerEntry ContainerEntry
	 * @return boolean
	 */
	public boolean filter(ContainerEntry containerEntry){
		switch(chainMode){
			case NONE: return predicate.visit(containerEntry);
			case AND: return chainFilter.filter(containerEntry) && predicate.visit(containerEntry);
			case OR: return chainFilter.filter(containerEntry) || predicate.visit(containerEntry);
		}
		return false;
	}
	
	/**If the Guest filter is null the the caller is output. If Guest is not null
	 * then the Guest is output and Guest hosts the caller and chainFilter. Little bit 
	 * of rocket science here read twice NO KIDDING.
	 * @param otherFilter
	 * @return FilterSpec
	 */
	public FilterSpec chain(FilterSpec otherFilter){
		if(otherFilter!=null){
			otherFilter.chainFilter=this;
			return otherFilter;
		}
		return this;
	}
}
