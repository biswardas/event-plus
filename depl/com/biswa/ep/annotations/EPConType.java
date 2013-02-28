package com.biswa.ep.annotations;

import com.biswa.ep.deployment.handler.DeploymentHandler;
import com.biswa.ep.deployment.handler.FeedbackDeploymentHandler;
import com.biswa.ep.deployment.handler.IDeployerHandler;
import com.biswa.ep.deployment.handler.JoinDeploymentHandler;
import com.biswa.ep.deployment.handler.PivotDeploymentHandler;
import com.biswa.ep.deployment.handler.ProxyDeploymentHandler;
import com.biswa.ep.deployment.handler.SplitDeploymentHandler;
import com.biswa.ep.deployment.handler.StaticDeploymentHandler;
import com.biswa.ep.deployment.handler.SubscriptionDeploymentHandler;
import com.biswa.ep.deployment.handler.TimedDeploymentHandler;

public enum EPConType {
	Basic(DeploymentHandler.class),
	Split(SplitDeploymentHandler.class), 
	Feedback(FeedbackDeploymentHandler.class,true), 
	Timed(TimedDeploymentHandler.class),
	Pivot(PivotDeploymentHandler.class), 
	Join(JoinDeploymentHandler.class),
	Subscription(SubscriptionDeploymentHandler.class,true),
	Proxy(ProxyDeploymentHandler.class),
	Static(StaticDeploymentHandler.class);
	IDeployerHandler handler = null;
	boolean feedback = false;

	EPConType(Class<? extends IDeployerHandler> handlerclass) {
		try {
			handler = handlerclass.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Could not initialize the deployer",e);
		}
	}
	EPConType(Class<? extends IDeployerHandler> handlerclass,boolean feedback) {
		try {
			handler = handlerclass.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Could not initialize the deployer",e);
		}
		this.feedback=feedback;
	}

	public IDeployerHandler getHandler() {
		return handler;
	}

	public boolean supportsFeedback() {
		return feedback;
	}
}
