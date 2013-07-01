package com.biswa.ep.annotations;

import com.biswa.ep.deployment.handler.BasicDeploymentHandler;
import com.biswa.ep.deployment.handler.FeedbackDeploymentHandler;
import com.biswa.ep.deployment.handler.ForkJoinDeploymentHandler;
import com.biswa.ep.deployment.handler.IDeployerHandler;
import com.biswa.ep.deployment.handler.JoinDeploymentHandler;
import com.biswa.ep.deployment.handler.PivotDeploymentHandler;
import com.biswa.ep.deployment.handler.ProxyDeploymentHandler;
import com.biswa.ep.deployment.handler.SplitDeploymentHandler;
import com.biswa.ep.deployment.handler.StaticDeploymentHandler;
import com.biswa.ep.deployment.handler.SubscriptionDeploymentHandler;
import com.biswa.ep.deployment.handler.TimedDeploymentHandler;
import com.biswa.ep.deployment.handler.ViewerDeploymentHandler;

public enum EPConType {
	Basic(BasicDeploymentHandler.class),
	Split(SplitDeploymentHandler.class), 
	ForkJoin(ForkJoinDeploymentHandler.class){
		@Override
		public boolean handleInheritancce(EPConType type) {
			switch(type){
			case Split:return true;
			case Static:return true;
			default:
				throw new RuntimeException("ForkJoin containers can only inherit Split,Static containers.");
			}
		}
	},
	Feedback(FeedbackDeploymentHandler.class,true){
		public boolean handleAttribute(EPAttrType type) {
			switch(type){
				case Stateless:
					throw new RuntimeException("Feedback containers can not handle Stateless Attributes.");
				default: return true;
			}
		}
	},
	Timed(TimedDeploymentHandler.class){
		public boolean handleAttribute(EPAttrType type) {
			switch(type){
				case Stateless:
					throw new RuntimeException("Timed containers can only inherit Split,Static containers.");
				default: return true;
			}
		}
	},
	Pivot(PivotDeploymentHandler.class), 
	Join(JoinDeploymentHandler.class),
	Subscription(SubscriptionDeploymentHandler.class,true){
		public boolean handleAttribute(EPAttrType type) {
			switch(type){
				case Stateless:
					throw new RuntimeException("Subscription containers can only inherit Split,Static containers.");
				default: return true;
			}
		}
	},
	Proxy(ProxyDeploymentHandler.class),
	Static(StaticDeploymentHandler.class),
	Viewer(ViewerDeploymentHandler.class);
	IDeployerHandler handler = null;
	boolean feedback = false;

	EPConType() {
	}

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

	public boolean handleInheritancce(EPConType type) {
		return true;
	}
	
	public boolean handleAttribute(EPAttrType type) {
		return true;
	}
}
