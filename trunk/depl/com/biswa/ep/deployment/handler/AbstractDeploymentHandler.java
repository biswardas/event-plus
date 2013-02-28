package com.biswa.ep.deployment.handler;

import java.util.List;
import java.util.Properties;

import com.biswa.ep.EPEvent;
import com.biswa.ep.deployment.Deployer;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.deployment.util.Param;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConnectionEvent;

public abstract class AbstractDeploymentHandler implements IDeployerHandler {
	public String getQualifiedName(Container container, Context context) {
		return context.getName()+"."+container.getName();
	}

	@Override
	public void expectConnected(Container container,
			AbstractContainer cs) {
		if(container.getListen().size()==0){
			cs.agent().addSource(new ConnectionEvent(EPEvent.DEF_SRC,EPEvent.DEF_SRC));
			cs.agent().connected(new ConnectionEvent(EPEvent.DEF_SRC,EPEvent.DEF_SRC));
		}else{
			for(Listen listen:container.getListen()){
				cs.agent().addSource(new ConnectionEvent(listen.getContext()+"."+listen.getContainer(),cs.getName()));	
			}
		}
	}

	protected Properties getProperties(List<Param> params) {
		return Deployer.getProperties(params);
	}
}
