package com.biswa.ep.entities.transaction;

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
	/**
	 * Each transaction is managed in this class. 
	 */
	private static final class TransactionState{
		//Transaction State
		enum State{INIT,READY,COMMIT}
		private State currentState=State.INIT;
		//This flag makes it enable to begin processing 
		//when all sources commit.
		private boolean beginOnCommit=false;
		private int allocationState = Integer.MAX_VALUE;
		private Map<String,Integer> sourceToGroupBeginMap = new HashMap<String,Integer>();
		private Map<String,Integer> sourceToGroupCommitMap = new HashMap<String,Integer>();
		//Operations which has been queued in this transaction
		private Queue<ContainerTask> operationQueueMap = new LinkedList<ContainerTask>();
		//Constructor to start a transaction state
		TransactionState(SourceGroupMap sourceGroupMap){
			this.allocationState=sourceGroupMap.allocationState;
			this.sourceToGroupBeginMap.putAll(sourceGroupMap.sourceToGroup);
			this.sourceToGroupCommitMap.putAll(sourceGroupMap.sourceToGroup);
		}
		
		//Checks if every one reported
		boolean didEveryOneBegin(String includingSource){
			boolean toBegin = false;
			if(sourceToGroupBeginMap.put(includingSource,0)!=null){
				int presentStatus =0;
				for(int oneSourceVal:sourceToGroupBeginMap.values()){
					presentStatus = presentStatus|oneSourceVal;
				}
				if((allocationState|presentStatus)<Integer.MAX_VALUE){
					currentState=State.READY;
					toBegin=true;
				}
			}else{
				currentState=State.READY;
				toBegin=true;
			}
			return toBegin;
		}
		
		//Checks if every one commit
		boolean didEveryOneCommit(String includingSource){
			boolean toCommit = false;
			if(sourceToGroupCommitMap.put(includingSource,0)!=null){
				int presentStatus =0;
				for(int oneSourceVal:sourceToGroupCommitMap.values()){
					presentStatus = presentStatus|oneSourceVal;
				}
				if((allocationState|presentStatus)<Integer.MAX_VALUE){
					currentState=State.COMMIT;
					toCommit=true;
				}
			}else{
				currentState=State.COMMIT;
				toCommit=true;
			}
			return toCommit;
		}
		
		boolean isNotAlreadyCommitted(){
			return currentState!=State.COMMIT;
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
	}
	private static final class SourceGroupMap{
		private int allocationState=Integer.MAX_VALUE;
		private Map<String,Integer> sourceToGroup = new HashMap<String,Integer>();
		private void buildCircuit(String key,Integer value){
			sourceToGroup.put(key, value);
			int usedState =0;
			for(int oneSourceVal:sourceToGroup.values()){
				usedState = usedState|oneSourceVal;
			}
			allocationState=Integer.MAX_VALUE^usedState;
		};		
	}
	//Source Group Map managing source grouping
	private final SourceGroupMap sourceGroupMap = new SourceGroupMap();

	//Map keeping the transactions
	private final Map<Integer,TransactionState> transactionStateMap = new HashMap<Integer,TransactionState>();

	//Transaction Adapter
	final TransactionAdapter transactionAdapter;

	//Transaction currently in progress
	private int currentTransactionID=0;
	
	private long currentTransactionStartedAt=-1;
	
	
	/**Constructor to initialize transaction tracker.
	 * 
	 * @param transactionAdapter TransactionAdapter 
	 */
	protected TransactionTracker(TransactionAdapter transactionAdapter) {
		assert transactionAdapter!=null;
		this.transactionAdapter=transactionAdapter;
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

	/**Track transaction begin.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackBeginTransaction(final int transactionId,String sourceName){
		assert transactionAdapter.log("Track Begin Transaction: "+sourceName+ " "+transactionId);
		TransactionState ts = transactionStateMap.get(transactionId);
		if(ts==null){
			ts=new TransactionState(sourceGroupMap);
			transactionStateMap.put(transactionId, ts);
		}
		if(ts.didEveryOneBegin(sourceName)){
			assert transactionAdapter.log("Every one begin for Transaction: "+transactionId);
			//All sources have reported
			if(!transactionAdapter.cl.beginOnCommit()){
				beginTransaction(transactionId);
			}else{
				ts.beginOnCommit=true;
			}
		}
	}
	/**Transaction can begin with as soon as all sources begin transaction 
	 * or it can be lazy one and wait till every one commit. 
	 * 
	 * @param transactionId int
	 * @param sourceName
	 * @param ts
	 */
	private void beginTransaction(final int transactionId) { 
		ContainerTask r = new ContainerTask(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 235003479179508804L;

			@Override
			public void runtask() {
				transactionAdapter.beginTran(transactionId);
			}
		};
		transactionAdapter.executeOrEnquePostConnected(r);
	}

	/**Track transaction commit.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackCommitTransaction(int transactionId,String sourceName){
		assert transactionAdapter.log("Track Commit Transaction: "+sourceName+ " "+transactionId);
		TransactionState ts = transactionStateMap.get(transactionId);
		if(ts!=null){
			if(ts.didEveryOneCommit(sourceName)){
				assert transactionAdapter.log("Every one committed for Transaction: "+transactionId);
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -5106553872435964358L;

					public void runtask() {
						transactionAdapter.commitTran();
					}
				};
				addOperation(transactionId, r);
				if(ts.beginOnCommit){
					beginTransaction(transactionId);
				}
			}else{
				assert transactionAdapter.log("Not Every one committed for Transaction: "+transactionId);
			}
		}
	} 
	
	/**Track transaction rollback.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackRollbackTransaction(int transactionId,String sourceName){
		TransactionState ts = transactionStateMap.get(transactionId);
		if(ts!=null){
			if(ts.isNotAlreadyCommitted()){
				if(currentTransactionID==transactionId){
					transactionAdapter.rollbackTran();
				}else{
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
		}
	}
	
	/**
	 * Marks the current transaction complete. 
	 */
	protected void completeTransaction(){
		transactionStateMap.remove(currentTransactionID);
		currentTransactionID=0;
	}
	
	/**Is any transaction in progress?
	 * 
	 * @return boolean
	 */
	boolean isIdle() {
		return currentTransactionID==0;
	}

	/**Transactions in progress currently
	 * 
	 * @return Integer[]
	 */
	Integer[] transactionsInProgress() {
		return transactionStateMap.keySet().toArray(new Integer[0]);
	}

	/**The next task to be dispatched
	 * 
	 * @return ContainerTask
	 */
	ContainerTask getNext() {
		ContainerTask whatIsNext = null;
		TransactionState ts = transactionStateMap.get(currentTransactionID);
		if(ts!=null){
			whatIsNext = ts.getNext();
		}
		return whatIsNext;
	}
	
	/**Source and the group they belong
	 * 
	 * @param sourceName
	 * @param transactionGroup
	 */
	protected void addSource(String sourceName, int transactionGroup) {
		sourceGroupMap.buildCircuit(sourceName,transactionGroup);
	}

	/**Returns the current transaction
	 * 
	 * @return int
	 */
	int getCurrentTransactionID() {
		return currentTransactionID;
	}
	
	/**Marks the current transaction in progress
	 * 
	 * @param currentTransactionID
	 */
	void setCurrentTransactionID(int currentTransactionID) {
		this.currentTransactionStartedAt=System.currentTimeMillis();
		this.currentTransactionID = currentTransactionID;
	}

	protected void addOperation(int transactionId,ContainerTask transactionAwareOperation) {

		if(transactionId!=0){
			if(currentTransactionID==transactionId){
				//Active transaction in progress dispatch directly to the underlying container.
				transactionAdapter.taskHandler.executeNow(transactionAwareOperation);
			}else{
				//Other transaction in progress queue the operation in transaction's private queue
				TransactionState ts = transactionStateMap.get(transactionId);
				if(ts!=null){
					 ts.enque(transactionAwareOperation);
				}
			}
		} else {
			//I dont belong here directly dispatch
			transactionAdapter.executeOrEnquePostConnected(transactionAwareOperation);
		}
	}
}
