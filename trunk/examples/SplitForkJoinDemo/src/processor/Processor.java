package processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class Processor {
	private ScheduledThreadPoolExecutor stp = new ScheduledThreadPoolExecutor(1);
	private CopyOnWriteArraySet<Object> subscriptionSet = new CopyOnWriteArraySet<Object>();

	public void init(final SynchronousQueue<Map<Object, Object>> queue) {
		stp.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				if(!subscriptionSet.isEmpty()){
					Map<Object,Object> hm = new HashMap<Object,Object>();
					for(Object oneSub:subscriptionSet){
						hm.put(oneSub, Math.random());
					}
					try {
						queue.put(hm);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}, delay(), delay(), TimeUnit.MILLISECONDS);
	}
	public Object subscribe(Object object) {
		subscriptionSet.add(object);
		return Math.random();
	}

	public void unsubscribe(Object object) {
		subscriptionSet.remove(object);
	}
	public void terminate(){
		stp.shutdownNow();
	}

	protected int delay() {
		return 200;
	}

}