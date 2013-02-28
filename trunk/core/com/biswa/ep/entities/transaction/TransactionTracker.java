package com.biswa.ep.entities.transaction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.biswa.ep.EPEvent;
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
	private static final class TransactionState{
		//This flag makes it enable to begin processing 
		//when all sources commit.
		private boolean beginOnCommit=false;
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
				throw new TransactionException("Transaction Begin already Received transaction:="+transactionID +" souce=" +includingSource+" Current state:"+stateForThisSource);
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
		private Map<String,Map<String,State>> originToSourcesMap = new HashMap<String,Map<String,State>>();
		private SourceGroupMap(){
			buildCircuit(EPEvent.DEF_SRC,EPEvent.DEF_SRC);
		}
		private void buildCircuit(String source,String[] transactionOrigin){
			for(String oneOrigin:transactionOrigin){
				if(!EPEvent.DEF_SRC.equals(oneOrigin)){
					Map<String,State> sourceMap=originToSourcesMap.get(oneOrigin);
					if(sourceMap==null){
						sourceMap = new HashMap<String,State>();
						originToSourcesMap.put(oneOrigin, sourceMap);
					}
					sourceMap.put(source, State.INIT);
				}
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

	//Transaction Adapter
	final TransactionAdapter transactionAdapter;

	//Transaction currently in progress
	private int currentTransactionID=0;
	
	//Current Transaction origin
	private String transactionOrigin;
	
	private long currentTransactionStartedAt=-1;
	
	
	/**Constructor to initialize transaction tracker.
	 * 
	 * @param transactionAdapter TransactionAdapter 
	 */
	protected TransactionTracker(TransactionAdapter transactionAdapter) {
		assert transactionAdapter!=null;
		this.transactionAdapter=transactionAdapter;
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

	/**Track transaction begin.
	 * 
	 * @param transactionId
	 * @param sourceName
	 */
	protected void trackBeginTransaction(TransactionEvent te){
		assert transactionAdapter.log("Track Begin Transaction: "+te);
		if(sourceGroupMap.knownOrigin(te.getOrigin())){
			TransactionState ts = transactionStateMap.get(te.getTransactionId());
			if(ts==null){
				ts=new TransactionState(te.getTransactionId(),sourceGroupMap.getSourceStateMap(te.getOrigin()));
				transactionStateMap.put(te.getTransactionId(), ts);
			}
			if(ts.didEveryOneBegin(te.getSource())){
				assert transactionAdapter.log("Every one begin for Transaction: "+te.getTransactionId());
				//All sources have reported
				if(!transactionAdapter.cl.beginOnCommit()){
					beginTransaction(te.getOrigin(),te.getTransactionId());
				}else{
					ts.beginOnCommit=true;
				}
			}
		
		}else{
			throw new TransactionException("Unknown source in transaction:"+te);
		}
	}
	/**Transaction can begin with as soon as all sources begin transaction 
	 * or it can be lazy one and wait till every one commit. 
	 * 
	 * @param transactionId int
	 * @param sourceName
	 * @param ts
	 */
	private void beginTransaction(final String origin,final int transactionId) { 
		ContainerTask r = new ContainerTask(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 235003479179508804L;

			@Override
			public void runtask() {
				setCurrentTransactionID(origin,transactionId);
				transactionAdapter.beginTran();
			}
		};
		transactionAdapter.executeOrEnquePostConnected(r);
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
					ContainerTask r = new ContainerTask() {
						/**
						 * 
						 */
						private static final long serialVersionUID = -5106553872435964358L;
	
						public void runtask() {
							transactionAdapter.commitTran();
						}
					};
					addOperation(te.getTransactionId(), r);
					if(ts.beginOnCommit){
						beginTransaction(te.getOrigin(),te.getTransactionId());
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
				if(ts.isNotAlreadyCommitted()){
					if(currentTransactionID==te.getTransactionId()){
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
		}else{
			throw new TransactionException("Unknown source in transaction:"+te);
		}
	}
	
	/**
	 * Marks the current transaction complete. 
	 */
	protected void completeTransaction(){
		transactionStateMap.remove(currentTransactionID);
		currentTransactionID=0;
		transactionOrigin=null;
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
	protected void addSource(String sourceName,String[] transactionOrigin) {
		System.out.println("#############"+Thread.currentThread().getName()+":"+ sourceName +" origins"+Arrays.toString(transactionOrigin));
		sourceGroupMap.buildCircuit(sourceName,transactionOrigin);
	}

	/**Returns the current transaction
	 * 
	 * @return int
	 */
	int getCurrentTransactionID() {
		return currentTransactionID;
	}


	String getCurrentTransactionOrigin() {
		return transactionOrigin;
	}
	
	public String[] getKnownTransactionOrigins() {
		return sourceGroupMap.getOrigins();
	}
	/**Marks the current transaction in progress
	 * 
	 * @param currentTransactionID
	 */
	void setCurrentTransactionID(String transactionOrigin,int currentTransactionID) {
		this.currentTransactionStartedAt=System.currentTimeMillis();
		this.transactionOrigin=transactionOrigin;
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
