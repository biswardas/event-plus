package com.biswa.ep.entities;

import java.util.Map;
import java.util.concurrent.SynchronousQueue;
/**
 * Subscription Processor Contract Interface
 * @author Biswa
 *
 */
public interface IProcessor {

	void init(SynchronousQueue<Map<Object, Object>> queue);

	Object subscribe(Object object);

	void unsubscribe(Object object);

	void terminate();

}