package com.biswa.ep.deployment;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.ObjectName;
import javax.xml.bind.JAXBException;

import com.biswa.ep.annotations.EPPublish;
import com.biswa.ep.deployment.mbean.CSOperation;
import com.biswa.ep.deployment.mbean.CSOperationMBean;
import com.biswa.ep.deployment.mbean.ConMan;
import com.biswa.ep.deployment.mbean.ConManMBean;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.discovery.DiscProperties;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.transaction.FeedbackAgent;
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
		for(EPPublish conAccepter:EPPublish.values()){
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
			Deployer.deployer.execute(new DeploymentTask(context, this,false));
		} catch (JAXBException e) {
			e.printStackTrace();
		}		
	}
	
	public Accepter valueOf(String method){
		return accepter.get(method);
	}
	
	public AbstractContainer[] getAllContainers(){
		return nameContainerMap.values().toArray(new AbstractContainer[0]);
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
		
		for(AbstractContainer concContainer:this.getAllContainers()){
			concContainer.agent().destroy();
		}
		
		try {
			Deployer.mbs.unregisterMBean(containerManagementBean);
		} catch (Exception e) {
			e.printStackTrace();				
		}
		//accepter.clear();
		sourceMap.clear();
		nameContainerMap.clear();
		containerMbeanMap.clear();
		containerPublishProtocolMap.clear();
		connectionManager.clear();
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
	//TODO what about the dead container resurrects before the cleanup procedure is invoked
	final private ConnectionManager connectionManager = new ConnectionManager(); 
	private class ConnectionManager{		
		private class Circuit{
			private final AbstractContainer cs;
			private final Listen listen;
			Circuit(AbstractContainer cs,Listen listen){
				this.cs=cs;
				this.listen=listen;
			}
		}
		final private HashMap<String,Set<Circuit>>  circuitOpen = new HashMap<String,Set<Circuit>>();
		final private HashMap<String,Set<Circuit>>  circuitClosed = new HashMap<String,Set<Circuit>>();
		private class Acknowledge{
			private final AbstractContainer cs;
			private final Feedback feedback;
			Acknowledge(AbstractContainer cs,Feedback feedback){
				this.cs=cs;
				this.feedback=feedback;
			}
		}
		final private HashMap<String,Set<Acknowledge>>  pendingFeedback = new HashMap<String,Set<Acknowledge>>();
		final private HashMap<String,Set<Acknowledge>>  existingFeedback = new HashMap<String,Set<Acknowledge>>();
		
		public void clear(){
			circuitClosed.clear();
			circuitOpen.clear();
			pendingFeedback.clear();
			existingFeedback.clear();
		}
		
		public void containerDeployed(String sourceName) {
			if(circuitOpen.containsKey(sourceName)){
				Iterator<Circuit> iter = circuitOpen.get(sourceName).iterator(); 
				while(iter.hasNext()){
					Circuit pc = iter.next();
					if(valueOf(pc.listen.getMethod()).listen(pc.listen, pc.cs)){
						addToCircuitMap(circuitClosed, pc.listen, pc.cs);
						iter.remove();
					}
				}
			}
			if(pendingFeedback.containsKey(sourceName)){
				Iterator<Acknowledge> iter = pendingFeedback.get(sourceName).iterator(); 
				while(iter.hasNext()){
					Acknowledge pc = iter.next();
					if(valueOf(pc.feedback.getMethod()).addFeedbackSource(pc.feedback, pc.cs)){
						addToFeedbackTracking(existingFeedback,pc.feedback, pc.cs);	
						iter.remove();
					}
				}
			}
		}
	
		public void containerDestroyed(final String oneDeadContainer) {
			if(existingFeedback.containsKey(oneDeadContainer)){
				Iterator<Acknowledge> iter = existingFeedback.get(oneDeadContainer).iterator(); 
				while(iter.hasNext()){
					Acknowledge pc = iter.next();
					//Send a disconnected message to the applicable container
					pc.cs.agent().disconnected(new ConnectionEvent(oneDeadContainer, pc.cs.agent().getName()));
					//Remove any stale feedback agents..
					pc.cs.agent().removeFeedbackAgent(new FeedbackAgent(){
						@Override
						public void completionFeedback(int transactionId) {}
						@Override
						public void addFeedbackSource() {}	
						@Override
						public String getFeedBackConsumer() {
							return oneDeadContainer;
						}				
					});
					addToFeedbackTracking(pendingFeedback,pc.feedback, pc.cs);	
					iter.remove();
				}
			}
			if(circuitClosed.containsKey(oneDeadContainer)){
				Iterator<Circuit> iter = circuitClosed.get(oneDeadContainer).iterator(); 
				while(iter.hasNext()){
					Circuit pc = iter.next();	
					if(Deployer.isSlave()){
						releaseSlaveInstance();
					}else{
						if(DiscProperties.SLAVE_SIDE.equals(pc.listen.getSide())){
							//If we are listening this container as a slave then drop it as source
							pc.cs.agent().dropSource(new ConnectionEvent(oneDeadContainer, pc.cs.getName()));
						}else{
							//Else add it to open circuit map
							addToCircuitMap(circuitOpen, pc.listen, pc.cs);							
						}
					}
					//Remove it from closed circuit map..
					iter.remove();
				}
			}
			
			//TODO till we figure out the best way to redistribute
			for(AbstractContainer abs:getAllContainers()){
				healthCheck(abs);
			}
		}

		protected void releaseSlaveInstance() {
			//Release any resources
			destroyAllContainers();
			//Return to registry
			try {
				Deployer.registerWithDiscovery(true);
			} catch (RemoteException e) {
				Deployer.asynchronouslyShutDown();
			}
		}

		private void healthCheck(final AbstractContainer abs) {
			abs.agent().invokeOperation(new ContainerTask() {				
				/**
				 * 
				 */
				private static final long serialVersionUID = -7020229819863137742L;
	
				@Override
				protected void runtask() throws Throwable {
					abs.agent().beginDefaultTran();
					abs.agent().commitDefaultTran();					
				}
			});
		}
		
		public void listenAsynchronously(Listen listen, AbstractContainer cs) {
			Accepter accepter = valueOf(listen.getMethod());
			if(!accepter.listen(listen, cs)){
				addToCircuitMap(circuitOpen,listen,cs);
			}else{
				addToCircuitMap(circuitClosed,listen,cs);
			}
		}
		
		private void addToCircuitMap(HashMap<String,Set<Circuit>> circuitMap,Listen listen, AbstractContainer cs){
			String name = Accepter.buildName(listen);
			Set<Circuit> awaitingConnection = circuitMap.get(name);
			if(awaitingConnection==null){
				awaitingConnection = new HashSet<Circuit>();
				circuitMap.put(name, awaitingConnection);
			}
			awaitingConnection.add(new Circuit(cs, listen));
		}
		
		public void addFeedbackSourceAsynchronously(Feedback feedback,
				AbstractContainer cs) {
			Accepter accepter = valueOf(feedback.getMethod());
			if(!accepter.addFeedbackSource(feedback, cs)){
				addToFeedbackTracking(pendingFeedback,feedback, cs);	
			}else{
				addToFeedbackTracking(existingFeedback,feedback, cs);	
			}
		}
	
		protected void addToFeedbackTracking(HashMap<String,Set<Acknowledge>>  feedbackMap,Feedback feedback, AbstractContainer cs) {
			String name = Accepter.buildName(feedback);
			Set<Acknowledge> awaitingFeedback = feedbackMap.get(name);
			if(awaitingFeedback==null){
				awaitingFeedback = new HashSet<Acknowledge>();
				feedbackMap.put(name, awaitingFeedback);
			}
			awaitingFeedback.add(new Acknowledge(cs, feedback));
		}
	}
	final public void addFeedbackSourceAsynchronously(Feedback feedback,
			AbstractContainer cs) {
		connectionManager.addFeedbackSourceAsynchronously(feedback, cs);		
	}

	final public void containerDeployed(String sourceName) {
		connectionManager.containerDeployed(sourceName);		
	}

	final public void containerDestroyed(String sourceName) {
		connectionManager.containerDestroyed(sourceName);		
	}

	final public void listenAsynchronously(Listen listen, AbstractContainer cs) {
		connectionManager.listenAsynchronously(listen, cs);		
	}
}
