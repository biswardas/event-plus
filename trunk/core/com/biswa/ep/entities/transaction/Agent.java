package com.biswa.ep.entities.transaction;

import java.util.HashMap;
import java.util.Map;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ConnectionListener;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerListener;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.OuterTask;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.subscription.SubscriptionEvent;
import com.biswa.ep.subscription.SubscriptionSupport;
/**Any and every operation on the container must be tunneled through this listener to ensure
 * data and structural integrity. All Structural and Data event are executed on the PPL prefixed 
 * threads and every container runs in its own PPL thread.  
 * 
 * @author biswa
 *
 */
public class Agent extends TransactionAdapter implements ContainerListener,ConnectionListener,SubscriptionSupport {

	private Map<String, Boolean> expectationsMap = new HashMap<String, Boolean>();
	
	/** The containing schema under management.
	 * 
	 * @param cl
	 */
	public Agent(AbstractContainer cl) {
		super(cl);
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
				//No Need to implement default transaction as it is not supposed to
				//generate data events.
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -2038228996079905588L;

					public void runtask() {
						assert log("Received source addition:"+ce.getSource());
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
	public void attributeAdded(final ContainerEvent ce) {
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
						cl.attributeAdded(ce);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);
	}

	@Override
	public void attributeRemoved(final ContainerEvent ce) {
		OuterTask outer = new OuterTask(){
			@Override
			public void runouter() {
				//No Need to implement default transaction as it is not supposed to
				//generate data events.
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 3817691048842932303L;

					public void runtask() {
						cl.attributeRemoved(ce);
					}
				};
				executeOrEnque(r);
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
				ContainerTask r = new ContainerTask() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 2654384610901249637L;

					public void runtask() {
						cl.disconnected(ce);
					}
				};
				executeOrEnque(r);
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
				assert Boolean.FALSE.equals(expectationsMap.get(ce.getSource())):"This source was already connected but received connected message";
				expectationsMap.put(ce.getSource(),true);
				addTransactionSource(ce.getSource(),ce.getTransactionGroup());
				//Not yet Connected return
				if(expectationsMap.containsValue(false)) return;
				cl.connected(ce);

				checkQueuedTransaction();				
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
						cl.clear();
					}
				};
				executeOrEnquePostConnected(r);
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
						((SubscriptionSupport)cl).substitute(subscriptionEvent);
					}
				};
				executeOrEnque(r);
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
						cl.updateStatic(attribute,substance,appliedFilter);
					}
				};
				executeOrEnque(r);
			}
		};
		executeInListenerThread(outer);		
	}
}
