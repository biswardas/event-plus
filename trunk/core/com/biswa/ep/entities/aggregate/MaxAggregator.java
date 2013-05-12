package com.biswa.ep.entities.aggregate;

import com.biswa.ep.ObjectComparator;


public class MaxAggregator extends Aggregator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4302847925466344642L;
	private ObjectComparator objectComparator = new ObjectComparator();
	public MaxAggregator(String aggrAttr) {
		super(aggrAttr);
	}

	@Override
	protected Object aggregate() {
		Object intermediateAggr = getNextObject();
		while(hasNext()){
			Object substance = getNextObject();
			if(substance==null){
				continue;
			}else{
				if(intermediateAggr==null){
					intermediateAggr = substance;
				} else {
					if(objectComparator.compare(intermediateAggr, substance)<0){
						intermediateAggr = substance;
					}
				}
			}
		}
		return intermediateAggr;
	}
	@Override
	protected Object aggregate(Object preUpdate,Object postUpdate){
		Object intermediateAggr = getCurrentPivotEntry().getSubstance(getAggrAttr());
		if(intermediateAggr==null){
			return postUpdate;
		}else{
			if(objectComparator.compare(intermediateAggr, postUpdate)<0){
				intermediateAggr = postUpdate;
			} else {
				intermediateAggr = failSafeaggregate(getCurrentPivotEntry());
			}
		}
		return intermediateAggr;
	}
}
