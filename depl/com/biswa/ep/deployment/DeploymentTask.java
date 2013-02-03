package com.biswa.ep.deployment;

import java.util.List;

import com.biswa.ep.deployment.handler.ContainerDeployer;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.deployment.util.Publish;
import com.biswa.ep.entities.AbstractContainer;

final class DeploymentTask implements Runnable {
	private final Context context;
	private final ContainerManager containerManager;
	
	public DeploymentTask(Context context) {
		this.context = context;
		containerManager = new ContainerManager();
		containerManager.registerWithManagementServer(context);
	}
	
	public DeploymentTask(Context context,ContainerManager containerManager) {
		this.context = context;
		this.containerManager = containerManager;
	}
	
	public void run() {
		List<Container> containers = Deployer.getOrderedContainers(context);
		for (Container container : containers) {
			ContainerDeployer deployer = ContainerDeployer
					.valueOf(container.getType());
			String name = deployer.getHandler().getQualifiedName(container, context);
			AbstractContainer cs = null;
			if((cs = containerManager.getSchema(name))==null){
				regularDeploy(containerManager,context, container, deployer);						
			}else{
				graphRepair(container, deployer, cs);	
			}
		}
	}

	private void graphRepair(Container container,
			ContainerDeployer deployer, AbstractContainer cs) {
		//Set Expectation on the container if any
		deployer.getHandler().expectConnected(container,cs);
		//Listen to new friends
		for(Listen listen:container.getListen()){
			Accepter accepter = containerManager.valueOf(listen.getMethod());
			accepter.listen(listen, cs);
		}
		//Sets additional feedback
		for(Feedback feedback:container.getFeedback()){
			Accepter accepter = containerManager.valueOf(feedback.getMethod());
			accepter.addFeedbackSource(feedback, cs);	
		}
	}

	private void regularDeploy(ContainerManager containerManager, final Context context,
			Container container, ContainerDeployer deployer) {
		AbstractContainer cs = deployer.getHandler().deploy(
				container, context,containerManager);
		containerManager.register(cs);
		containerManager.registerWithManagementServer(context,cs);						
		for(Publish publish:container.getPublish()){
			Accepter accepter = containerManager.valueOf(publish.getMethod());
			accepter.publish(cs);
			containerManager.registerPublishProtocolHandler(cs,accepter);
		}
		
		for(Listen listen:container.getListen()){
			Accepter accepter = containerManager.valueOf(listen.getMethod());
			accepter.listen(listen, cs);
		}

		for(Feedback feedback:container.getFeedback()){
			Accepter accepter = containerManager.valueOf(feedback.getMethod());
			accepter.addFeedbackSource(feedback, cs);	
		}
	}
}