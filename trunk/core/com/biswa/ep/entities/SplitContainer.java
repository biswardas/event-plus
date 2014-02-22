package com.biswa.ep.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.transaction.Agent;
/**
 * Container which does the load balancing of the entities. Useful when you want to
 * perform symmetric multiprocessing on the records. When one client quits the container
 * will automatically re balance the entries to remaining containers.
 * @author biswa
 *
 */
public class SplitContainer extends ConcreteContainer {
	
	final class SplitFilterAgent extends FilterAgent{
		private int count;
		private SplitFilterAgent(String sink,Agent agent) {
			super(sink,agent);
		}
		private int incrementCount() {
			return ++count;
		}
		private int decrementCount() {
			return --count;
		}
		private int getCount() {
			return count;
		}
		private int reset() {
			return count=0;
		}		
	}
	final int max_per_client;
	/**Constructor to build a Split container.
	 * 
	 * @param name
	 * @param props
	 */
	public SplitContainer(String name,Properties props) {
		super(name,props);
		String max_per_client_config = props.getProperty(PropertyConstants.MAX_PER_CLIENT);
		if(max_per_client_config!=null){
			max_per_client = Integer.parseInt(max_per_client_config);
		}else{
			max_per_client = Integer.MAX_VALUE;
		}
	}
	
	@Override
	public void dispatchEntryAdded(ContainerEntry containerEntry) {
		SplitFilterAgent toClient = getOneFreeAgent();
		if(toClient!=null){
			sendEntryToNewClient(toClient, containerEntry);
		}
	}

	@Override
	final protected SplitFilterAgent buildFilterAgent(String sink,Agent dcl) {
		return new SplitFilterAgent(sink,dcl);
	}

	@Override
	final protected SplitFilterAgent getFilterAgent(String sink) {
		return (SplitFilterAgent) super.getFilterAgent(sink);
	}

	@Override
	final protected SplitFilterAgent[] getFilterAgents() {
		FilterAgent[] sourceAgents = super.getFilterAgents();
		SplitFilterAgent[] spFilterAgent = new SplitFilterAgent[sourceAgents.length];
		System.arraycopy(sourceAgents, 0,spFilterAgent,0,sourceAgents.length);
		return spFilterAgent;
	}
	
	final private SplitFilterAgent getOneFreeAgent(){
		return getOneFreeAgent(max_per_client);
	}
	
	final private SplitFilterAgent getOneFreeAgent(int capacity){
		SplitFilterAgent toClient = null;
		SplitFilterAgent[] clients = getAgentsWithCapacity(capacity);
		int numOfClients = clients.length;
		if(clients.length>0){
			//Assign the entry to client
			toClient=clients[(int) ((Math.random())*numOfClients)];
		}
		return toClient;
	}
	
	final private SplitFilterAgent[] getAgentsWithCapacity(int capacity) {
		SplitFilterAgent[] freeAgents;
		if(capacity==Integer.MAX_VALUE){
			freeAgents = getFilterAgents();
		}else{
			ArrayList<SplitFilterAgent> withCapacity = new ArrayList<SplitFilterAgent>();
			for(SplitFilterAgent oneAgent:getFilterAgents()){
				if(oneAgent.getCount()<capacity){
					withCapacity.add(oneAgent);
				}
			}
			freeAgents = withCapacity.toArray(new SplitFilterAgent[0]); 
		}
		return freeAgents;
	}
	
	@Override
	public void replay(final ConnectionEvent connectionEvent) {
		assert isConnected():"How the hell did you reach here";
		final SplitFilterAgent dcl = getFilterAgent(connectionEvent.getSink());
		dcl.reset();
		FilterSpec agentFilter = new FilterSpec(null,null){
			/**
			 * 
			 */
			private static final long serialVersionUID = 167033831084818056L;

			@Override
			public boolean filter(ContainerEntry containerEntry) {
				if(dcl.primeIdentity==containerEntry.getToClient()){
					return true;
				}else{
					return false;
				}
			}			
		};
		dcl.setFilterSpec(filterSpec.chain(agentFilter));

		ContainerEntry[] conEntries = getLogicalEntries();
		//First allocate the unallocated / if the old client requested replay
		for(ContainerEntry conEntry:conEntries){
			if(conEntry.getToClient()==0){
				//This entry was never allocated so just send it
				sendEntryToNewClient(dcl, conEntry);
				if(dcl.getCount()==max_per_client){
					//Already allocated enough
					return;
				}
			}else if(conEntry.getToClient()==dcl.primeIdentity){
				//Remove the entry
				dispatchEntryRemoved(conEntry);
				//Resend it
				sendEntryToNewClient(dcl, conEntry);
			}
		}
		
		rebalance();
	}

	protected void rebalance() {
		//When an target container requests replay
		
		//Dispatch all the qualifying entry in the current container		
		final SplitFilterAgent[] clients = getFilterAgents();
		//Too less clients to perform rebalancing
		if(clients.length<2) return;
		
		ContainerEntry[] conEntries = getLogicalEntries();
		//Number of entries/client
		int averageEntriesPerClient = getMaxEntryPerClient(conEntries.length,clients.length);
		Map<Integer,SplitFilterAgent> agentToCountMap = new HashMap<Integer,SplitFilterAgent>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 5912552893187549705L;

			{
				for(SplitFilterAgent client:clients){
					put(client.primeIdentity,client);
				}
			}
		};

		SplitFilterAgent freeAgent = getOneFreeAgent(averageEntriesPerClient);
		if(freeAgent!=null){
			for(ContainerEntry conEntry:conEntries){
				if(freeAgent.getCount()>averageEntriesPerClient){
					//This agent can not handle more, grab another agent.
					freeAgent = getOneFreeAgent(averageEntriesPerClient);
				}
				if(freeAgent!=null){
					SplitFilterAgent currentlyAllocatedTO = agentToCountMap.get(conEntry.getToClient());
					if(currentlyAllocatedTO==null){
						sendEntryToNewClient(freeAgent, conEntry);
					}else{
						if(currentlyAllocatedTO.getCount()>averageEntriesPerClient){
							dispatchEntryRemoved(conEntry);
							sendEntryToNewClient(freeAgent, conEntry);					
						}
					}
				}else{
					//No free agent return
					return;				
				}
			}
		}
	}
	
	@Override
	public void disconnect(ConnectionEvent connectionEvent) {
		assert isConnected():"How the hell did you reach here";
		//Always ensure the disconnected is played back to existing sinks.
		final SplitFilterAgent dcl = getFilterAgent(connectionEvent.getSink());
		//remove the disconnecting client
		super.disconnect(connectionEvent);
		
		ContainerEntry[] conEntries = getLogicalEntries();
		
		final SplitFilterAgent[] clients = getFilterAgents();
		SplitFilterAgent freeAgent = null;
		int averageEntriesPerClient = 0;
		
		if(clients.length>0){
			averageEntriesPerClient = getMaxEntryPerClient(conEntries.length,clients.length);
			freeAgent = getOneFreeAgent(averageEntriesPerClient);
		}
		
		for(ContainerEntry conEntry:conEntries){
			//Process the entries which were present on the outgoing client earlier
			if(dcl.primeIdentity==conEntry.getToClient()){
				//Turn off the dead agents filtered bit position
				conEntry.setFiltered(dcl.primeIdentity, false);
				//Allocate to a free agent if there is any				
				if(freeAgent!=null){
					if(freeAgent.getCount()>=averageEntriesPerClient){
						//This agent can not handle more, grab another agent.
						freeAgent = getOneFreeAgent(averageEntriesPerClient);
						if(freeAgent!=null){
							sendEntryToNewClient(freeAgent, conEntry);
						}
					}else{
						sendEntryToNewClient(freeAgent, conEntry);
					}
				}
			}
		}
	}
	
	private int getMaxEntryPerClient(int numOfContainerEntry,int numOfClient){
		//Number of entries/client
		int averageEntriesPerClient = numOfContainerEntry/numOfClient;
		//If Exceeding max/client cap it to max_per_client 
		averageEntriesPerClient = averageEntriesPerClient > max_per_client?max_per_client:averageEntriesPerClient;
		
		return averageEntriesPerClient;
	}
	
	/**This method sends the entry to the asked client and turns on the
	 * filter position for the client.
	 * @param dcl SplitFilterAgent
	 * @param conEntry ContainerEntry
	 */
	private void sendEntryToNewClient(final SplitFilterAgent dcl,
			ContainerEntry conEntry) {
		conEntry.setFiltered(dcl.primeIdentity, true);
		dcl.incrementCount();
		final ContainerEvent containerEvent = new ContainerInsertEvent(getName(),conEntry.cloneConcrete(),getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.agent.entryAdded(containerEvent);
			}
		});
		recomputeStatelessAttributes(dcl.agent,conEntry,getStatelessAttributes());
	}
	
	@Override
	public void dispatchEntryRemoved(ContainerEntry containerEntry){
		for(SplitFilterAgent dcl : getFilterAgents()){
			//Filter marks this entry as not qualifying to be propagated
			if(containerEntry.isFiltered(dcl.primeIdentity)){
				//This entry was earlier being propagated mark this 
				//not qualifying anymore
				containerEntry.setFiltered(dcl.primeIdentity,false);
				dcl.decrementCount();
				//Remove this entry from requesting containers			
				dispatchEntryRemoved(dcl.agent,containerEntry);
			}
		}
	}
	
	private void dispatchEntryRemoved(final Agent dcl,ContainerEntry containerEntry){
		final ContainerEvent containerEvent = new ContainerDeleteEvent(getName(),containerEntry.getInternalIdentity(),getCurrentTransactionID());
		getEventDispatcher().submit(new Runnable(){
			public void run(){
				dcl.entryRemoved(containerEvent);
			}
		});
	}
	/**
	 *Dumps the current container contents. 
	 */
	public void dumpContainer(){
		//super.dumpContainer();
		int unAllocated= getLogicalEntries().length;
		verbose("##################Split details"+getName());
		for(SplitFilterAgent oneAgent:getFilterAgents()){
			verbose(oneAgent.name+" has "+oneAgent.getCount());
			unAllocated = unAllocated-oneAgent.getCount();
		}
		verbose("UNALLOCATED = "+unAllocated);
		verbose("##################End Dumping Container "+getName());
	}
	
	@Override
	public void applyFilter(final FilterSpec filterSpec){
		assert false:"Filter Operation Not supported on this type of container.";
	}
}
