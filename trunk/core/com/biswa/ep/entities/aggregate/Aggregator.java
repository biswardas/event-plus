package com.biswa.ep.entities.aggregate;

import java.util.Iterator;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.PivotContainer.PivotEntry;

abstract public class Aggregator{
	private Attribute aggrAttr;
	private Iterator<? extends ContainerEntry> iter;
	
	public Aggregator(String aggrAttr){
		this.aggrAttr = new LeafAttribute(aggrAttr);
	}
	
	public Attribute getAggrAttr() {
		return aggrAttr;
	}
	
	public final Object failSafeaggregate(PivotEntry pivotEntry) {
		Object aggergatedSubstance = null;
		try{
			iter=pivotEntry.iterator();
			return aggregate();
		}catch(Exception e){
			aggergatedSubstance = null; 
		}
		return aggergatedSubstance;
	}
	
	protected Object getNextObject(){
		return getNextEntry().getSubstance(aggrAttr);
	}
	
	protected ContainerEntry getNextEntry(){
		return iter.next();
	}
	
	protected boolean hasNext(){
		return iter.hasNext();
	}
	
	public void prepare() {
		aggrAttr=aggrAttr.getRegisteredAttribute();
	}
	
	protected abstract Object aggregate();
}
