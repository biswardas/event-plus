package com.biswa.ep.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.biswa.ep.NamedThreadFactory;
import com.biswa.ep.UncaughtExceptionHandler;
import com.biswa.ep.deployment.mbean.ConMan;
import com.biswa.ep.deployment.mbean.ConManMBean;
import com.biswa.ep.deployment.util.Container;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.deployment.util.Param;

public class Deployer extends UncaughtExceptionHandler{
	private static final String MANUAL = "manual";

	private static final String DEPLOYMENT_ORDER = "deployment.order";

	private static final String DEPLOYMENT_DESC = "deployment.desc";

	final static ExecutorService deployer = Executors
			.newSingleThreadExecutor(new NamedThreadFactory("Deployer",false));

	//public final static ContainerManager containerManager = new ContainerManager();
	final static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	
	public static void main(String[] args) throws JAXBException {
		//Register this with mbean server
		ConManMBean csMbean = new ConMan();
		try {
			Deployer.mbs.registerMBean(csMbean, new ObjectName("RootDeployer:name=Root"));
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Error deploying root deployer with JMX");
		}
		
		String fileName = System.getProperty(DEPLOYMENT_DESC);
		if(fileName!=null){
			deploy(fileName);
		}else{
			deployer.execute(new Runnable() {				
				@Override
				public void run() {
					System.err.println("No deployment descripter found standing by. (-Ddeployment.desc=$fileName)");					
				}
			});						
		}
	}


	public static void deploy(String fileName) throws JAXBException {
		System.out.println("Attempting to deploy "+fileName);
		Context context = buildContext(fileName);
		deploy(context);
		System.out.println(fileName+" deployed.");
	}
	
	
	@SuppressWarnings("unchecked")
	public static Context buildContext(String fileName) throws JAXBException{
		JAXBContext jc = JAXBContext
				.newInstance("com.biswa.ep.deployment.util");

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		InputStream ins = null;
		JAXBElement<Context> rootObj = null;
		try{
			File file = new File(fileName);
			if(file.exists()){
				ins = new FileInputStream(file);
			}else{
				ins = ClassLoader.getSystemResourceAsStream(fileName);
			}
			rootObj = (JAXBElement<Context>) unmarshaller
					.unmarshal(ins);
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			try {
				ins.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return (Context) rootObj.getValue();
	}

	public static void deploy(final Context context) {
		deployer.execute(new DeploymentTask(context));
	}


	static List<Container> getOrderedContainers(final Context context) {
		Properties props = getProperties(context.getParam());
		List<Container> containers = context.getContainer();
		if(MANUAL.equals(props.getProperty(DEPLOYMENT_ORDER))){
			return manualOrdering(containers);
		}
		return autoOrdering(context, containers);
	}

	private static List<Container> manualOrdering(List<Container> containers) {
		Collections.sort(containers, new Comparator<Container>() {
			@Override
			public int compare(Container o1, Container o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
		return containers;
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
				AtomicInteger nested = null;
				if(listen.getContext().equals(context.getName())){
					String dependsOn = listen.getContainer();
					if((nested=containerDependencyMap.get(dependsOn))==null){
						for(Container searchedContainer: context.getContainer()){
							if(searchedContainer.getName().equals(dependsOn)){
								addContainer(containerDependencyMap,searchedContainer,context);
								nested=containerDependencyMap.get(searchedContainer);
								break;
							}
						}
					}
					atomInt.addAndGet(nested.get());
				}
			}
			containerDependencyMap.put(container, atomInt);
		}
	}
	
	public static Properties getProperties(List<Param> params) {
		Properties props = new Properties();
		for(Param oneParam:params){
			props.put(oneParam.getName(), oneParam.getValue());
		}
		return props;
	}
}
