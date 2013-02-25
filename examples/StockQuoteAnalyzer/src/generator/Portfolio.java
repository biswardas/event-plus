package generator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class Portfolio {
	private SynchronousQueue<Map<Object, Object>> queue = null;
	public void init(final SynchronousQueue<Map<Object, Object>> queue) throws InterruptedException {
		this.queue=queue;
		put("IBM",100.0,200.0);
		put("CSCO",500.0,20.0);
		put("NVDA",1000.0,15.0);
		put("ORCL",400.0,34.0);
		put("IBM",100.0,200.0);
		put("CSCO",500.0,20.0);
		put("NVDA",1000.0,15.0);
		put("ORCL",400.0,34.0);
	}
	private void put(String stock,Double quantity,Double tranPrice) throws InterruptedException{
		HashMap<Object,Object> hm2 = new HashMap<Object,Object>();
		hm2.put("symbol",stock);
		hm2.put("quantity",quantity);
		hm2.put("tranPrice",tranPrice);
		queue.put(hm2);
	}
}
