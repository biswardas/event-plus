package com.biswa.ep.deployment.handler;

import java.util.ArrayList;
import java.util.List;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Pivot;
import com.biswa.ep.deployment.util.Sort;
import com.biswa.ep.deployment.util.Summary;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.PivotContainer;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.spec.AggrSpec;
import com.biswa.ep.entities.spec.PivotSpec;
import com.biswa.ep.entities.spec.SortSpec;
import com.biswa.ep.entities.spec.SortSpec.SortOrder;

public class PivotDeploymentHandler extends DeploymentHandler{

	public ConcreteContainer deploy(Container container,Context context,ContainerManager containerManager) {
		ConcreteContainer cs = new PivotContainer(getQualifiedName(container, context),getProperties(container.getParam()));
		
		deployCommon(container, cs,containerManager);
		
		applyPivot(container, cs);

		applySummary(container, cs);
		
		applySort(container, cs);
		
		expectConnected(container, cs);
		
		return cs;
	}

	private void applySort(Container container, ConcreteContainer cs) {
		Sort sort = container.getSort();
		if(sort!=null){
			SortOrder[] sortorder = new SortOrder[sort.getAttribute().size()];
			int index = 0;
			for(com.biswa.ep.deployment.util.Attribute attribute:sort.getAttribute()){
				sortorder[index++]=new SortOrder(new LeafAttribute(attribute.getName()),attribute.isDescending());
			}
			SortSpec sortSpec = new SortSpec(sortorder);
			cs.agent().applySpec(sortSpec);
		}
	}

	private void applySummary(Container container, ConcreteContainer cs) {
		Summary summary = container.getSummary();
		if(summary!=null){
			AggrSpec aggrSpec = new AggrSpec();
			for(com.biswa.ep.deployment.util.Attribute attr : summary.getAttribute()){
				aggrSpec.add(new LeafAttribute(attr.getName()), Aggregators.valueOf(attr.getSummary()).AGGR);
			}
			cs.agent().applySpec(aggrSpec);
		}
	}

	private void applyPivot(Container container, ConcreteContainer cs) {
		Pivot pivot = container.getPivot();
		if(pivot!=null){
			List<Attribute> list = new ArrayList<Attribute>();
			for(com.biswa.ep.deployment.util.Attribute attr : pivot.getAttribute()){
				list.add(new LeafAttribute(attr.getName()));
			}
			PivotSpec pivotSpec = new PivotSpec(list.toArray(new Attribute[0]));
			cs.agent().applySpec(pivotSpec);
		}
	}
}
