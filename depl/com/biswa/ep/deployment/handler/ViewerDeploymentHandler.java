package com.biswa.ep.deployment.handler;

import javax.swing.SwingUtilities;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.util.GenericViewer;

public class ViewerDeploymentHandler extends DeploymentHandler {
	@Override
	public ConcreteContainer deploy(final Container container,final Context context,final ContainerManager containerManager) {
		Task task= new Task(container,context,containerManager);
		try {
			SwingUtilities.invokeAndWait(task);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return task.viewer;
	}
	class Task implements Runnable{
		final Container container;
		final Context context;
		final ContainerManager containerManager;
		GenericViewer viewer;
		public Task(final Container container,final Context context,final ContainerManager containerManager){
			this.container=container;
			this.context=context;
			this.containerManager=containerManager;
		}
		@Override
		public void run() {
			String name = getQualifiedName(container, context);
			viewer = new GenericViewer(name);
			for(Listen listen:container.getListen()){
				viewer.setSourceAgent(containerManager.getSchema(listen.getContext()+"."+listen.getContainer()).agent());				
			}
			ViewerDeploymentHandler.super.deploy(viewer, container, context, containerManager);
		}
	}
}
