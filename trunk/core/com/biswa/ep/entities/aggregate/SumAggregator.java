package com.biswa.ep.entities.aggregate;


public class SumAggregator extends Aggregator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4302847925466344642L;

	public SumAggregator(String aggrAttr) {
		super(aggrAttr);
	}

	@Override
	protected Object aggregate() {
		double intermediateAggr = 0d;
		while(hasNext()){
			Object substance = getNextObject();
			if(substance==null){
				continue;
			}else{
				intermediateAggr = intermediateAggr + ((Number) substance).doubleValue();
			}
		}
		return intermediateAggr;
	}
	@Override
	protected Object aggregate(Object preUpdate,Object postUpdate){
		Double intermediateAggr = (Double) getCurrentPivotEntry().getSubstance(getAggrAttr());
		if(intermediateAggr==null){
			if(preUpdate==null){
				if(postUpdate==null){
					return intermediateAggr;
				}else{
					return postUpdate;
				}
			}else{
				if(postUpdate==null){
					return -((Number) preUpdate).doubleValue();
				}else{
					return ((Number) postUpdate).doubleValue()-((Number) preUpdate).doubleValue();
				}
			}
		}else{
			if(preUpdate==null){
				if(postUpdate==null){
					return intermediateAggr;
				}else{
					return intermediateAggr+((Number) postUpdate).doubleValue();
				}
			}else{
				if(postUpdate==null){
					return intermediateAggr -((Number) preUpdate).doubleValue();					
				} else {
					return intermediateAggr+((Number) postUpdate).doubleValue()-((Number) preUpdate).doubleValue();
				}
			}
		}
	}
}
