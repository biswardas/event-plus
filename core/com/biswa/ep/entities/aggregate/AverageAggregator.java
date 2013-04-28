package com.biswa.ep.entities.aggregate;


public class AverageAggregator extends Aggregator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2076577937588920754L;

	public AverageAggregator(String aggrAttr) {
		super(aggrAttr);
	}

	@Override
	protected Object aggregate() {
		Double intermediateAggr = 0d;
		int i=0;
		while(hasNext()){
			i++;
			Object substance = getNextObject();
			if(substance==null){
				continue;
			}else{
				intermediateAggr = intermediateAggr + (Double) substance;
			}
		}
		return intermediateAggr/i;
	}
}
