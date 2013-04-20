package com.biswa.ep.subscription;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.biswa.ep.NamedThreadFactory;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;

public class DummySubscriptionProcessor extends SubscriptionContainerProcessor {

	final private Set<ContainerEntry> containerEntrySet = new HashSet<ContainerEntry>();
	final private ScheduledThreadPoolExecutor eventDispatcher =new ScheduledThreadPoolExecutor(1,new NamedThreadFactory("Randomizer"));
	/**
	 * 
	 */
	private static final long serialVersionUID = -3323119285543564956L;
	//WeakHashMap instrumentMap = new WeakHashMap<K, V>();
	public DummySubscriptionProcessor() {
		super("DummySubscriptionProcessor");
	}

	@Override
	public Object subscribe(Attribute attribute,
			final ContainerEntry containerEntry) throws Exception {
		System.err.println("Subscribe Invoked"+attribute+containerEntry);
		eventDispatcher.execute(new Runnable(){
			@Override
			public void run() {
				containerEntrySet.add(containerEntry);		
				begin();
				update(containerEntry, Math.random());
				commit();		
			}			
		});
		return null;
	}

	@Override
	public void unsubscribe(final ContainerEntry containerEntry) {
		System.err.println("UnSubscribe Invoked"+containerEntry);
		eventDispatcher.execute(new Runnable(){
			@Override
			public void run() {
				containerEntrySet.remove(containerEntry);				
			}			
		});
		
	}
	@Override
	protected void init(){
		eventDispatcher.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if(!containerEntrySet.isEmpty()){
				try{
				begin();
				for(ContainerEntry conEntry:containerEntrySet){
					update(conEntry, Math.random());
				}
				commit();
				}catch(Exception e){
					e.printStackTrace();
				}
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	protected void terminate() {
		eventDispatcher.shutdownNow();
	};
}
