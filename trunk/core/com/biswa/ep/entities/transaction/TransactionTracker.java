package com.biswa.ep.entities.transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.biswa.ep.entities.ContainerTask;
/**Class responsible to manage each block transactions.
 * 
 * @author biswa
 *
 */
public final class TransactionTracker {
	enum State{
		INIT,READY,COMMIT;
	}
	/**
	 * Each transaction is managed in this class. 
	 */
	private class TransactionState{
		private final int transactionID;
		private final String origin;
		private final Map<String,State> currentStateMap = new HashMap<String,State>();
		//Operations which has been queued in this transaction
		private final Queue<ContainerTask> operationQueue = new LinkedList<ContainerTask>();
		private State currentState=State.INIT;
		private long startedAt=0;
		//Constructor to start a transaction state
		TransactionState(String origin,int transactionID, Map<String,State> initialState){
			this.transactionID=transactionID;
			this.origin=origin;
			this.currentStateMap.putAll(initialState);
		}
		
		//Checks if every one reported
		boolean didEveryOneBegin(String includingSource){
			State stateForThisSource = null;
			if((stateForThisSource=currentStateMap.get(includingSource)) !=State.INIT){
				throw new TransactionException("Transaction Begin already Received transaction:="+transactionID +" souce=" +includingSource+" Current state:"+stateForThisSource + " in thread "+Thread.currentThread().getName());
			}else{
				currentStateMap.put(includingSource, State.READY);
			}
			boolean toBegin = false;
			if(!currentStateMap.containsValue(State.INIT)){
				currentState=State.READY;
				toBegin=true;
			}
			return toBegin;
		}
		
		//Checks if every one commit
		boolean didEveryOneCommit(String includingSource){
			State stateForThisSource = null;
			if((stateForThisSource=currentStateMap.get(includingSource)) !=State.READY){
				throw new TransactionException("Transaction attmpted to be committed without corresponding begin transaction:="+transactionID +" souce=" +includingSource+" Current state:"+stateForThisSource);
			}else{
				currentStateMap.put(includingSource, State.COMMIT);
			}
			boolean toCommit = false;
			if(currentState==State.READY && !currentStateMap.containsValue(State.READY)){
				currentState=State.COMMIT;
				toCommit=true;
			}
			return toCommit;
		}
		/** If this source participating in this transaction? return the state for the transaction
		 * of this source.
		 * @param source
		 * @return State
		 */
		State stateForTheSource(String source){
			return currentStateMap.get(source);
		}
		//Enque the task
		void enque(ContainerTask transactionAwareOperation) {
			operationQueue.add(transactionAwareOperation);
		}

		//Empty the task queue,scenario when task is rolled back before even begun
		void emptyTaskQueue(){
			operationQueue.clear();
		}
		
		//Returns the next possible task and removes from the task queue
		ContainerTask getNext() {
			return operationQueue.poll();
		}

		int getTransactionID() {
			return transactionID;
		}

		String getOrigin() {
			return origin;
		}
		void begin(){
			startedAt = System.currentTimeMillis();
		}
		long startedAt(){
			return startedAt;
		}
	}
	private static final class OriginToSourceManager{
		private Map<String,String[]> sourceToOriginMap = new HashMap<String,String[]>();
		private Map<String,Map<String,State>> originToSourcesMap = new HashMap<String,Map<String,State>>();
		private void buildCircuit(String source,String[] transactionOrigin){
			sourceToOriginMap.put(source, transactionOrigin);
			//Build reverse Map
			for(String oneOrigin:transactionOrigin){
				Map<String,State> sourceMap=originToSourcesMap.get(oneOrigin);
				if(sourceMap==null){
					sourceMap = new HashMap<String,State>();
					originToSourcesMap.put(oneOrigin, sourceMap);
				}
				sourceMap.put(source, State.INIT);
			}
		};

		private void dropSource(String sourceName) {
			for(String origin:sourceToOriginMap.get(sourceName)){
				Map<String, State> sourceMap = originToSourcesMap.get(origin);
				sourceMap.remove(sourceName);
				if(sourceMap.isEmpty()){
					originToSourcesMap.remove(origin);
				}
			}
			sourceToOriginMap.remove(sourceName);
		}

		private void buildCircuit(String source,String transactionOrigin){
			Map<String,State> sourceMap = new HashMap<String,State>();
			sourceMap.put(source, State.INIT);
			originToSourcesMap.put(transactionOrigin, sourceMap);
		}

		public Map<String,State> getSourceStateMap(String origin){
			return originToSourcesMap.get(origin);
		}
		
		public boolean knownOrigin(String origin){
			return originToSourcesMap.containsKey(origin);
		}
		
		public String[] getOrigins() {
			return originToSourcesMap.keySet().toArray(new String[0]);
		}
	}
	//Source Group Map managing source grouping
	private final OriginToSourceManager originToSourceManager = new OriginToSourceManager();

	//Map keeping the transactions
	private final Map<Integer,TransactionState> transactionStateMap = new HashMap<Integer,TransactionState>();
	
	//Queue to manage transaction ready queue
	private final Queue<Integer> transactionReadyQueue = new LinkedList<Integer>();
	
	//Transaction Adapter
	private final TransactionAdapter transactionAdapter;
	
	//Atomic operations with zero transaction id
	private final TransactionState atomicTransactionState;
	
	//Begin the transaction only when all sources commit the transaction
	private final boolean beginOnCommit;
	
	//Transaction in progress currently
	private TransactionState activeTransaction;	
	
	/**Constructor to initialize transaction tracker.
	 * 
	 * @param transactionAdapter TransactionAdapter 
	 */
	protected TransactionTracker(TransactionAdapter transactionAdapter) {
		assert transactionAdapter!=null;
		this.transactionAdapter=transactionAdapter;
		this.beginOnCommit=transactionAdapter.cl.beginOnCommit();
		originToSourceManager.buildCircuit(transactionAdapter.cl.getName(),transactionAdapter.cl.getName());
		atomicTransactionState = new TransactionState(transactionAdapter.cl.getName(),0,originToSourceManager.getSourceStateMap(transactionAdapter.cl.getName())){
			@Override
			boolean didEveryOneBegin(String includingSource) {
				throw new IllegalStateException("Atomic operations should not care about begin transaction");
			}

			@Override
			boolean didEveryOneCommit(String includingSource) {
				throw new IllegalStateException("Atomic operations should not care about commit transaction");
			}

			@Override
			void emptyTaskQueue() {
				throw new IllegalStateException("Can not drain Atomic operations queue");
			}
		};
		activeTransaction = atomicTransactionState;
		initializeTimeOut();
	}
	
	/**
	 * Method to initialize the timeout implementation.
	 */
	protected void initializeTimeOut() {
		if (this.transactionAdapter.getTimeOutPeriodInMillis() > 0) {
			Runnable timeOutTask = new Runnable() {
				@Override
				public void run() {
					if (!TransactionTracker.this.isIdle() && (System.currentTimeMillis() - activeTransaction.startedAt()) >= TransactionTracker.this.transactionAdapter
							.getTimeOutPeriodInMillis()) {
						rollbackTran();
						TransactionTracker.this.transactionAdapter
								.transactionTimedOut();
					}
				}
			};
			ScheduledThreadPoolExecutor executor = this.transactionAdapter.getEventCollector();
			
			executor.scheduleWithFixedDelay(
					timeOutTask,
					0,
					this.transactionAdapter.getTimeOutPeriodInMillis(),
					TimeUnit.MILLISECONDS);
		}
	}

	/**Source and the group they belong
	 * 
	 * @param sourceName
	 * @param transactionGroup
	 */
	protected void addSource(String sourceName,String[] transactionOrigin) {
		originToSourceManager.buildCircuit(sourceName,transactionOrigin);
	}
	
	/**Method drops the source.
	 * 
	 * @param sourceName
	 */
	protected void dropSource(String sourceName) {
		clearTransaction(sourceName);
		originToSourceManager.dropSource(sourceName);
	}
	
	/**Method clears any outstanding transaction from this source.
	 * 
	 * @param sourceName
	 */
	protected void clearTransaction(String sourceName) {				
		for (Entry<Integer, TransactionState> oneTransactionEntry : transactionStateMap
				.entrySet()) {
			TransactionState oneTransaction = oneTransactionEntry.getValue();
			State currentState = oneTransaction.stateForTheSource(sourceName);
			if (currentState!=null) {
				switch (currentState) {
				case INIT:
					trackBeginTransaction(new TransactionEvent(sourceName,
							oneTransaction.getOrigin(),
							oneTransactionEntry.getKey()));
				case READY:
					trackCommitTransaction(new TransactionEvent(sourceName,
							oneTransaction.getOrigin(),
							oneTransactionEntry.getKey()));
				default:
					break;
				}
			}
		}
	}
	
	/**Track transaction begin.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackBeginTransaction(final TransactionEvent te){
		assert transactionAdapter.log("Track Begin Transaction: "+te);
		if(originToSourceManager.knownOrigin(te.getOrigin())){
			if(te.getTransactionId()!=0){
				if(!transactionStateMap.containsKey(te.getTransactionId())){
					final TransactionState newTransaction=new TransactionState(te.getOrigin(),te.getTransactionId(),originToSourceManager.getSourceStateMap(te.getOrigin()));
					transactionStateMap.put(te.getTransactionId(), newTransaction);
					ContainerTask transactionAwareOperation = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 235003479179508804L;
	
						@Override
						public void runtask() {
							beginTransaction(newTransaction);
						}
					};
					newTransaction.enque(transactionAwareOperation);
				}
				TransactionState ts = transactionStateMap.get(te.getTransactionId());
				if(ts.didEveryOneBegin(te.getSource())){
					//All sources have reported build the task and push the task into transaction state queue
					assert transactionAdapter.log("Every one begin for Transaction: "+te.getTransactionId());
					if(!beginOnCommit){
						//Push the message only if begin on commit is not enabled.
						transactionReadyQueue.add(te.getTransactionId());
						assert transactionAdapter.log("###########Queuing Transaction:"+te.getTransactionId());
						assert transactionAdapter.log("###########Transaction Queue Size:"+ts.operationQueue.size());
						assert transactionAdapter.log("###########All Queued Transactions:"+transactionReadyQueue);
					}
				}
			}
		}else{
			throw new TransactionException("Unknown source in transaction:"+te);
		}
	}

	/**Track transaction commit.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackCommitTransaction(TransactionEvent te){
		assert transactionAdapter.log("Track Commit Transaction: "+te);
		if(originToSourceManager.knownOrigin(te.getOrigin())){
			if(te.getTransactionId()!=0){
				TransactionState ts = transactionStateMap.get(te.getTransactionId());			
				if(ts!=null){
					if(ts.didEveryOneCommit(te.getSource())){
						assert transactionAdapter.log("Every one committed for Transaction: "+te.getTransactionId());
						//Enqueue Commit transaction task
						ContainerTask buildCommitTranTask = new ContainerTask() {
							/**
							 * 
							 */
							private static final long serialVersionUID = -5106553872435964358L;
	
							public void runtask() {
								commitTran();
							}
						};
						ts.enque(buildCommitTranTask);
						if(beginOnCommit){
							transactionReadyQueue.add(te.getTransactionId());
						}
					}else{
						assert transactionAdapter.log("Not Every one committed for Transaction: "+te.getTransactionId());
					}
				} else {
					throw new TransactionException("Transaction attmpted to be committed without corresponding begin transaction:="+te.getTransactionId());
				}
			}
		}else{
			throw new TransactionException("Unknown source in transaction:"+te);
		}
	}
	
	/**Track transaction rollback.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackRollbackTransaction(TransactionEvent te){
		assert transactionAdapter.log("Track Rollback Transaction: "+te);
		if(originToSourceManager.knownOrigin(te.getOrigin())){
			TransactionState ts = transactionStateMap.get(te.getTransactionId());
			if(ts!=null){
				if(transactionReadyQueue.contains(te.getTransactionId())){
					//Best Case scenario transaction rolled back without any side effect.
					transactionReadyQueue.remove(te.getTransactionId());
					transactionStateMap.remove(te.getTransactionId());
				}else{
					//Next best just let know down stream at best effort 
					//in case down stream can manage this atom bomb
					ts.emptyTaskQueue();
					ContainerTask transactionAwareOperation = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 48624528355863414L;

						@Override
						public void runtask() {
							rollbackTran();
						}
					};
					ts.enque(transactionAwareOperation);
				}
			}
		}else{
			throw new TransactionException("Unknown source in transaction:"+te);
		}
	}

	protected void addOperation(int transactionId,ContainerTask transactionAwareOperation) {
		if(transactionId!=0){
			TransactionState ts = transactionStateMap.get(transactionId);
			ts.enque(transactionAwareOperation);
		} else {
			atomicTransactionState.enque(transactionAwareOperation);
		}
	}

	/**The next task to be dispatched
	 * 
	 * @return ContainerTask
	 */
	protected ContainerTask getNext() {
		ContainerTask whatIsNext = null;
		if(isIdle()){
			whatIsNext = atomicTransactionState.getNext();
			if(whatIsNext==null){
				Integer nextTransaction = transactionReadyQueue.poll();
				if(nextTransaction!=null){
					TransactionState ts = transactionStateMap.get(nextTransaction);
					whatIsNext = ts.getNext();
				}
			}
		}else{
			whatIsNext = activeTransaction.getNext();
		}
		return whatIsNext;
	}
	


	protected void beginDefaultTran() {
		if(activeTransaction.getTransactionID()==0){
			int generatedTransactionID = transactionAdapter.getNextTransactionID();
			TransactionState ts=new TransactionState(transactionAdapter.cl.getName(),generatedTransactionID,originToSourceManager.getSourceStateMap(transactionAdapter.cl.getName()));
			transactionStateMap.put(generatedTransactionID, ts);
			beginTransaction(ts);
		}else{
			throw new IllegalStateException("Can not initiate a default transaction while a transaction already in progress:"+activeTransaction.getTransactionID());
		}
	}

	protected void commitDefaultTran() {
		commitTran();
	}
	
	protected void rollbackDefaultTran() {
		rollbackTran();
	}
	
	/**Marks the current transaction in progress
	 * 
	 * @param newTransaction TransactionState
	 */
	private void beginTransaction(TransactionState newTransaction) {
		assert transactionAdapter.log("##########################Begining transaction:"+newTransaction.transactionID);
		newTransaction.begin();
		activeTransaction=newTransaction;
		transactionAdapter.beginTran();
	}
	
	private void commitTran() {
		assert transactionAdapter.log("##########################Commiting transaction:"+activeTransaction.getTransactionID());
		transactionAdapter.commitTran();
		transactionStateMap.remove(activeTransaction.getTransactionID());
		activeTransaction=atomicTransactionState;
	}
	
	private void rollbackTran() {
		assert transactionAdapter.log("##########################Rolling back transaction:"+activeTransaction.getTransactionID());
		transactionAdapter.rollbackTran();
		transactionStateMap.remove(activeTransaction.getTransactionID());
		activeTransaction=atomicTransactionState;
	}
	
	/**Is any transaction in progress?
	 * 
	 * @return boolean
	 */
	public boolean isIdle() {
		return activeTransaction==atomicTransactionState;
	}

	/**Transactions in progress currently
	 * 
	 * @return Integer[]
	 */
	public Integer[] transactionsInProgress() {
		return transactionStateMap.keySet().toArray(new Integer[0]);
	}	

	/**Returns the current transaction
	 * 
	 * @return int
	 */
	public int getCurrentTransactionID() {
		return activeTransaction.getTransactionID();
	}

	/**
	 * Returns the Current transaction origin
	 * @return
	 */
	public String getCurrentTransactionOrigin() {
		return activeTransaction.getOrigin();
	}
	
	/**Returns all origins this tracker knows about.
	 * 
	 * @return String[]
	 */
	public String[] getKnownTransactionOrigins() {
		return originToSourceManager.getOrigins();
	}
	
	/**Returns operations queued in current state queue
	 * 
	 * @return int
	 */
	public Collection<Integer> getTransactionReadyQueue() {
		return Collections.unmodifiableCollection(transactionReadyQueue);
	}
	
	/**Returns operations queued in current state queue
	 * 
	 * @return int
	 */
	public int getOpsInTransactionQueue() {
		Collection<TransactionState> stateCollection = transactionStateMap.values();
		int count = atomicTransactionState.operationQueue.size();
		for (TransactionState ts:stateCollection){
			count = count + ts.operationQueue.size();
		}
		return count;
	}
}
