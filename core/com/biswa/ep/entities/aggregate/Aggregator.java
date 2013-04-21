package com.biswa.ep.entities.aggregate;

import java.util.ArrayList;
import java.util.Iterator;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.PivotContainer.PivotEntry;

abstract public class Aggregator{
	private Attribute aggrAttr;
	private ContainerEntry pivotEntry;
	private Iterator<? extends ContainerEntry> iter;
	private final ArrayList<Aggregator> chainedAggrList = new ArrayList<Aggregator>();
	public Aggregator(String aggrAttr){
		this.aggrAttr = new LeafAttribute(aggrAttr);
	}

	public Attribute getAggrAttr() {
		return aggrAttr;
	}
	
	public Attribute getTargetAttr() {
		return aggrAttr;
	}
	
	public final Object failSafeaggregate(PivotEntry pivotEntry) {
		this.pivotEntry=pivotEntry;
		Object aggergatedSubstance = null;
		try{
			iter=pivotEntry.iterator();
			return aggregate();
		}catch(Exception e){
			aggergatedSubstance = null; 
		}
		return aggergatedSubstance;
	}
	
	public void prepare() {
		aggrAttr=aggrAttr.getRegisteredAttribute();
		for(Aggregator oneChainedAggregator:chainedAggrList){
			oneChainedAggregator.prepare();
		}
	}

	public Aggregator chain(Aggregator aggregator){
		chainedAggrList.add(aggregator);
		return this;
	}
	
	public ArrayList<Aggregator> getChainedAggregators(){
		return chainedAggrList;
	}
	
	public boolean isExpression(){
		return false;
	}	

	protected final ContainerEntry getCurrentPivotEntry(){
		return pivotEntry;
	}
	
	protected final Object getNextObject(){
		return getNextEntry().getSubstance(aggrAttr);
	}
	
	protected final ContainerEntry getNextEntry(){
		return iter.next();
	}
	
	protected final boolean hasNext(){
		return iter.hasNext();
	}
	
	protected abstract Object aggregate();
}
