package com.biswa.ep.entities.transaction;

import java.util.Collection;
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
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.DataOperation;
import com.biswa.ep.entities.OuterTask;
import com.biswa.ep.entities.PropertyConstants;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.Spec;
import com.biswa.ep.entities.substance.Substance;

/**Class responsible to provide transaction support to the underlying containers
 * and hides it from the complexities. This class responsible to manage the task 
 * priorities and maintaining the order of the operations.
 * 
 * @author biswa
 *
 */
abstract public class TransactionAdapter extends TransactionGeneratorImpl implements DataOperation, TransactionSupport,TransactionRelay,FeedbackSupport {

	/**
	 * Queue manages the task which must be performed only when container is in connected state.
	 * Any transactional operation can only be performed after a container is connected.
	 */
	final private Queue<ContainerTask> postConnectedQueue = new LinkedList<ContainerTask>();
	
	/**
	 * Queue manages the task list which can be performed right before container is connected.
	 */
	final private Queue<ContainerTask> preConnectedQueue = new LinkedList<ContainerTask>();
	
	/**
	 * The collector thread which is responsible to receive all incoming events and process it on the 
	 * underlying container. Its imperative that NOTHING should skip this thread.
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
	 *The Handler which applies the container task on the container. This is holy grail
	 *for container multi-threading.
	 */
	final ContainerTaskHandler taskHandler;
	
	/**Default single threaded task handler which applies the task on the container.
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

		public boolean ensureExecutingInRightThread(boolean b) {
			return Thread.currentThread().getName().startsWith("PPL-"+cl.getName());
		}

		public void destroy() {
			eventCollector.shutdownNow();			
		}
	}
	
	/**Task handler which dispatches all threads in Swing thread.
	 * 
	 * @author Biswa
	 *
	 */
	private class SwingTaskHandler extends ContainerTaskHandler{
		/**Method which applies the task on the container.
		 * 
		 * @param r
		 */
		public void executeNow(final ContainerTask r){
			try {
				if(SwingUtilities.isEventDispatchThread()){
					r.run();
				}else{
					SwingUtilities.invokeAndWait(r);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public boolean ensureExecutingInRightThread(boolean b) {
			return SwingUtilities.isEventDispatchThread();
		}
	}
	
	/**Multi-threaded task handler which applies the updates on the container in multiple
	 * threads. The sequence of operation on the records are guaranteed. It uses the record 
	 * locking concept for achieving parallelism. 
	 * @author biswa
	 *
	 */
	private class MultiThreadedHandler extends ContainerTaskHandler{
		final protected Map<Integer,Future<?>> lockedRows = new HashMap<Integer,Future<?>>();
		final protected ThreadPoolExecutor workerThreadPoolExecutor;
		private MultiThreadedHandler(AbstractContainer cl){
			String worker_thread_count_str = cl.getProperty(PropertyConstants.WORKER_THREAD_COUNT);
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

		public boolean ensureExecutingInRightThread(boolean worker) {
			if(worker){
				return Thread.currentThread().getName().startsWith("Worker-"+cl.getName());
			}else{
				return super.ensureExecutingInRightThread(worker);
			}
		}

		public void destroy() {
			super.destroy();
			workerThreadPoolExecutor.shutdownNow();			
		}
	}
	
	/** Constructor takes underlying container being managed by this transaction adapter.
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
				final ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -1233200541986647567L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.entryAdded(ce);
					}
				};
				if(cl.isConnected()){
					transactionTracker.addOperation(ce.getTransactionId(), r);
					checkQueuedTransaction();
				}else{
					ContainerTask wrapperTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 6033820351648817067L;

						@Override
						protected void runtask() throws Throwable {
							transactionTracker.addOperation(ce.getTransactionId(), r);							
						}						
					};
					enqueueInPostConnectedQueue(wrapperTask);
				}
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void entryRemoved(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				final ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -1233200541986647567L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.entryRemoved(ce);
					}
				};
				if(cl.isConnected()){
					transactionTracker.addOperation(ce.getTransactionId(), r);
					checkQueuedTransaction();
				}else{
					ContainerTask wrapperTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 6033820351648817067L;

						@Override
						protected void runtask() throws Throwable {
							transactionTracker.addOperation(ce.getTransactionId(), r);							
						}						
					};
					enqueueInPostConnectedQueue(wrapperTask);
				}
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void entryUpdated(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				final ContainerTask r = new ContainerTask(ce.getIdentitySequence()) {
					/**
					 * 
					 */
					private static final long serialVersionUID = -1233200541986647567L;

					public void runtask() {
						cl.entryUpdated(ce);
					}
				};
				if(cl.isConnected()){
					transactionTracker.addOperation(ce.getTransactionId(), r);
					checkQueuedTransaction();
				}else{
					ContainerTask wrapperTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 6033820351648817067L;

						@Override
						protected void runtask() throws Throwable {
							transactionTracker.addOperation(ce.getTransactionId(), r);							
						}						
					};
					enqueueInPostConnectedQueue(wrapperTask);
				}
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void clear() {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				//No Need to implement default transaction as it is not supposed to
				//generate data events.
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -6605205914636092169L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.clear();
					}
				};
				executeOrEnquePostConnected(r);
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void updateStatic(final Attribute attribute,final Substance substance,
			final FilterSpec appliedFilter) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				//Need to implement default transaction as it can
				//potentially generate data events
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 969041971977780486L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.updateStatic(attribute,substance,appliedFilter);
					}
				};
				executeOrEnque(r);
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
						assert ensureExecutingInRightThread();
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
					checkQueuedTransaction();
				}else{
					ContainerTask containerTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 1098720506243982025L;

						@Override
						protected void runtask(){
							transactionTracker.trackBeginTransaction(te);
						}
						
					};
					enqueueInPostConnectedQueue(containerTask);
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
					checkQueuedTransaction();
				}else{
					ContainerTask containerTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 1098720506243982025L;

						@Override
						protected void runtask(){
							transactionTracker.trackCommitTransaction(te);						
						}
						
					};
					enqueueInPostConnectedQueue(containerTask);
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
					checkQueuedTransaction();
				}else{
					ContainerTask containerTask = new ContainerTask(){
						/**
						 * 
						 */
						private static final long serialVersionUID = -3946096388155163788L;

						@Override
						protected void runtask(){
							transactionTracker.trackRollbackTransaction(te);					
						}
						
					};
					enqueueInPostConnectedQueue(containerTask);
				}
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void beginDefaultTran(){
		transactionTracker.beginDefaultTran();
	}
	
	@Override
	public void commitDefaultTran(){
		transactionTracker.commitDefaultTran();
	}
	
	@Override
	public void rollbackDefaultTran(){
		transactionTracker.rollbackDefaultTran();
	}

	@Override
	public void beginTran(){		
		assert ensureExecutingInRightThread();
		cl.beginTran();
	}
	
	@Override
	public void commitTran(){
		assert ensureExecutingInRightThread();
		cl.commitTran();
	}

	@Override
	public void rollbackTran(){
		assert ensureExecutingInRightThread();
		cl.rollbackTran();
	}
	@Override
	public void completionFeedback(int transactionID){
		assert ensureExecutingInRightThread();
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
						assert ensureExecutingInRightThread();
						cl.addFeedbackAgent(feedBackAgent);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);
	}
	@Override
	public void removeFeedbackAgent(final FeedbackAgent feedBackAgent){
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 4510373033258245094L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.removeFeedbackAgent(feedBackAgent);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void transactionTimedOut(){
		checkQueuedTransaction();
	}
	
	@Override
	public long getTimeOutPeriodInMillis(){
		return cl.getTimeOutPeriodInMillis();
	}	
	/**
	 * Dispatch all tasks which can possibly be dispatched at this point.
	 */
	protected void checkQueuedTransaction(){ 		
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
	
	/**Enqueue the container tasks in post connected queue.
	 * 
	 * @param outer OuterTask
	 */
	protected void enqueueInPostConnectedQueue(ContainerTask r) {
		postConnectedQueue.add(r);
	}

	/**The tasks which are supposed to be dispatched even before connected
	 * are submitted to this method.
	 * @param r Runnable
	 */
	protected void executeOrEnque(ContainerTask r) {		
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
		ContainerTask whatIsNext = null;
		if(transactionTracker.isIdle()){
			//No transaction in progress so pick up the next major task in transaction ready queue.
			if(isConnected()){
				whatIsNext = postConnectedQueue.poll();
			}else{
				whatIsNext = preConnectedQueue.poll();
			}
		}
		if(whatIsNext==null){
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
		taskHandler.destroy();
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

	public int getPostConnectedQueueSize() {
		return postConnectedQueue.size();
	}

	public Integer[] getTransactionsInProgress() {
		return transactionTracker.transactionsInProgress();
	}

	public int getPreConnectedQueueSize() {
		return preConnectedQueue.size();
	}
	
	public int getOpsInTransactionQueue() {
		return transactionTracker.getOpsInTransactionQueue();
	}
	
	public Collection<Integer> getTransactionReadyQueue() {
		return transactionTracker.getTransactionReadyQueue();
	}	

	public boolean log(String string) {
		return cl.log(string);
	}

	/**
	 * Run it on appropriate container thread. not the worker thread.
	 * @return
	 */
	protected boolean ensureExecutingInRightThread() {
		return taskHandler.ensureExecutingInRightThread(false);
	}
	
	/**If worker is true then It runs on worker thread, If worker is false runs it in container thread.
	 * 
	 * @param worker boolean
	 * @return boolean 
	 */
	protected boolean ensureExecutingInRightThread(boolean worker) {
		return taskHandler.ensureExecutingInRightThread(worker);
	}
	
	@Override
	public String toString() {
		return cl.getName();
	}
}