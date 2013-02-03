package com.biswa.ep.entities.spec;
import java.util.HashMap;
import java.util.Map;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.PivotContainer;
import com.biswa.ep.entities.aggregate.Aggregator;
public class AggrSpec implements Spec {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7397632399828335683L;
	Map<Attribute,Aggregator> aggrMap = new HashMap<Attribute,Aggregator>();
	@Override
	public void apply(ContainerListener listener) {
		PivotContainer pivotSchema = (PivotContainer)listener;
		pivotSchema.aggregate(aggrMap);
	}
	public void add(Attribute attr,Aggregator aggr){
		aggrMap.put(attr, aggr);
	}
}
