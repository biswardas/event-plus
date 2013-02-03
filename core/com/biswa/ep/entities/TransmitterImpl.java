package com.biswa.ep.entities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.biswa.ep.NamedThreadFactory;
/**Transmitter implementation to dispatch events to downstream containers.
 * 
 * @author biswa
 *
 */
public class TransmitterImpl implements Transmitter {
	/**Thread which deals with downstream container dynamic listeners.  
	 * 
	 */
	final private ExecutorService eventDispatcher;
	/**
	 * Creates the standard transmitter.
	 * @param name
	 */
	public TransmitterImpl(String name){
		eventDispatcher = Executors.newSingleThreadExecutor(new NamedThreadFactory("PPD-"+name));
	}
	@Override
	public void submit(Runnable r) {
		eventDispatcher.execute(r);
	}
	@Override
	public void destroy() {
		eventDispatcher.shutdown();
	}
}
