package com.biswa.ep;

import java.util.PriorityQueue;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.StatelessContainerEntry;
/**Container information associated with 
 * 
 * @author biswa
 *
 */
public class ContainerContext {
	public static final ThreadLocal<AbstractContainer> CONTAINER = new ThreadLocal<AbstractContainer>();
	public static final ThreadLocal<PriorityQueue<Attribute>> STATELESS_QUEUE = new ThreadLocal<PriorityQueue<Attribute>>();
	public static final ThreadLocal<StatelessContainerEntry> SLC_ENTRY = new ThreadLocal<StatelessContainerEntry>();
	public static void initialize(AbstractContainer container){
		CONTAINER.set(container);
		STATELESS_QUEUE.set(new PriorityQueue<Attribute>());
		SLC_ENTRY.set(new StatelessContainerEntry());
	}
}
