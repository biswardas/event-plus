package com.biswa.ep.entities.spec;
import java.util.LinkedHashMap;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;
import com.biswa.ep.entities.aggregate.Aggregator;
public class AggrSpec extends Spec {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7397632399828335683L;
	private LinkedHashMap<Attribute,Aggregator> aggrMap = new LinkedHashMap<Attribute,Aggregator>();
	public AggrSpec(String sinkName){
		super(sinkName);
	}
	@Override
	public void apply(ContainerListener listener) {
		PivotContainer pivotSchema = (PivotContainer)listener;
		pivotSchema.getFilterAgent(getSinkName()).applyAggregation(aggrMap);
	}
	public void add(Aggregator aggr){
		if(!aggr.isExpression()){
			aggrMap.put(aggr.getAggrAttr(), aggr);
		}else{
			aggrMap.get(aggr.getAggrAttr()).chain(aggr);
		}
	}
}
