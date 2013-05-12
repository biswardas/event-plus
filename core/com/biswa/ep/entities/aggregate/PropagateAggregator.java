package com.biswa.ep.entities.aggregate;


public class PropagateAggregator extends Aggregator { 

	/**
	 * 
	 */
	private static final long serialVersionUID = -8877702159215112794L;

	public PropagateAggregator(String aggrAttr) {
		super(aggrAttr);
	}

	@Override
	protected Object aggregate() {
		Object intermediateAggr = getNextObject();
		while(hasNext()){
			Object substance = getNextObject();
			if(substance==null){
				intermediateAggr=null;
				break;
			}else{
				if(intermediateAggr==null){
					break;
				}else{
					if(intermediateAggr.equals(substance)){
						continue;
					}else{
						intermediateAggr=null;
						break;
					}
				}				
			}
		}
		return intermediateAggr;
	}
}
