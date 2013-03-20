package processor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public abstract class MarketData {

	private final ScheduledThreadPoolExecutor stp = new ScheduledThreadPoolExecutor(1);
	private final Map<Object,Object> subscriptionSet = new ConcurrentHashMap<Object,Object>();

	public void init(final SynchronousQueue<Map<Object, Object>> queue) {
		stp.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				if(!subscriptionSet.isEmpty()){
					for(Object oneSub:subscriptionSet.keySet()){
						subscriptionSet.put(oneSub, new Quote(5-Math.random(),5+Math.random()));
					}
					try {
						queue.put(subscriptionSet);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}, updateDelay(), updateDelay(), TimeUnit.SECONDS);
	}

	public Object subscribe(Object object) {
		if(subscriptionSet.containsKey(object)){
			return subscriptionSet.get(object);
		}else{
			return subscriptionSet.put(object,new Quote(null, null));
		}
	}

	public void unsubscribe(Object object) {
		subscriptionSet.remove(object);
	}
	protected abstract int updateDelay();
	public void terminate(){
		stp.shutdownNow();
	}
}
