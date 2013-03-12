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
		private int transactionID;
		private Map<String,State> currentStateMap = new HashMap<String,State>();
		//Operations which has been queued in this transaction
		private Queue<ContainerTask> operationQueueMap = new LinkedList<ContainerTask>();
		private State currentState;
		//Constructor to start a transaction state
		TransactionState(int transactionID, Map<String,State> initialState){
			this.transactionID=transactionID;
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
			assert TransactionTracker.this.transactionAdapter.log("Number of tasks queued in transaction "+transactionID + "="+operationQueueMap.size());
		}

		//Empty the task queue,scenario when task is rolled back before even begun
		void emptyTaskQueue(){
			operationQueueMap.clear();
		}
		
		//Returns the next possible task and removes from the task queue
		ContainerTask getNext() {
			return operationQueueMap.poll();
		}
	}
	private static final class SourceGroupMap{
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
	private final SourceGroupMap sourceGroupMap = new SourceGroupMap();

	//Map keeping the transactions
	private final Map<Integer,TransactionState> transactionStateMap = new HashMap<Integer,TransactionState>();
	
	//Queue to manage transaction ready queue
	private final Queue<Integer> transactionReadyQueue = new LinkedList<Integer>();
	
	//Default transaction state
	private final TransactionState defaultTranState = new TransactionState(0,Collections.<String,State>emptyMap()){
		@Override
		boolean didEveryOneBegin(String includingSource) {
			throw new IllegalStateException("Default transaction should not care about begin transaction");
		}

		@Override
		boolean didEveryOneCommit(String includingSource) {
			throw new IllegalStateException("Default transaction should not care about commit transaction");
		}

		@Override
		void emptyTaskQueue() {
			throw new IllegalStateException("Can not drain default transaction queue");
		}
	};
	
	//Transaction Adapter
	private final TransactionAdapter transactionAdapter;
	
	//Begin the transaction only when all sources commit the transaction
	private final boolean beginOnCommit;

	//Transaction currently in progress
	private int currentTransactionID=0;
	
	//Current Transaction origin
	private String transactionOrigin;
	
	//Time at which current transaction started, used to timeout the transaction.
	private long currentTransactionStartedAt=-1;
	
	
	/**Constructor to initialize transaction tracker.
	 * 
	 * @param transactionAdapter TransactionAdapter 
	 */
	protected TransactionTracker(TransactionAdapter transactionAdapter) {
		assert transactionAdapter!=null;
		this.transactionAdapter=transactionAdapter;
		this.beginOnCommit=transactionAdapter.cl.beginOnCommit();
		sourceGroupMap.buildCircuit(transactionAdapter.cl.getName(),transactionAdapter.cl.getName());
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
					if (!TransactionTracker.this.isIdle() && (System.currentTimeMillis() - currentTransactionStartedAt) > TransactionTracker.this.transactionAdapter
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
		sourceGroupMap.buildCircuit(sourceName,transactionOrigin);
	}
	
	/**Track transaction begin.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackBeginTransaction(final TransactionEvent te){
		assert transactionAdapter.log("Track Begin Transaction: "+te);
		if(sourceGroupMap.knownOrigin(te.getOrigin())){
			TransactionState ts = transactionStateMap.get(te.getTransactionId());
			if(ts==null){
				ts=new TransactionState(te.getTransactionId(),sourceGroupMap.getSourceStateMap(te.getOrigin()));
				transactionStateMap.put(te.getTransactionId(), ts);
				ContainerTask transactionAwareOperation = new ContainerTask(){
					/**
					 * 
					 */
					private static final long serialVersionUID = 235003479179508804L;

					@Override
					public void runtask() {
						setCurrentTransactionID(te.getOrigin(),te.getTransactionId());
						transactionAdapter.beginTran();
					}
				};
				ts.enque(transactionAwareOperation);
			}
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
		if(sourceGroupMap.knownOrigin(te.getOrigin())){
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
		if(sourceGroupMap.knownOrigin(te.getOrigin())){
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
			if(ts!=null){
				 ts.enque(transactionAwareOperation);
			}else{
				assert transactionAdapter.log("????????????????Invalid Operation(transaction ever begin?) with transactionID=:"+transactionId);
			}
		} else {
			defaultTranState.enque(transactionAwareOperation);
		}
	}

	/**The next task to be dispatched
	 * 
	 * @return ContainerTask
	 */
	protected ContainerTask getNext() {
		assert currentTransactionID!=0;
		ContainerTask whatIsNext = null;
		TransactionState ts = transactionStateMap.get(currentTransactionID);
		if(ts!=null){
			whatIsNext = ts.getNext();
		}
		return whatIsNext;
	}
	
	/**The next task to be dispatched
	 * 
	 * @return ContainerTask
	 */
	protected ContainerTask getNextFromReadyQueue() {
		assert currentTransactionID==0;
		ContainerTask whatIsNext = null;
		TransactionState ts = null;
		whatIsNext = defaultTranState.getNext();
		if(whatIsNext==null){
			Integer nextTransaction = transactionReadyQueue.poll();
			assert transactionAdapter.log("###########Polled Queued :"+nextTransaction);
			if(nextTransaction!=null){
				ts = transactionStateMap.get(nextTransaction);				
				whatIsNext = ts.getNext();
				assert whatIsNext!=null;
			}
		}
		return whatIsNext;
	}
	
	
	/**Marks the current transaction in progress
	 * 
	 * @param currentTransactionID
	 */
	protected void setCurrentTransactionID(String transactionOrigin,int currentTransactionID) {
		this.currentTransactionStartedAt=System.currentTimeMillis();
		this.transactionOrigin=transactionOrigin;
		this.currentTransactionID = currentTransactionID;
		assert transactionAdapter.log("##########################Completing transaction:"+currentTransactionID);
	}
	
	/**
	 * Marks the current transaction complete. 
	 */
	protected void completeTransaction(){
		assert transactionAdapter.log("##########################Completing transaction:"+currentTransactionID);
		transactionStateMap.remove(currentTransactionID);
		currentTransactionID=0;
		transactionOrigin=null;
	}
	
	/**Is any transaction in progress?
	 * 
	 * @return boolean
	 */
	public boolean isIdle() {
		return currentTransactionID==0;
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
		return currentTransactionID;
	}

	/**
	 * Returns the Current transaction origin
	 * @return
	 */
	public String getCurrentTransactionOrigin() {
		return transactionOrigin;
	}
	
	/**Returns all origins this tracker knows about.
	 * 
	 * @return String[]
	 */
	public String[] getKnownTransactionOrigins() {
		return sourceGroupMap.getOrigins();
	}
	
	/**Returns operations queued in current state queue
	 * 
	 * @return int
	 */
	public int getTransactionReadyQueue() {
		return transactionReadyQueue.size();
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
