package com.biswa.ep.deployment.handler;

import java.util.Properties;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Attribute;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Filter;
import com.biswa.ep.deployment.util.Handler;
import com.biswa.ep.deployment.util.Source;
import com.biswa.ep.deployment.util.Subscribe;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.predicate.Predicate;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.transaction.Inlet;
import com.biswa.ep.entities.transaction.SubscriptionAgent;
import com.biswa.ep.subscription.SubscriptionAttrHandler;
import com.biswa.ep.subscription.SubscriptionAttrHandlerImpl;
import com.biswa.ep.subscription.SubscriptionAttribute;
import com.biswa.ep.util.parser.predicate.PredicateBuilder;

public abstract class DeploymentHandler extends AbstractDeploymentHandler{
	public void deploy(ConcreteContainer cs, Container container,Context context,ContainerManager containerManager) {
		deployCommon(container, cs,containerManager);
		expectConnected(container, cs);
		attachSources(container, cs,containerManager);
	}

	protected void deployCommon(Container container,
			ConcreteContainer cs,ContainerManager containerManager) {
		applyFilter(container, cs);
		addAttributes(container, cs);
		addSubscriptions(container,cs,containerManager);
	}
	
	protected void addSubscriptions(Container container, ConcreteContainer cs,ContainerManager containerManager) {
		for(Subscribe subscribe:container.getSubscribe()){
			Accepter accepter = containerManager.valueOf(subscribe.getMethod());			
			SubscriptionAgent subAgent = accepter.getSubscriptionAgent(subscribe.getContext(), subscribe.getContainer());
			Handler handler = subscribe.getHandler();
			SubscriptionAttrHandler attrSubs = null;
			if(handler!=null){
				try {
					attrSubs = (SubscriptionAttrHandler) Class.forName(handler.getClassName()).newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else{
				attrSubs = new SubscriptionAttrHandlerImpl();
			}
			SubscriptionAttribute subsAttribute = new SubscriptionAttribute(subscribe.getDepends(),subscribe.getResponse(),subscribe.getContext()+"."+subscribe.getContainer(),subAgent,attrSubs);
			ContainerEvent ce = new ContainerStructureEvent(cs.getName(),subsAttribute);
			cs.agent().attributeAdded(ce);
		}
	}
	protected void attachSources(Container container, ConcreteContainer cs, ContainerManager containerManager) {
		for(Source source:container.getSource()){
			Properties props = getProperties(source.getParam());
			try {
				Inlet oneInlet = (Inlet) Class.forName(source.getClassName()).newInstance();
				oneInlet.setAgent(cs.agent(),props);
				oneInlet.init();
				containerManager.registerSource(cs,oneInlet);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	protected void applyFilter(Container container, ConcreteContainer cs) {
		Filter filter = container.getFilter();
		if(filter != null){
			Predicate pred = PredicateBuilder.buildPredicate(filter.getPredicate());
			cs.agent().applySpec(new FilterSpec(pred));
		}
	}
	protected void addAttributes(Container container,
			ConcreteContainer cs) {
		for(Attribute attribute:container.getAttribute()){
			try{
				Class<?> className = Class.forName(attribute.getClassName());
				com.biswa.ep.entities.Attribute schemaAttribute = null;
				if(attribute.getExpression()!=null){
					schemaAttribute = (com.biswa.ep.entities.Attribute) className.getConstructor(String.class).newInstance(attribute.getExpression());
				}else if(attribute.getName()!=null){
					schemaAttribute = (com.biswa.ep.entities.Attribute) className.getConstructor(String.class).newInstance(attribute.getName());
				}else{
					schemaAttribute = (com.biswa.ep.entities.Attribute) className.newInstance();
				}
				for(Attribute dependency:attribute.getAttribute()){
					schemaAttribute.addDependency(new LeafAttribute(dependency.getName()));
				}
				ContainerEvent ce = new ContainerStructureEvent(cs.getName(),schemaAttribute);
				cs.agent().attributeAdded(ce);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}
}
