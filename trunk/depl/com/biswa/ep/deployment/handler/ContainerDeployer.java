package com.biswa.ep.deployment.handler;

public enum ContainerDeployer {
	Simple(DeploymentHandler.class),
	Split(SplitDeploymentHandler.class), 
	Feedback(FeedbackDeploymentHandler.class), 
	Timed(TimedDeploymentHandler.class),
	Pivot(PivotDeploymentHandler.class), 
	Join(JoinDeploymentHandler.class),
	Channel(ChannelDeploymentHandler.class),
	Proxy(ProxyDeploymentHandler.class),
	TProxy(TransactionDeploymentHandler.class),
	Static(StaticDeploymentHandler.class);
	IDeployerHandler handler = null;

	ContainerDeployer(Class<? extends IDeployerHandler> handlerclass) {
		try {
			handler = handlerclass.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Could not initialize the deployer",e);
		}
	}

	public IDeployerHandler getHandler() {
		return handler;
	}
}
