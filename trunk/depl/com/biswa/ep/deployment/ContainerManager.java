package com.biswa.ep.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.ObjectName;
import javax.xml.bind.JAXBException;

import com.biswa.ep.deployment.mbean.CSOperation;
import com.biswa.ep.deployment.mbean.CSOperationMBean;
import com.biswa.ep.deployment.mbean.ConMan;
import com.biswa.ep.deployment.mbean.ConManMBean;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.transaction.Inlet;

public class ContainerManager {
	private Map<String,Accepter> accepter = new HashMap<String,Accepter>();
	private Map<AbstractContainer,Inlet> sourceMap = new HashMap<AbstractContainer,Inlet>();
	private Map<String,AbstractContainer> nameContainerMap = new HashMap<String,AbstractContainer>();
	private Map<AbstractContainer,ObjectName> containerMbeanMap = new HashMap<AbstractContainer,ObjectName>();
	private Map<AbstractContainer,Accepter> containerPublishProtocolMap = new HashMap<AbstractContainer,Accepter>();
	private ObjectName containerManagementBean = null;  
	
	
	public ContainerManager(){
		//Load protocol handlers
		for(ContainerAccepter conAccepter:ContainerAccepter.values()){
			try {
				Accepter handler = (Accepter) conAccepter.getHandler().getConstructors()[0].newInstance(this);
				this.accepter.put(conAccepter.name(),handler);				
			} catch (Throwable e) {
				throw new RuntimeException("Could not initialize the Publisher",e);
			}
		}
	}
	
	public void register(AbstractContainer cs){
		nameContainerMap.put(cs.getName(),cs);
	}
	
	public AbstractContainer getSchema(String source) {
		return nameContainerMap.get(source);
	}
	
	public void mergeGraph(String fileName) {
		try {
			Context context = Deployer.buildContext(fileName);
			Deployer.deployer.execute(new DeploymentTask(context, this));
		} catch (JAXBException e) {
			e.printStackTrace();
		}		
	}
	
	public Accepter valueOf(String method){
		return accepter.get(method);
	}
	
	public ConcreteContainer[] getAllContainers(){
		return nameContainerMap.values().toArray(new ConcreteContainer[0]);
	}
	
	public void destroyAllContainers(){
		for(Entry<AbstractContainer, Inlet> oneEntry:sourceMap.entrySet()){
			try {
				oneEntry.getValue().terminate();
			} catch (Exception e) {
				e.printStackTrace();				
			}
		}
		
		for(Entry<AbstractContainer, Accepter> oneEntry:containerPublishProtocolMap.entrySet()){
			try {
				oneEntry.getValue().unpublish(oneEntry.getKey());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for(ObjectName jmxObject:containerMbeanMap.values()){
			try {
				Deployer.mbs.unregisterMBean(jmxObject);
			} catch (Exception e) {
				e.printStackTrace();		
			}
		}
		
		for(ConcreteContainer concContainer:this.getAllContainers()){
			concContainer.agent().destroy();
		}
		try {
			Deployer.mbs.unregisterMBean(containerManagementBean);
		} catch (Exception e) {
			e.printStackTrace();				
		}
		accepter.clear();
		sourceMap.clear();
		nameContainerMap.clear();
		containerMbeanMap.clear();
		containerPublishProtocolMap.clear();
		containerManagementBean=null;
	}

	public void registerWithManagementServer(Context context, AbstractContainer cs) {
		CSOperationMBean csMbean = new CSOperation(cs);
		try {
			ObjectName objectName =  new ObjectName(context.getName(),cs.getName(),cs.getName());
			Deployer.mbs.registerMBean(csMbean, objectName);
			containerMbeanMap.put(cs, objectName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void registerWithManagementServer(Context context) {
		//Register this with mbean server
		ConManMBean csMbean = new ConMan(this);
		try {
			containerManagementBean = new ObjectName(context.getName(),"ContainerSchema","ContextManager");
			Deployer.mbs.registerMBean(csMbean, containerManagementBean);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void registerPublishProtocolHandler(AbstractContainer cs,Accepter accepter) {	
		containerPublishProtocolMap.put(cs,accepter);
	}

	public void registerSource(ConcreteContainer cs, Inlet oneInlet) {	
		sourceMap.put(cs,oneInlet);		
	}
}
