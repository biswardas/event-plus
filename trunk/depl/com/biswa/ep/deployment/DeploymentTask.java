package com.biswa.ep.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.biswa.ep.annotations.EPConType;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.deployment.util.Publish;
import com.biswa.ep.entities.AbstractContainer;

final class DeploymentTask implements Runnable {
	private final Context context;
	private final ContainerManager containerManager;
	private boolean sorted = false;
	
	public DeploymentTask(Context context,ContainerManager containerManager,boolean sorted) {
		this.context = context;
		this.containerManager = containerManager;
		this.sorted=sorted;
	}
	
	public void run() {
		List<Container> containers = sorted?context.getContainer():getOrderedContainers(context);
		for (Container container : containers) {
			EPConType deployer = EPConType
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
			EPConType deployer, AbstractContainer cs) {
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
			Container container, EPConType deployer) {
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
	
	private static List<Container> getOrderedContainers(final Context context) {
		List<Container> containers = context.getContainer();
		return autoOrdering(context, containers);
	}

	private static List<Container> autoOrdering(final Context context,
			List<Container> containers) {
		HashMap<Container,AtomicInteger> containerDependencyMap = new HashMap<Container,AtomicInteger>();
		for(Container container:containers){
			addContainer(containerDependencyMap, container,context);
		}
		List<Map.Entry<Container,AtomicInteger>> sortedContainer = new ArrayList<Map.Entry<Container,AtomicInteger>>();
		sortedContainer.addAll(containerDependencyMap.entrySet());
		Collections.sort(sortedContainer, new Comparator<Map.Entry<Container,AtomicInteger>>() {
			@Override
			public int compare(Map.Entry<Container,AtomicInteger> o1, Map.Entry<Container,AtomicInteger> o2) {
				return o1.getValue().get() - o2.getValue().get();
			}
		});
		containers.clear();
		for(Map.Entry<Container, AtomicInteger> entry:sortedContainer){
			containers.add(entry.getKey());
		}
		return containers;
	}

	private static void addContainer(
			HashMap<Container, AtomicInteger> containerDependencyMap,
			Container container,final Context context) {
		if(!containerDependencyMap.containsKey(container)){
			AtomicInteger atomInt = new AtomicInteger(1);
			for(Listen listen:container.getListen()){
				if(listen.getContext().equals(context.getName())){
					AtomicInteger nested = null;
					String dependsOn = listen.getContainer();
					if((nested=getNestedWeight(containerDependencyMap, dependsOn))==null){
						for(Container searchedContainer: context.getContainer()){
							if(searchedContainer.getName().equals(dependsOn)){
								addContainer(containerDependencyMap,searchedContainer,context);
								nested=containerDependencyMap.get(searchedContainer);
								break;
							}
						}
						if(nested==null){
							throw new RuntimeException("Unresolved container:"+dependsOn);
						}
					}
					atomInt.addAndGet(nested.get());
				}
			}
			containerDependencyMap.put(container, atomInt);
		}
	}


	private static AtomicInteger getNestedWeight(
			HashMap<Container, AtomicInteger> containerDependencyMap,
			String dependsOn) {
		for(Entry<Container,AtomicInteger> oneEntry: containerDependencyMap.entrySet()){
			if(oneEntry.getKey().getName().equals(dependsOn)){
				return oneEntry.getValue();
			}
		}
		return null;
	}
}