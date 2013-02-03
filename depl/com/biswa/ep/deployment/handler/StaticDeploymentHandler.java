package com.biswa.ep.deployment.handler;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Attribute;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.StaticContainer;
import com.biswa.ep.entities.StaticLeafAttribute;

public class StaticDeploymentHandler extends  AbstractDeploymentHandler {

	@Override
	public AbstractContainer deploy(Container container, Context context,
			ContainerManager containerManager) {
		AbstractContainer cs = new StaticContainer(getQualifiedName(container, context),getProperties(container.getParam()));

		for(Attribute attribute:container.getAttribute()){
			try{
				Class<?> className = Class.forName(attribute.getClassName());
				com.biswa.ep.entities.Attribute schemaAttribute = null;
				if(attribute.getName()!=null){
					schemaAttribute = new StaticLeafAttribute(attribute.getName());
				}else{
					schemaAttribute = (com.biswa.ep.entities.Attribute) className.newInstance();
				}
				ContainerEvent ce = new ContainerStructureEvent(cs.getName(),schemaAttribute);
				cs.agent().attributeAdded(ce);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		expectConnected(container, cs);
		return cs;
	}
}
