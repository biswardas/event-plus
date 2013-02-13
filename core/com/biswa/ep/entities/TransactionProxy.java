package com.biswa.ep.entities;

import java.util.Properties;

import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.Agent;

/**
 * This container only relays the transaction. No data or attributes are
 * propagated from this container. Purpose of this container is to uniquely
 * group the transactions in downstream container. Think about forming a perfect
 * diamond.
 * 
 * @author biswa
 * 
 */
public class TransactionProxy extends AbstractContainer {

	public TransactionProxy(String name, Properties props) {
		super(name, props);
	}

	@Override
	public void connect(final ConnectionEvent connectionEvent) {
		assert isConnected() : "How the hell did you reach here";
		final Agent dcl = connectionEvent.getAgent();
		getEventDispatcher().submit(new Runnable() {
			public void run() {
				dcl.connected(connectionEvent);
			}
		});

		final FilterAgent filterAgent = buildFilterAgent(
				connectionEvent.getSink(), dcl);
		// 3. Add the target container to the listener list
		listenerMap.put(connectionEvent.getSink(), filterAgent);
		FilterSpec incomingFilter = connectionEvent.getFilterSpec();
		if (incomingFilter != null) {
			incomingFilter = incomingFilter.prepare();
			filterAgent.setFilterSpec(filterSpec.chain(incomingFilter));
		} else {
			filterAgent.setFilterSpec(filterSpec);
		}
	}

	@Override
	public void replay(final ConnectionEvent connectionEvent) {
	}

	@Override
	public void entryAdded(ContainerEvent ce) {
	}

	@Override
	public void entryRemoved(ContainerEvent ce) {
	}

	@Override
	public void entryUpdated(ContainerEvent ce) {
	}

	@Override
	public void clear() {
	}

	@Override
	public void attributeAdded(ContainerEvent ce) {
	}

	@Override
	public void attributeRemoved(ContainerEvent ce) {
	}

	@Override
	protected Attribute[] getSubscribedAttributes() {
		return null;
	}

	@Override
	protected Attribute[] getStatelessAttributes() {
		return null;
	}

	@Override
	protected Attribute[] getStaticAttributes() {
		return null;
	}

	@Override
	protected String[] getAllAttributeNames() {
		return null;
	}

	@Override
	public Attribute getAttributeByName(String attributeName) {
		return null;
	}

	@Override
	public ContainerEntry[] getContainerEntries() {
		return null;
	}

	@Override
	public ContainerEntry getConcreteEntry(int id) {
		return null;
	}

	@Override
	public void updateStatic(Attribute attribute, Substance substance,
			FilterSpec appliedFilter) {
	}
}
