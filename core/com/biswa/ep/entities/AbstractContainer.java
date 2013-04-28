package com.biswa.ep.entities;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.biswa.ep.ClientToken;
import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.identity.ConcreteIdentityGenerator;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.Spec;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.FeedbackAgent;
import com.biswa.ep.entities.transaction.TransactionEvent;
import com.biswa.ep.entities.transaction.TransactionRelay;
/**Abstract schema defines the most of the behavior of the event flow.
 * Manages the entity identity in one subsystem. manages the event flow  & propagation.
 * Key Responsibilities<br>
 * 1. Accept connection related activities.(Replay Attributes,Replay Container Entries,Add Client,Disconnect Client)<br>
 * 2. Dispatch all events to the sink containers.(Structural Event,Data Event)<br>
 * 3. Filter the entries, when filter is applied.(Filter the Inserts and Updates)<br>
 * 4. Generating Container Identity.<br>
 * @author biswa
 *
 */
abstract public class AbstractContainer implements ContainerListener,ConnectionListener,TransactionRelay,Dispatcher,PropertyConstants{
	final private ConcreteIdentityGenerator idGen = new ConcreteIdentityGenerator(); 
	/**
	 * Listeners listening this container
	 */
	final protected ListenerMap listenerMap = new ListenerMap();
	
	final class ListenerMap{
		final private HashMap<String,FilterAgent> hm = new HashMap<String,FilterAgent>();
		private FilterAgent[] listeners = new FilterAgent[0];
		
		
		public void put(String key, FilterAgent value) {
			hm.put(key, value);
			listeners = (FilterAgent[])hm.values().toArray(new FilterAgent[0]);
		}

		public void remove(Object key) {
			hm.remove(key);
			listeners = (FilterAgent[])hm.values().toArray(new FilterAgent[0]);
		}

		public FilterAgent[] values() {
			return listeners;
		}

		public FilterAgent get(String sink) {
			return hm.get(sink);
		}

		public boolean isEmpty() {
			return hm.isEmpty();
		}
	};

	/**
	 * Listeners listening this container
	 */
	final protected Map<String,FeedbackAgent> feedBackAgents = new  HashMap<String,FeedbackAgent>();
	
	/**
	 *Static storage for this container. 
	 */
	final protected Map<Attribute,Object> staticStorage = new HashMap<Attribute,Object>();
	
	/**
	 * Name of this container
	 */
	final private  String name;
	
	/**
	 * Dynamic container listener for this container. The thread which modifies all data structures in the 
	 * current container. No other thread should touch the internal data structures directly.
	 */ 
	final private Agent containerAgent;
	
	/**Thread which deals with downstream container dynamic listeners.  
	 * 
	 */
	final private Transmitter transmitter;
	
	/**
	 * Client token generator.
	 */
	final private ClientToken clientToken = new ClientToken();
	
	/**
	 * Configuration properties associated with this container.
	 */
	final private Properties props;
	
	/**
	 * Verbose messages from the current container when assertion is enabled.
	 */
	private boolean verbose = false;
	/**
	 * Whether this source is connected to its known sources. 
	 */
	private boolean connected;
	
	/**
	 *The filter applied at the container level  
	 */
	protected FilterSpec filterSpec=FilterSpec.TRUE;
		
	/**Class used to manage the listening agent and its filter and the container entries it passes.
	 * 
	 * @author biswa
	 *
	 */
	public class FilterAgent{
		public final String name;
		public final int primeIdentity;
		public final Agent agent;
		private FilterSpec filterSpec;
		FilterAgent(String sink,Agent agent){
			primeIdentity = clientToken.getToken();//Obtain client id
			this.agent=agent;
			this.name=sink;
		}
		public void setFilterSpec(FilterSpec filterSpec) {
			this.filterSpec = filterSpec;
		}
		public void refilter() {
			Attribute[] statelessAttributes = getStatelessAttributes();
			for(ContainerEntry containerEntry:getContainerEntries()){
				dispatchEntryAdded(this,containerEntry, statelessAttributes,true);
			}
		}
	}	
	/**Constructor with properties to configure the container. properties are
	 * strictly for the container configuration. Do not use it for any business
	 * information.
	 * @param name
	 * @param props
	 */
	public AbstractContainer(String name,Properties props){
		assert name!=null:"Name can not be null for the container";
		assert props!=null:"Properties can not be null for the container";
		this.name=name;
		this.props=props;
		String verboseStr = props.getProperty(PropertyConstants.VERBOSE);
		if(verboseStr!=null){
			this.verbose=Boolean.parseBoolean(verboseStr);
		}
		transmitter = new TransmitterImpl(name);
		containerAgent = new Agent(this);
	}
	
	@Override
	public void disconnect(ConnectionEvent connectionEvent) {
		assert isConnected():"How the hell did you reach here";
		//Always ensure the disconnected is played back to existing sinks.
		final FilterAgent dcl = listenerMap.get(connectionEvent.getSink());
		dispatchDisconnected(dcl.agent,connectionEvent);
		listenerMap.remove(connectionEvent.getSink());
		clientToken.releaseToken(dcl.primeIdentity);//Return the client id
	}

	private void dispatchDisconnected(final Agent dcl,final ConnectionEvent containerEvent){
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.disconnected(containerEvent);
			}
		});	
	}
	
	@Override
	public void connect(final ConnectionEvent connectionEvent) {
		assert isConnected():"How the hell did you reach here";
		final Agent dcl = connectionEvent.getAgent();
		//When an target container requests connection
		
		//1. Write the public attributes to the requesting container 
		for(Attribute attribute:getSubscribedAttributes()){
			if(attribute.propagate()){
				AbstractContainer.this.dispatchAttributeAdded(dcl,new LeafAttribute(attribute));
			}
		}
		//2. Send the connected event
		dispatchConnected(dcl,new ConnectionEvent(connectionEvent.getSource(),connectionEvent.getSink(),getKnownTransactionOrigins()));
		
		//3. Add the target container to the listener list
		listenerMap.put(connectionEvent.getSink(),buildFilterAgent(connectionEvent.getSink(),dcl));
		replay(connectionEvent);
	}
	
	/**Constructs filter agent for the container.
	 * 
	 * @param dcl Agent
	 * @return FilterAgent
	 */
	protected FilterAgent buildFilterAgent(final String sink,final Agent dcl) {
		return new FilterAgent(sink,dcl);
	}

	/**Is Any clients Attached to this container?
	 * 
	 * @return boolean
	 */
	protected boolean isClientsAttached() {
		return !listenerMap.isEmpty();
	}
	
	/**Returns the filter agent for this container.
	 * 
	 * @param sink String
	 * @return FilterAgent
	 */
	protected FilterAgent getFilterAgent(String sink) {
		return listenerMap.get(sink);
	}
	
	/**Returns the filter agent for this container.
	 * 
	 * @return FilterAgent[]
	 */
	protected FilterAgent[] getFilterAgents() {
		return listenerMap.values();
	}
	
	protected void dispatchConnected(final Agent dcl,final ConnectionEvent connectionEvent){
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.connected(connectionEvent);
			}
		});	
	}
	
	@Override
	public void replay(final ConnectionEvent connectionEvent) {
		assert isConnected():"How the hell did you reach here";
		final FilterAgent dcl = listenerMap.get(connectionEvent.getSink());
		FilterSpec incomingFilter = connectionEvent.getFilterSpec();
		if(incomingFilter!=null){
			incomingFilter = incomingFilter.prepare();
			dcl.filterSpec=filterSpec.chain(incomingFilter);
		}else{
			dcl.filterSpec=filterSpec;
		}
		//When an target container requests replay
		//Dispatch all the qualifying entry in the current container
		Attribute[] statelessAttributes = getStatelessAttributes();
		for(ContainerEntry conEntry:getContainerEntries()){
			conEntry.setFiltered(dcl.primeIdentity, false);
			AbstractContainer.this.dispatchEntryAdded(dcl,conEntry,statelessAttributes,false);
		}
	}
	
	@Override
	public void beginTran(){
		assert log("Begin Transaction: "+getCurrentTransactionID());
		dispatchBeginTransaction();
	}

	/**
	 * Method which delegates the begin transaction to the dispatcher thread.
	 * @param transactionID int
	 */
	protected void dispatchBeginTransaction(){
		for(FilterAgent dcl : listenerMap.values()){
			assert log("Dispatch Begin Transaction: "+getCurrentTransactionID() +" to "+dcl.primeIdentity);
			dispatchBeginTransaction(dcl.agent);
		}
	}
		
	private void dispatchBeginTransaction(final Agent dcl){
		final TransactionEvent te = new TransactionEvent(this.name,getCurrentTransactionOrigin(),getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.beginTran(te);
			}
		});
	}
	
	@Override
	public void commitTran(){
		assert log("Commit Transaction: "+getCurrentTransactionID());
		dispatchCommitTransaction();
		dispatchFeedback();
	}

	/**
	 * Method which delegates the commit transaction to the dispatcher thread.
	 */
	protected void dispatchCommitTransaction(){
		for(FilterAgent dcl : listenerMap.values()){
			assert log("Dispatch Commit Transaction: "+getCurrentTransactionID() +" to "+dcl.primeIdentity);
			dispatchCommitTransaction(dcl.agent);
		}
	}
		
	private void dispatchCommitTransaction(final Agent dcl){
		final TransactionEvent te = new TransactionEvent(this.name,getCurrentTransactionOrigin(),getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.commitTran(te);
			}
		});
	}
	@Override
	public void rollbackTran(){
		dispatchRollbackTransaction();
		dispatchFeedback();
	}

	/**
	 * Method which delegates the rollback transaction to the dispatcher thread.
	 */
	protected void dispatchRollbackTransaction(){
		for(FilterAgent dcl : listenerMap.values()){
			dispatchRollbackTransaction(dcl.agent);
		}
	}
		
	private void dispatchRollbackTransaction(final Agent dcl){
		final TransactionEvent te = new TransactionEvent(this.name,getCurrentTransactionOrigin(),getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.rollbackTran(te);
			}
		});
	}
	
	/**
	 * Dispatches feedback to interested upstream containers about the completion of transaction.
	 */
	protected void dispatchFeedback(){
		String currentOrigin = getCurrentTransactionOrigin();
		if(feedBackAgents.containsKey(currentOrigin)){
			feedbackDispatched(feedBackAgents.get(currentOrigin),getCurrentTransactionID());
		}
	}

	private void feedbackDispatched(final FeedbackAgent feedbackAgent,final int transactionID) {
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				feedbackAgent.completionFeedback(transactionID);
			}
		});
	}
	
	@Override
	public void addFeedbackAgent(final FeedbackAgent feedbackAgent){
		feedBackAgents.put(feedbackAgent.getFeedBackConsumer(), feedbackAgent);
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				feedbackAgent.addFeedbackSource();
			}
		});
	}
	
	@Override
	public void removeFeedbackAgent(FeedbackAgent feedbackAgent){
		feedBackAgents.remove(feedbackAgent.getFeedBackConsumer());
	}
	
	@Override
	public void dispatchAttributeAdded(Attribute requestedAttribute){
		if(requestedAttribute.propagate()){
			for(FilterAgent dcl : listenerMap.values()){
				dispatchAttributeAdded(dcl.agent,new LeafAttribute(requestedAttribute));
			}
		}
	}
		
	private void dispatchAttributeAdded(final Agent dcl,Attribute requestedAttribute){
		final ContainerEvent containerEvent = new ContainerStructureEvent(this.name,requestedAttribute);
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.attributeAdded(containerEvent);
			}
		});
	}
	
	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute){
		if(requestedAttribute.propagate()){
			requestedAttribute=new LeafAttribute(requestedAttribute);
			for(FilterAgent dcl : listenerMap.values()){
				dcl.refilter();
				dispatchAttributeRemoved(dcl.agent,requestedAttribute);
			}
		}
	}
	
	private void dispatchAttributeRemoved(final Agent dcl,Attribute requestedAttribute){
		final ContainerEvent containerEvent = new ContainerStructureEvent(this.name,requestedAttribute);
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.attributeRemoved(containerEvent);
			}
		});
	}
	
	@Override
	public void dispatchEntryAdded(ContainerEntry containerEntry){
		Attribute[] statelessAttributes = getStatelessAttributes();
		for(FilterAgent dcl : listenerMap.values()){
			dispatchEntryAdded(dcl,containerEntry, statelessAttributes,false);
		}
	}

	private void dispatchEntryAdded(final FilterAgent dcl,ContainerEntry containerEntry, Attribute[] statelessAttributes,boolean refiltered) {
		if(dcl.filterSpec.filter(containerEntry)){
			if(!refiltered || !containerEntry.isFiltered(dcl.primeIdentity)){
				//Filter marks this entry as qualifying to be propagated
				containerEntry.setFiltered(dcl.primeIdentity,true);
				//Propagate this entry to all listening containers
				dispatchFilteredEntryAdded(dcl.agent,containerEntry);
				//Perform stateless attribution only after the entry is update ready
				recomputeStatelessAttributes(dcl.agent,containerEntry,statelessAttributes);
			}
		}else{
			dispatchEntryRemoved(containerEntry, dcl);
		}
	}

	/**Prepares the record for state less processing.
	 * 
	 * @param containerEntry ContainerEntry
	 * @return StatelessContainerEntry
	 */
	protected StatelessContainerEntry prepareStatelessProcessing(
			ContainerEntry containerEntry) {
		StatelessContainerEntry slc = ContainerContext.SLC_ENTRY.get();
		slc.setStatelessContainerEntry(containerEntry);
		return slc;
	}

	/**Recomputes given set of state less attributes for the given receiver in the underlying
	 * record.
	 * @param receiver Agent
	 * @param containerEntry ContainerEntry
	 * @param statelessAttributes Attribute[]
	 */
	protected void recomputeStatelessAttributes(final Agent receiver,
			ContainerEntry containerEntry, Attribute[] statelessAttributes) {
		if(statelessAttributes.length>0){
			StatelessContainerEntry slc = prepareStatelessProcessing(containerEntry);
			for (Attribute notifiedAttribute : statelessAttributes) {
				Object substance = notifiedAttribute.failSafeEvaluate(notifiedAttribute, slc); 
				slc.silentUpdate(notifiedAttribute, substance);
				if(notifiedAttribute.propagate()){
					dispatchEntryUpdated(receiver,notifiedAttribute, substance, containerEntry);
				}
			}
		}
	}
	
	/**Performs state less attribution exclusively for the given receiver. This is performed
	 * when the triggered change is applicable only to the applicable receiver.
	 * @param receiver Agent
	 * @param containerEntry CotnainerEntry
	 */
	protected void performExclusiveStatelessAttribution(final Agent receiver,ContainerEntry containerEntry) {
		Collection<Attribute> statelessAttributes = ContainerContext.STATELESS_QUEUE.get(); 
		if(!statelessAttributes.isEmpty()){ 
			recomputeStatelessAttributes(receiver,containerEntry,statelessAttributes.toArray(Attribute.ZERO_DEPENDENCY));
			statelessAttributes.clear();
		}
	}
	
	/** This method performs state less attribution after an attribute update request is received.
	 * The attributed results are sent to all qualifying listeners.
	 * 
	 * @param containerEntry ContainerEntry
	 */
	protected void performPostUpdateStatelessAttribution(ContainerEntry containerEntry) {
		Collection<Attribute> statelessAttributes = ContainerContext.STATELESS_QUEUE.get();
		if(!listenerMap.isEmpty() && !statelessAttributes.isEmpty()){ 
			StatelessContainerEntry slcEntry = prepareStatelessProcessing(containerEntry);
			for (Attribute notifiedAttribute : statelessAttributes) {
				Object substance = notifiedAttribute.failSafeEvaluate(notifiedAttribute, slcEntry); 
				substance = slcEntry.silentUpdate(notifiedAttribute, substance);
				if(notifiedAttribute.propagate()){
					for(FilterAgent dcl : listenerMap.values()){
						if(containerEntry.isFiltered(dcl.primeIdentity)){
							//Not participating in filter direct dispatch
							dispatchEntryUpdated(dcl.agent,notifiedAttribute,substance,containerEntry);
						}
					}
				}
			}
		}
		statelessAttributes.clear();
	}
	
	private void dispatchFilteredEntryAdded(
			final Agent dcl, ContainerEntry containerEntry) {
		final ContainerEvent containerEvent = new ContainerInsertEvent(this.name,containerEntry.cloneConcrete(),getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.entryAdded(containerEvent);
			}
		});
	}
	
	@Override
	public void dispatchEntryRemoved(ContainerEntry containerEntry){
		for(FilterAgent dcl : listenerMap.values()){
			dispatchEntryRemoved(containerEntry, dcl);
		}
	}

	private void dispatchEntryRemoved(ContainerEntry containerEntry,
			FilterAgent dcl) {
		//Filter marks this entry as not qualifying to be propagated
		if(containerEntry.isFiltered(dcl.primeIdentity)){
			//This entry was earlier being propagated mark this 
			//not qualifying anymore
			containerEntry.setFiltered(dcl.primeIdentity,false);
			//Remove this entry from requesting containers			
			dispatchEntryRemoved(dcl.agent,containerEntry);
		}
	}
	
	private void dispatchEntryRemoved(final Agent dcl,ContainerEntry containerEntry){
		final ContainerEvent containerEvent = new ContainerDeleteEvent(this.name,containerEntry.getInternalIdentity(),getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.entryRemoved(containerEvent);
			}
		});
	}
	@Override
	public void dispatchEntryUpdated(Attribute attribute, Object substance, ContainerEntry containerEntry){
		if(attribute.propagate()){
			for(FilterAgent dcl : listenerMap.values()){
				if(dcl.filterSpec.filter(containerEntry)){
					//Filtered allowed this entry
					if(containerEntry.isFiltered(dcl.primeIdentity)){
						//This entry was being propagated before so just send the delta
						dispatchEntryUpdated(dcl.agent,attribute,substance,containerEntry);
					}else{
						//The entry was not propagated before mark this being filtered 
						containerEntry.setFiltered(dcl.primeIdentity,true);
						//send the entire entry
						dispatchFilteredEntryAdded(dcl.agent,containerEntry);
					}
				}else{
					dispatchEntryRemoved(containerEntry, dcl);
				}
			}
		}		
	}

	private void dispatchEntryUpdated(
			final Agent dcl, Attribute attribute,
			Object substance, ContainerEntry containerEntry) {
		final ContainerEvent containerEvent = new ContainerUpdateEvent(this.name,containerEntry.getInternalIdentity(),attribute,substance,getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.entryUpdated(containerEvent);
			}
		});
	}
	
	/**Returns all subscribed attributes in this container.
	 * 
	 * @return Attribute[]
	 */
	public abstract Attribute[] getSubscribedAttributes();

	/**Returns all stateless attributes in this container.
	 * 
	 * @return Attribute[]
	 */
	abstract protected Attribute[] getStatelessAttributes();
	
	/**Returns all static attributes in this container.
	 * 
	 * @return Attribute[]
	 */
	abstract protected Attribute[] getStaticAttributes();
	
	/**Returns all attribute names including transitively added attribute in this container.
	 * 
	 * @return String[]
	 */
	abstract protected String[] getAllAttributeNames();
	
	/**Obtains the attribute by name registered in this container
	 * 
	 * @param attributeName
	 * @return Attribute
	 */
	public abstract Attribute getAttributeByName(String attributeName);

	/** The entries of this container as visible from external world.
	 * 
	 * @return ContainerEntry[]
	 */
	public abstract ContainerEntry[] getContainerEntries();
	
	/** Number of entries this container has must always match to 
	 * <code>getContainerEntries().length</code>
	 * 
	 * @return int
	 */
	public abstract int getEntryCount();
	
	/**Method returns the concrete container entry.
	 * 
	 * @param id of the entry
	 * @return ContainerEntry
	 */
	public abstract ContainerEntry getConcreteEntry(int id);
	
	/**
	 * Name of the container.
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**Returns unique integer generated for this request.
	 * 
	 * @return int
	 */
	final public int generateIdentity() {
		return idGen.generateIdentity();
	}
	
	/**Gets the property for this propertyName.
	 * 
	 * @return String
	 */
	public String getProperty(String propertyName){
		return props.getProperty(propertyName);
	}
	/** Returns the dynamic container listener of this container.
	 * 
	 * @return Agent
	 */
	public Agent agent() {
		return containerAgent;
	}
	
	/**Returns the associated filter agent with this sink.
	 * 
	 * @param sinkName String
	 * @return FilterAgent
	 */
	public FilterAgent getFliterAgent(String sinkName){
		return listenerMap.get(sinkName);	
	}
	
	/**Returns the transmitter with this container.
	 * 
	 * @return Transmitter
	 */
	public Transmitter getEventDispatcher() {
		return transmitter;
	}

	/**Returns the current activities transaction id.
	 * 
	 * @return int
	 */
	@Override
	public int getCurrentTransactionID(){
		return containerAgent.getCurrentTransactionID();
	}
	
	@Override
	public String getCurrentTransactionOrigin(){
		return containerAgent.getCurrentTransactionOrigin();
	}
	
	@Override
	public String[] getKnownTransactionOrigins(){
		return containerAgent.getKnownTransactionOrigins();
	}
	
	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void connected(final ConnectionEvent connectionEvent) {
		connected=true;
	}

	@Override
	public void disconnected(final ConnectionEvent connectionEvent) {
		connected=false;
	}

	/**Returns the static substance associated with the container.
	 * 
	 * @param attribute
	 * @return Substance
	 */
	public Object getStatic(Attribute attribute) {
		return staticStorage.get(attribute);		
	}
	
	@Override
	abstract public void updateStatic(Attribute attribute,Object substance,FilterSpec appliedFilter);

	@Override
	final public void addSource(final ConnectionEvent connectionEvent){
		//No Operation as Agent will manage this.
		throw new UnsupportedOperationException("Add Source operation is managed by agent");
	}
	
	@Override
	final public void dropSource(final ConnectionEvent connectionEvent){
		//No Operation as Agent will manage this.
		throw new UnsupportedOperationException("Drop Source operation is managed by agent");
	}
	
	@Override
	public void applySpec(Spec spec){
		spec.apply(this);
	}

	@Override
	final public void invokeOperation(final ContainerTask task) {
		//No Operation as Agent will manage this.
		throw new UnsupportedOperationException("invokeOperation is managed by agent");		
	}
	
	/**This method allows subclasses to schedule tasks on agent
	 * in harmless manner.
	 * 
	 * @param task ContainerTask
	 * @param initial int
	 * @param delay int
	 * @param timeUnit TimeUnit
	 */
	public void invokePeriodically(final ContainerTask task,int initial,int delay,TimeUnit timeUnit) {
		agent().getEventCollector().scheduleWithFixedDelay(
		new Runnable() {
			@Override
			public void run() {					
				agent().invokeOperation(task);					
			}

		}, initial, delay, timeUnit);
	}
		
	/**Apply the source filter on the container.
	 * 
	 * @param filterSpec FilterSpec
	 */
	public void applyFilter(final FilterSpec filterSpec){
		filterSpec.prepare();
		if(getName().equals(filterSpec.getSinkName())){
			//Source Filter updated update the filter chains
			for(FilterAgent sinkAgent:listenerMap.values()){
				if(this.filterSpec!=sinkAgent.filterSpec){
					//If the sink has not provided any filter then the only filter in action is 
					//source filter so chain only when there is a sink filter
					sinkAgent.filterSpec=filterSpec.chain(sinkAgent.filterSpec);	
				}else{
					//Replace the filter as the source filter is only in action.
					sinkAgent.filterSpec=filterSpec;
				}
				sinkAgent.refilter();
			}
			//Update the source filter
			this.filterSpec = filterSpec;
		}else{
			//Sink Filter updated
			FilterAgent sinkAgent = listenerMap.get(filterSpec.getSinkName());
			sinkAgent.filterSpec=filterSpec.chain(sinkAgent.filterSpec);
			sinkAgent.refilter();
		}
	}
	
	/**Returns the concurrency support for this container
	 * 
	 * @return int concurrency level
	 */
	public int concurrencySupport(){
		String concurrent = getProperty(CONCURRENT);
		int concurrencyLevel = 0;
		if(concurrent!=null){
			concurrencyLevel = Integer.parseInt(concurrent);
		}
		return concurrencyLevel;
	}
	
	/**
	 * Returns the transaction timeout period for this container in milliseconds. defaults 0
	 * @return int
	 */
	public int getTimeOutPeriodInMillis(){
		String strTranTimeOut = getProperty(TRAN_TIME_OUT);
		int timeOut = 0;
		if(strTranTimeOut!=null){
			timeOut = Integer.parseInt(strTranTimeOut);
		}
		return timeOut;
	}
	
	/**
	 * Returns the begin on commit status of this container.
	 * defaulted to false
	 * @return boolean 
	 */
	public boolean beginOnCommit(){
		String beginOnCommit = getProperty(BEGIN_ON_COMMIT);
		boolean begCommit = false;
		if(beginOnCommit!=null){
			begCommit = Boolean.parseBoolean(beginOnCommit);
		}
		return begCommit;
	}	
	
	public boolean log(String str){
		if(verbose){
			verbose(str);
		}
		return true;
	}
	
	public void verbose(String str){
		System.out.println(Thread.currentThread().getName()+":"+str);
	}
	
	/**
	 *Dumps the current container contents. 
	 */
	public void dumpContainer(){
		verbose("##################Begin Dumping Container "+getName());
		ContainerEntry[] contEntries = getContainerEntries();
		for(ContainerEntry conEntry:contEntries){
			verbose(conEntry.toString());
		}
		verbose("##################Number of entries:="+contEntries.length);
		verbose("##################End Dumping Container "+getName());
	}
	
	/**
	 *Dumps the meta information for this container 
	 */
	public void dumpMetaInfo(){
		verbose("##################Begin Dumping Container "+getName());
		verbose(this.toString());
		verbose("##################Container Runtime Stats "+getName());
		verbose("Transaction in progress:"+agent().getCurrentTransactionID());
		verbose("Transaction origin:"+agent().getCurrentTransactionOrigin());
		verbose("Post Connected Queue Length:"+agent().getPostConnectedQueueSize());
		verbose("Pre Connected Queue Length:"+agent().getPreConnectedQueueSize());
		verbose("Known Transaction Origins:"+Arrays.toString(agent().getKnownTransactionOrigins()));
		verbose("Transaction Ready Queue:"+Arrays.toString(agent().getTransactionReadyQueue().toArray()));
		verbose("Operations In Transaction Queue:"+agent().getOpsInTransactionQueue());
		verbose("Connection Status:"+isConnected());
	}

	/**
	 * Is verbose enabled?
	 * @return boolean
	 */
	public boolean isVerbose() {
		return verbose;
	}
	/**
	 * Enables or disables verbose on this container.
	 * @param verbose boolean
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void destroy() {
		getEventDispatcher().destroy();		
	}
}