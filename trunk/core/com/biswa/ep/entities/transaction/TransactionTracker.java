package com.biswa.ep.entities.transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
		private final Queue<ContainerTask> operationQueueMap = new LinkedList<ContainerTask>();
		private State currentState;
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

		//Enque the task
		void enque(ContainerTask transactionAwareOperation) {
			operationQueueMap.add(transactionAwareOperation);
		}

		//Empty the task queue,scenario when task is rolled back before even begun
		void emptyTaskQueue(){
			operationQueueMap.clear();
		}
		
		//Returns the next possible task and removes from the task queue
		ContainerTask getNext() {
			return operationQueueMap.poll();
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
		private Map<String,Map<String,State>> originToSourcesMap = new HashMap<String,Map<String,State>>();
		private void buildCircuit(String source,String[] transactionOrigin){
			for(String oneOrigin:transactionOrigin){
				Map<String,State> sourceMap=originToSourcesMap.get(oneOrigin);
				if(sourceMap==null){
					sourceMap = new HashMap<String,State>();
					originToSourcesMap.put(oneOrigin, sourceMap);
				}
				sourceMap.put(source, State.INIT);
			}
		};

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
					if (!TransactionTracker.this.isIdle() && (System.currentTimeMillis() - activeTransaction.startedAt()) > TransactionTracker.this.transactionAdapter
							.getTimeOutPeriodInMillis()) {
						TransactionTracker.this.transactionAdapter
								.transactionTimedOut();
					}
				}
			};
			ScheduledThreadPoolExecutor executor = this.transactionAdapter.getEventCollector();
			
			executor.scheduleWithFixedDelay(
					timeOutTask,
					this.transactionAdapter.getTimeOutPeriodInMillis(),
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
							transactionAdapter.beginTran();
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
						assert transactionAdapter.log("###########Transaction Queue Size:"+ts.operationQueueMap.size());
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
								transactionAdapter.commitTran();
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
							transactionAdapter.rollbackTran();
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
	
	/**Marks the current transaction in progress
	 * 
	 * @param newTransaction TransactionState
	 */
	private void beginTransaction(TransactionState newTransaction) {
		assert transactionAdapter.log("##########################Begining transaction:"+newTransaction.transactionID);
		newTransaction.begin();
		activeTransaction=newTransaction;
	}
	
	/**
	 * Marks the current transaction complete. 
	 */
	protected void completeTransaction(){
		assert transactionAdapter.log("##########################Completing transaction:"+activeTransaction.getTransactionID());
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
		int count = 0;
		for (TransactionState ts:stateCollection){
			count = count + ts.operationQueueMap.size();
		}
		return count;
	}
}
