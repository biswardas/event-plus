package com.biswa.ep.entities.aggregate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.PivotContainer.PivotAgent.PivotEntry;
/**
 * Abstract aggregator class providing aggregation life cycle methods.
 * @author Biswa
 *
 */
abstract public class Aggregator implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -716425530339944777L;
	private Attribute aggrAttr;
	private ContainerEntry pivotEntry;
	private Iterator<? extends ContainerEntry> iter;
	private final ArrayList<Aggregator> chainedAggrList = new ArrayList<Aggregator>();
	public Aggregator(String aggrAttr){
		this.aggrAttr = new LeafAttribute(aggrAttr);
	}
	
	/**Driver of this aggregation, Who triggered the aggregation.
	 * 
	 * @return Attribute
	 */
	public Attribute getAggrAttr() {
		return aggrAttr;
	}
	
	/**
	 * Attribute on which the result going to reflect.
	 * @return Attribute
	 */
	final public Attribute getTargetAttr() {
		return aggrAttr;
	}
	
	/**
	 * Entry point for aggregation. Returns aggregated substance.
	 * @param pivotEntry PivotEntry
	 * @return Object
	 */
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

	/**
	 * Entry point for aggregation. Returns aggregated substance.
	 * @param pivotEntry PivotEntry
	 * @param preUpdate Object
	 * @param postUpdate Object
	 * @return Object
	 */
	public final Object failSafeaggregate(PivotEntry pivotEntry,Object preUpdate,Object postUpdate) {
		this.pivotEntry=pivotEntry;
		Object aggergatedSubstance = null;
		try{
			aggergatedSubstance = aggregate(preUpdate,postUpdate);
		}catch(Exception e){
			aggergatedSubstance = null; 
		}
		return aggergatedSubstance;
	}
	/**
	 * Before this aggregator is used in this container. This method must be called. This
	 * is the hook for any necessary optimization required in this aggregator.
	 */
	public void prepare(AbstractContainer abs) {
		Attribute tempAttr=aggrAttr.getRegisteredAttribute();
		if(tempAttr!=null){
			aggrAttr=tempAttr;
			for(Aggregator oneChainedAggregator:chainedAggrList){
				oneChainedAggregator.prepare(abs);
			}
		}else{
			throw new RuntimeException("Referencing non existent Attribute:"+aggrAttr);
		}
	}

	/**
	 * Chains any additional aggregator to this.
	 */
	public Aggregator chain(Aggregator aggregator){
		chainedAggrList.add(aggregator);
		return this;
	}
	
	/**
	 * Returns the chained aggregator.
	 * @return ArrayList<Aggregator>
	 */
	public ArrayList<Aggregator> getChainedAggregators(){
		return chainedAggrList;
	}
	
	/**
	 * Is this aggregator an expression?
	 * @return boolean
	 */
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
	
	protected Object aggregate(Object preUpdate,Object postUpdate){
		return aggregate();
	}
}
