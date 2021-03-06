package com.biswa.ep.entities.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.AbstractContainer.FilterAgent;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ConnectionListener;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.LightWeightEntry;
import com.biswa.ep.entities.OuterTask;
import com.biswa.ep.entities.PropertyConstants;
import com.biswa.ep.subscription.SubscriptionEvent;
import com.biswa.ep.subscription.SubscriptionSupport;
/**Any and every operation on the container must be tunneled through this listener to ensure
 * data and structural integrity. All Structural and Data event are executed on the PPL prefixed 
 * threads and every container runs in its own PPL thread. Its imperative that no external world 
 * code ever invoke any operation on the container directly.
 * Container and agent has 1-1 relation ship.
 *   
 * 
 * @author biswa
 *
 */
public class Agent extends TransactionAdapter implements ContainerListener,ConnectionListener,SubscriptionSupport {
	/**
	 * Map containing all the upstream sources which are going to send messages through this
	 * agent. When a source sends connected to this value against the source name becomes
	 * true, When all sources report connected Container is ready to process messages.
	 */
	private final Map<String, Boolean> expectationsMap = new HashMap<String, Boolean>();
	
	private boolean allowAlien = true;
	
	/** Agent can only be constructed with an underlying container. No business code ever need to 
	 * create an agent manually. An agent must be accessed through AbstractContainer.agent()
	 * method.
	 * @param cl container under management.
	 */
	public Agent(AbstractContainer cl) {
		super(cl);
		String alienAttrString = cl.getProperty(PropertyConstants.ALLOW_ALIEN_ATTRIBUTES);
		if(alienAttrString!=null){
			allowAlien=Boolean.parseBoolean(cl.getProperty(PropertyConstants.ALLOW_ALIEN_ATTRIBUTES));
		}
	}

	@Override
	public void replay(final ConnectionEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -3711759506396173774L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.replay(ce);
					}
				};
				executeOrEnquePostConnected(r);	
			}
		};
		executeInListenerThread(outer);
	}


	@Override
	public void addSource(final ConnectionEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -2038228996079905588L;

					public void runtask() {
						assert log("Received source addition:"+ce);
						expectationsMap.put(ce.getSource(),false);
						if(cl.isConnected()){
							assert log("Here comes Mr. Bean, too early for next show.");
							//Turn off connected status if it was already connected.
							cl.disconnected(ce);
						}
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void dropSource(final ConnectionEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				assert log("Received source drop:"+ce);
				expectationsMap.remove(ce.getSource());
				transactionTracker.dropSource(ce.getSource());
				ContainerTask r = new ContainerTask() {

					/**
					 * 
					 */
					private static final long serialVersionUID = 2838421606291516488L;

					public void runtask() {
						checkQueuedTransaction();
					}
				};
				//We may be still in middle of transaction
				//Directly execute this task dont enque
				taskHandler.executeNow(r);
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void attributeAdded(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				if(allowAlien || notAlien(ce)){
					//Need to implement default transaction as it can
					//potentially generate data events
					ContainerTask r = new ContainerTask() {
						/**
						 * 
						 */
						private static final long serialVersionUID = 969041971977780486L;
	
						public void runtask() {
							assert ensureExecutingInRightThread();
							cl.attributeAdded(ce);
						}
					};
					//Following to make sure cascading attributes gets into the system before
					//directly added attributes
					if(isConnected()){
						executeOrEnque(r);					
					}else{
						if(notAlien(ce)){
							enqueueInPreConnectedQueue(r);
						}else{
							executeOrEnque(r);	
						}
					}
				}
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void attributeRemoved(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				if(allowAlien || notAlien(ce)){
					ContainerTask r = new ContainerTask() {
						/**
						 * 
						 */
						private static final long serialVersionUID = 969041971977780486L;
	
						public void runtask() {
							assert ensureExecutingInRightThread();
							cl.attributeRemoved(ce);
						}
					};
					//Following to make sure cascading attributes gets into the system before
					//directly added attributes
					if(isConnected()){
						executeOrEnque(r);					
					}else{
						if(notAlien(ce)){
							enqueueInPreConnectedQueue(r);
						}else{
							executeOrEnque(r);	
						}
					}
				}
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void disconnect(final ConnectionEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				//No Need to implement default transaction as it is not supposed to
				//generate data events.
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 3390398537432811489L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.disconnect(ce);
					}
				};
				executeOrEnquePostConnected(r);
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void disconnected(final ConnectionEvent ce){	
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				assert Boolean.TRUE.equals(expectationsMap.get(ce.getSource())):"This source was not connected but received disconnected message";
				expectationsMap.put(ce.getSource(),false);
				//Clear any pending transaction from this source
				transactionTracker.clearTransaction(ce.getSource());
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 2654384610901249637L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.disconnected(ce);
					}
				};
				executeOrEnque(r);
				//Check for any outstanding work
				checkQueuedTransaction();
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void connect(final ConnectionEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 338402669457934546L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.connect(ce);
					}
				};
				executeOrEnquePostConnected(r);	
			}
		};
		executeInListenerThread(outer);
	}
	
	@Override
	public void connected(final ConnectionEvent ce) {
		//No data events should be able to processed till the container received a connected message
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				assert log("Received source connected:"+ce);
				assert !Boolean.TRUE.equals(expectationsMap.get(ce.getSource())):"This source was already connected but received connected message";
				expectationsMap.put(ce.getSource(),true);
				transactionTracker.addSource(ce.getSource(),ce.getTransactionGroup());
				//Not yet Connected return
				if(expectationsMap.containsValue(false)) return;
				flushPreconnectedQueue();
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -6286061490419197136L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						cl.connected(ce);
					}
				};
				taskHandler.executeNow(r);
				

				checkQueuedTransaction();				
			}
		};
		executeInListenerThread(outer);
	}


	@Override
	public void subscribe(final SubscriptionEvent subscriptionEvent) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 19832482510541758L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						((SubscriptionSupport)cl).subscribe(subscriptionEvent);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);		
	}
	
	@Override
	public void unsubscribe(final SubscriptionEvent subscriptionEvent) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 4005059765756489435L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						((SubscriptionSupport)cl).unsubscribe(subscriptionEvent);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);		
	}
	
	@Override
	public void substitute(final SubscriptionEvent subscriptionEvent) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -5235163705114117576L;

					public void runtask() {
						assert ensureExecutingInRightThread();
						((SubscriptionSupport)cl).substitute(subscriptionEvent);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);		
	}
	
	private boolean notAlien(ContainerEvent ce){
		if(cl.getName().equals(ce.getSource())){
			return true;
		}
		return false;
	}
	/**
	 * Method exclusively for testing purposes and waits for event queue to drain
	 * for a post event analysis. There is no business requirement for this method.
	 */
	public void waitForEventQueueToDrain() {
		final Semaphore semaphore = new Semaphore(1);
		semaphore.drainPermits();
		getEventCollector().execute(new Runnable() {
			@Override
			public void run(){				
				semaphore.release();
			}
		});
		semaphore.acquireUninterruptibly();
	}


	private void executeWithRequestedIsolation(final int isolation,
			ContainerTask runnable) {
		switch(isolation){
			case 0:
				invokeOperation(runnable);
				break;
			case 1:
				getEventCollector().execute(runnable);
				break;
			default:
				runnable.run();
		}
	}
	
	public int getEntryCount(final String name,final int isolation){
		final AtomicInteger ai = new AtomicInteger(0);
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		ContainerTask runnable = new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					FilterAgent pa = cl.getFliterAgent(name);
					if(pa!=null){
						ai.set(pa.getEntryCount());
					}
				} finally {
					s.release();
				}
			}
		};
		executeWithRequestedIsolation(isolation, runnable);
		s.acquireUninterruptibly();
		return ai.get();
	}

	public LightWeightEntry getSortedEntry(final String name,final int id,final int isolation){
		final AtomicReference<LightWeightEntry> atom = new AtomicReference<LightWeightEntry>();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		ContainerTask runnable = new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					FilterAgent pa = cl.getFliterAgent(name);
					if(pa!=null){
						ContainerEntry conEntry = pa.getContainerEntries()[id];
						atom.set(new LightWeightEntry(conEntry.getIdentitySequence(), conEntry.getSubstancesAsArray()));
					}
				} finally {
					s.release();
				}
			}
		};

		executeWithRequestedIsolation(isolation, runnable);
		s.acquireUninterruptibly();
		return atom.get();
	}

	public String[] getAttributes(){
		final AtomicReference<String[]> atom = new AtomicReference<String[]>();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					atom.set(getContainer().getSubscribedAttrStrNames());
				} finally {
					s.release();
				}
			}
		});
		s.acquireUninterruptibly();
		return atom.get();
	}


	public Map<String,Class<? extends Object>> getTypeMap(){
		final AtomicReference<Map<String,Class<? extends Object>>> atom = new AtomicReference<Map<String,Class<? extends Object>>>();
		final Semaphore s = new Semaphore(1);
		s.drainPermits();
		invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6263129328893041488L;

			@Override
			protected void runtask() {
				try {
					atom.set(getContainer().getTypeMap());
				} finally {
					s.release();
				}
			}
		});
		s.acquireUninterruptibly();
		return atom.get();
	}
	public String getName() {
		return cl.getName();
	}
}
