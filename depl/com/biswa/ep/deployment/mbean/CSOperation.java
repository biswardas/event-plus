package com.biswa.ep.deployment.mbean;

import java.util.logging.Logger;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.Predicate;
import com.biswa.ep.entities.StaticLeafAttribute;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.provider.PredicateBuilder;

public class CSOperation implements CSOperationMBean {
	static final Logger logger = Logger.getLogger(CSOperation.class.getName());
	private AbstractContainer cs;
	private Thread pausedThread;
	
	public	CSOperation(AbstractContainer cs){
		this.cs=cs;
	}
	
	@Override
	public void removeAttribute(String name) {
		Attribute schemaAttribute = new LeafAttribute(name);
		ContainerEvent ce = new ContainerStructureEvent(cs.getName(),schemaAttribute);
		cs.agent().attributeRemoved(ce);
	}
	
	@Override
	public void dumpContainer(){
		cs.agent().invokeOperation(new ContainerTask() {			
			/**
			 * 
			 */
			private static final long serialVersionUID = 5527207032925018855L;

			@Override
			protected void runtask() {
				cs.dumpContainer();
			}
		});
	}

	@Override
	public void dumpMetaInfo(){
		cs.agent().invokeOperation(new ContainerTask() {			
			/**
			 * 
			 */
			private static final long serialVersionUID = -279485675748911793L;

			@Override
			protected void runtask() {
				cs.dumpMetaInfo();
			}
		});
	}

	@Override
	public void updateStatic(final String attributeName, final String value) {
		cs.agent().updateStatic(new StaticLeafAttribute(attributeName), value, null);
	}
	
	@Override
	public void updateStaticWithFilter(final String attributeName, final String value,final String filterString) {
		//Prepare the filter predicate
		Predicate pred = PredicateBuilder.buildPredicate(filterString);
		
		cs.agent().updateStatic(new StaticLeafAttribute(attributeName), value, new FilterSpec(cs.getName(),pred));
	}
	
	@Override
	public void latency() {
		cs.agent().invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1126185464526403783L;
			long createdAt = System.nanoTime();
			@Override
			protected void runtask() {
				logger.info("Latency Experiencing(ns) in container "+ContainerContext.CONTAINER.get().getName()+" :"+(System.nanoTime()-createdAt));
			}
		});	
	}
	@Override
	public void pauseForSeconds(final int duration) {
		cs.agent().invokeOperation(new ContainerTask() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8474806319078334502L;

			@Override
			protected void runtask() throws Exception{
				pausedThread = Thread.currentThread();
				Thread.sleep(duration*1000);
				pausedThread = null;
			}
		});			
	}
	@Override
	public String interruptPausedThread() {
		String message = null;
		if(pausedThread!=null){
			pausedThread.interrupt();
			message = pausedThread.getName() + " interrupted.";
			pausedThread = null;
		} else {
			message = "No threads to interrupt";
		}
		return message;
	}
}
