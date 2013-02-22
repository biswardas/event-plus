package com.biswa.ep.deployment.handler;

public enum ContainerDeployer {
	Simple(DeploymentHandler.class),
	Split(SplitDeploymentHandler.class), 
	Feedback(FeedbackDeploymentHandler.class,true), 
	Timed(TimedDeploymentHandler.class),
	Pivot(PivotDeploymentHandler.class), 
	Join(JoinDeploymentHandler.class),
	Subscription(SubscriptionDeploymentHandler.class,true),
	Proxy(ProxyDeploymentHandler.class),
	TProxy(TransactionDeploymentHandler.class),
	Static(StaticDeploymentHandler.class);
	IDeployerHandler handler = null;
	boolean feedback = false;

	ContainerDeployer(Class<? extends IDeployerHandler> handlerclass) {
		try {
			handler = handlerclass.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Could not initialize the deployer",e);
		}
	}
	ContainerDeployer(Class<? extends IDeployerHandler> handlerclass,boolean feedback) {
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
