package com.biswa.ep.entities.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.SwingUtilities;

import com.biswa.ep.NamedThreadFactory;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.DataOperation;
import com.biswa.ep.entities.OuterTask;
import com.biswa.ep.entities.PropertyConstants;
import com.biswa.ep.entities.spec.Spec;

/**Class responsible to provide transaction support to the underlying containers
 * and hides it from the complexities. This class responsible to manage the task 
 * priorities and maintaining the order of the operations.
 * 
 * @author biswa
 *
 */
abstract public class TransactionAdapter extends TransactionGeneratorImpl implements DataOperation, TransactionSupport,TransactionRelay,FeedbackSupport {

	/**
	 * Queue manages the transaction which are ready to be processed as soon it is possible.
	 */
	final private Queue<ContainerTask> postConnectedQueue = new LinkedList<ContainerTask>();
	
	/**
	 * Queue manages the transaction which are ready to be processed as soon it is possible.
	 */
	final private Queue<ContainerTask> preConnectedQueue = new LinkedList<ContainerTask>();
	
	/**
	 * The collector thread which is responsible to receive all incoming events and process it on the 
	 * underlying container.
	 */
	private final ScheduledThreadPoolExecutor eventCollector;	
	
	/**
	 * Tracker responsible to manage each batched transaction with required isolation.
	 */
	final TransactionTracker transactionTracker;

	/**
	 * Tracker responsible to manage feedback
	 */
	final FeedbackTracker feedbackTracker;

	/**
	 * The Underlying container on which the events are going to be processed.
	 */
	final protected AbstractContainer cl;
	
	/**
	 *The Handler which applies the container task on the container. 
	 */
	final ContainerTaskHandler taskHandler;
	
	/**Default task handler which applies the task on the container.
	 * 
	 * @author biswa
	 *
	 */
	public class ContainerTaskHandler{
		/**Method which applies the task on the container.
		 * 
		 * @param r
		 */
		public void executeNow(final ContainerTask r){
			r.run();
		}
	}
	
	private class SwingTaskHandler extends ContainerTaskHandler{
		/**Method which applies the task on the container.
		 * 
		 * @param r
		 */
		public void executeNow(final ContainerTask r){
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	/**Multithreaded task handler which applies the updates on the container in multiple
	 * threads. The sequence of operation on the records are guaranteed.
	 * @author biswa
	 *
	 */
	private class MultiThreadedHandler extends ContainerTaskHandler{
		final protected Map<Integer,Future<?>> lockedRows = new HashMap<Integer,Future<?>>();
		final protected ThreadPoolExecutor workerThreadPoolExecutor;
		private MultiThreadedHandler(AbstractContainer cl){
			String worker_thread_count_str = cl.getProperties().getProperty(PropertyConstants.WORKER_THREAD_COUNT);
			int threadCount = Runtime.getRuntime().availableProcessors();
			if(worker_thread_count_str!=null){
				threadCount = Integer.parseInt(worker_thread_count_str);
			}
			workerThreadPoolExecutor = new ScheduledThreadPoolExecutor(threadCount,new NamedThreadFactory("Worker",cl));
		}
		@Override
		public void executeNow(final ContainerTask r){
			final int rowID = r.getRowToBeLocked();
			if(rowID==0){
				Set<Entry<Integer,Future<?>>>  entrySet = lockedRows.entrySet();
				Iterator<Entry<Integer,Future<?>>> iterator = entrySet.iterator();
				while(iterator.hasNext()){
					Entry<Integer,Future<?>> entry = iterator.next();
					waitForTaskCompletion(entry.getValue());
					iterator.remove();
				}
				r.run();				
			} else {
				Future<?> f = lockedRows.get(rowID);
				if(f!=null){
					waitForTaskCompletion(f);
				}
				f = workerThreadPoolExecutor.submit(r);
				lockedRows.put(rowID, f);
			}
		}		
	}
	/** The containing schema under management.
	 * 
	 * @param cl AbstractContainer
	 */
	public TransactionAdapter(final AbstractContainer cl) {
		this.cl=cl;
		eventCollector = new ScheduledThreadPoolExecutor(1,new NamedThreadFactory("PPL",cl));
		if(cl.concurrencySupport()>0){
			taskHandler = new MultiThreadedHandler(cl);
		}else if(cl.concurrencySupport()==0){//Concurrency not supported.
			taskHandler = new ContainerTaskHandler();
		}else{//Execute in Swing thread
			taskHandler = new SwingTaskHandler();
		}
		transactionTracker = new TransactionTracker(this);
		feedbackTracker = new FeedbackTracker(this);
	}
	
	/**Method educates the transaction manager about the transactional source and the
	 * transaction group it manages.
	 * 
	 * @param sourceName
	 * @param transactionGroup
	 */
	protected void addTransactionSource(String sourceName,String[] transactionOrigin){
		assert cl.ensureExecutingInRightThread();
		transactionTracker.addSource(sourceName,transactionOrigin);
	}
	
	@Override
	public void addFeedbackSource(final FeedbackEvent feedbackEvent) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -4797910052132196386L;

					public void runtask() {
						feedbackTracker.addFeedbackSource(feedbackEvent.getSource());
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void removeFeedbackSource(final FeedbackEvent feedbackEvent) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -7746282951782449425L;

					public void runtask() {
						feedbackTracker.removeFeedbackSource(feedbackEvent.getSource());
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);
	}	

	@Override
	public void receiveFeedback(final FeedbackEvent feedbackEvent) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1161720797713415315L;

					public void runtask() {
						feedbackTracker.trackFeedback(feedbackEvent.getTransactionId(),feedbackEvent.getSource());
					}
				};
				executeOrEnquePostConnected(r);
			}
		};
		executeInListenerThread(outer);		
	}

	@Override
	public void entryAdded(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -1233200541986647567L;

					public void runtask() {
						cl.entryAdded(ce);
					}
				};
				transactionTracker.addOperation(ce.getTransactionId(), r);	
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void entryRemoved(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -1336168655096370704L;

					public void runtask() {
						cl.entryRemoved(ce);
					}
				};
				transactionTracker.addOperation(ce.getTransactionId(), r);	
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void entryUpdated(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r =new ContainerTask(ce.getIdentitySequence()) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 7119423560062857413L;

					@Override
					public void runtask() {
						cl.entryUpdated(ce);
					}
				};
				transactionTracker.addOperation(ce.getTransactionId(), r);
			}
		};
		executeInListenerThread(outer);
	}


	@Override
	public void applySpec(final Spec spec) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 2561765662542487226L;

					public void runtask() {
						spec.apply(cl);
					}
				};
				executeOrEnquePostConnected(r);	
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void invokeOperation(final ContainerTask task) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 268349565984980296L;

					public void runtask() {
						taskHandler.executeNow(task);
					}
				};
				executeOrEnquePostConnected(r);	
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void beginTran(final TransactionEvent te) {
		//For every source in transactionSource expect beginTran
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				if(cl.isConnected()){
					transactionTracker.trackBeginTransaction(te);
				}else{
					ContainerTask containerTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 1098720506243982025L;

						@Override
						protected void runtask() throws Exception {
							transactionTracker.trackBeginTransaction(te);							
						}
						
					};
					enqueueInPreConnectedQueue(containerTask);
				}
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void commitTran(final TransactionEvent te) {
		//For every source in transactionSource expect commitTran/rollbackTran
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {

				if(cl.isConnected()){
					transactionTracker.trackCommitTransaction(te);
				}else{
					ContainerTask containerTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 1098720506243982025L;

						@Override
						protected void runtask() throws Exception {
							transactionTracker.trackCommitTransaction(te);						
						}
						
					};
					enqueueInPreConnectedQueue(containerTask);
				}
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void rollbackTran(final TransactionEvent te) {
		//For every source in transactionSource expect commitTran/rollbackTran
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {

				if(cl.isConnected()){
					transactionTracker.trackRollbackTransaction(te);
				}else{
					ContainerTask containerTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = -3946096388155163788L;

						@Override
						protected void runtask() throws Exception {
							transactionTracker.trackRollbackTransaction(te);					
						}
						
					};
					enqueueInPreConnectedQueue(containerTask);
				}
			}
		};
		executeInListenerThread(outer);
	}
	
	/**
	 * Method responsible to begin a default transaction if an Atomic operation is received
	 * by the underlying container.
	 */
	public void beginDefaultTran(){
		transactionTracker.setCurrentTransactionID(cl.getName(),getNextTransactionID());
		beginTran();
	}
	
	/**
	 * Method responsible to commit a default transaction if an Atomic operation is committed
	 * by the underlying container.
	 */

	public void commitDefaultTran(){
		commitTran();
	}
	
	/**
	 * Method responsible to rollback a default transaction if an Atmoic operation fails.
	 */

	public void rollbackDefaultTran(){
		rollbackTran();
	}

	@Override
	public void beginTran(){		
		assert cl.ensureExecutingInRightThread();
		cl.beginTran();
		checkQueuedTransaction();
	}
	
	@Override
	public void commitTran(){
		assert cl.ensureExecutingInRightThread();
		cl.commitTran();
		transactionTracker.completeTransaction();
		checkQueuedTransaction();
	}

	@Override
	public void rollbackTran(){
		assert cl.ensureExecutingInRightThread();
		cl.rollbackTran();
		transactionTracker.completeTransaction();
		checkQueuedTransaction();
	}

	
	public void completionFeedback(int transactionID){
		assert cl.ensureExecutingInRightThread();
		cl.completionFeedback(transactionID);
	}

	@Override
	public void addFeedbackAgent(final FeedbackAgent feedBackAgent){
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 4510373033258245094L;

					public void runtask() {
						cl.addFeedbackAgent(feedBackAgent);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void transactionTimedOut(){
		rollbackTran();
	}
	@Override
	public long getTimeOutPeriodInMillis(){
		return cl.getTimeOutPeriodInMillis();
	}	
	/**
	 * Dispatch all tasks which can possibly be dispatched at this point.
	 */
	protected void checkQueuedTransaction(){ 
		assert cl.ensureExecutingInRightThread();
		
		ContainerTask r = null;
		//Task is dispatched to executor
		while ((r = getNext())!=null) {
			taskHandler.executeNow(r);
		}	
	}
	
	/**Flush any messages left in pre connected queue before marking container connected.
	 * 
	 */
	protected void flushPreconnectedQueue(){
		ContainerTask r = null;
		while((r=preConnectedQueue.poll())!=null){
			taskHandler.executeNow(r);
		}
	}

	/**Enqueue the container tasks in pre connected queue.
	 * 
	 * @param outer OuterTask
	 */
	protected void enqueueInPreConnectedQueue(ContainerTask r) {
		preConnectedQueue.add(r);
	}

	/**The tasks which are supposed to be dispatched even before connected
	 * are submitted to this method.
	 * @param r Runnable
	 */
	protected void executeOrEnque(ContainerTask r) {
		assert cl.ensureExecutingInRightThread();
		
		if (transactionTracker.isIdle()) {
			taskHandler.executeNow(r);
		} else {
			//Source container is processing other transaction enqueing it in transaction ready queue
			if(!isConnected()){
				preConnectedQueue.add(r);
			}else{
				postConnectedQueue.add(r);
			}
		}
	}
	
	/**The tasks which are supposed to be dispatched after connected are submitted 
	 * to this method. Anything submitted to this method would be queued up
	 * till the container is connected.
	 * @param r Runnable
	 */
	protected void executeOrEnquePostConnected(ContainerTask r) {
		assert cl.ensureExecutingInRightThread();
		
		if (isConnected() && transactionTracker.isIdle()) {
			taskHandler.executeNow(r);
		} else {
			//Source container is processing other transaction enqueing it in transaction ready queue
			postConnectedQueue.add(r);
		}
	}

	/**The task submitted by outside world are submitted to the event collector thread
	 * 
	 * @param outer OuterTask
	 */
	protected void executeInListenerThread(OuterTask outer) {
		eventCollector.execute(outer);
	}

	private void waitForTaskCompletion(Future<?> f) {
		try {
			f.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getCurrentTransactionID(){
		return transactionTracker.getCurrentTransactionID();
	}

	@Override
	public String getCurrentTransactionOrigin(){
		return transactionTracker.getCurrentTransactionOrigin();
	}
	@Override
	public String[] getKnownTransactionOrigins(){
		return transactionTracker.getKnownTransactionOrigins();		
	}
	
	/**Returns the next task to be executed.
	 * 
	 * @return Runnable
	 */
	protected ContainerTask getNext(){
		assert cl.ensureExecutingInRightThread();
		
		ContainerTask whatIsNext = null;
		if(transactionTracker.isIdle()){
			//No transaction in progress so pick up the next major task in transaction ready queue.
			if(isConnected()){
				whatIsNext = postConnectedQueue.poll();
			}else{
				whatIsNext = preConnectedQueue.poll();
			}
		}else{
			//Transaction in progress so pick up the next minor task which is in private queue of current transaction.
			whatIsNext = transactionTracker.getNext();
		}
		return whatIsNext;
	}
	
	public ScheduledThreadPoolExecutor getEventCollector() {
		return eventCollector;
	}

	public ContainerTaskHandler getTaskHandler() {
		return taskHandler;
	}

	/**Destroy the listener.
	 * 
	 */
	public synchronized void destroy(){
		eventCollector.shutdownNow();
		cl.destroy();
	}

	@Override
	protected synchronized void finalize(){
		if(eventCollector!=null && !eventCollector.isTerminated()){
			destroy();
		}
	}

	public boolean isConnected() {
		return cl.isConnected();
	}
		
	@Override
	public String toString() {
		return cl.getName();
	}
}