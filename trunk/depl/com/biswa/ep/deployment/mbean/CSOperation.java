package com.biswa.ep.deployment.mbean;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.StaticLeafAttribute;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.dyna.ConcreteAttributeProvider;
import com.biswa.ep.entities.predicate.Predicate;
import com.biswa.ep.entities.spec.AggrSpec;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.PivotSpec;
import com.biswa.ep.entities.spec.SortSpec;
import com.biswa.ep.provider.PredicateBuilder;

public class CSOperation implements CSOperationMBean {
	private ConcreteAttributeProvider attrProvider = new ConcreteAttributeProvider();
	private AbstractContainer cs;
	private Thread pausedThread;
	
	public	CSOperation(AbstractContainer cs){
		this.cs=cs;
	}
	@Override
	public void applyAggr(String aggrString) {
		AggrSpec aggrSpec = new AggrSpec();
		StringTokenizer stk = new StringTokenizer(aggrString,",");
		while(stk.hasMoreTokens()){
			String[] oneAttribute = stk.nextToken().split(":");
			aggrSpec.add(new LeafAttribute(oneAttribute[0]), Aggregators.valueOf(oneAttribute[1]).AGGR);
		}
		cs.agent().applySpec(aggrSpec);
	}

	@Override
	public void applyFilter(final String filterString) {
		//Prepare the filter predicate
		Predicate pred = PredicateBuilder.buildPredicate(filterString);
		
		//Apply the predicate on the target container		
		cs.agent().applySpec(new FilterSpec(pred));
	}

	@Override
	public void applyPivot(String pivotString) {
		StringTokenizer stk = new StringTokenizer(pivotString,",");
		List<Attribute> list = new ArrayList<Attribute>();
		while(stk.hasMoreTokens()){
			list.add(new LeafAttribute(stk.nextToken()));
		}
		PivotSpec pivotSpec = new PivotSpec(list.toArray(new Attribute[0]));
		cs.agent().applySpec(pivotSpec);
	}
	
	@Override
	public void applySort(String sortString) {
		StringTokenizer stk = new StringTokenizer(sortString,",");
		SortSpec sortSpec = new SortSpec();
		while(stk.hasMoreTokens()){
			sortSpec.addSortOrder(new LeafAttribute(stk.nextToken()),true);
		}
		cs.agent().applySpec(sortSpec);
	}
	
	@Override
	public void addBeanShellAttribute(String expression) {
		com.biswa.ep.entities.Attribute schemaAttribute = attrProvider.getAttribute(expression);
		ContainerEvent ce = new ContainerStructureEvent(cs.getName(),schemaAttribute);
		cs.agent().attributeAdded(ce);
	}
	
	@Override
	public void addKnownAttribute(String name) {
		try{
			Class<?> className = Class.forName(name);
			Attribute schemaAttribute = null;
			schemaAttribute = (com.biswa.ep.entities.Attribute) className.newInstance();
			ContainerEvent ce = new ContainerStructureEvent(cs.getName(),schemaAttribute);
			cs.agent().attributeAdded(ce);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
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
		
		cs.agent().updateStatic(new StaticLeafAttribute(attributeName), value, new FilterSpec(pred));
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
				System.out.println("Latency Experiencing(ns) in container "+ContainerContext.CONTAINER.get().getName()+" :"+(System.nanoTime()-createdAt));
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
