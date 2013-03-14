package com.biswa.ep.subscription;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerUpdateEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.TransactionEvent;

public abstract class SubscriptionContainerProcessor extends Subscription {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7094648212902888346L;
	/**
	 * Subject Attribute for this processor
	 */
	private transient final Attribute SUBJECT = new LeafAttribute("SUBJECT");
	
	{
		addDependency(SUBJECT);
	}
	/**
	 * The current transaction in progress.
	 */
	private transient int transactionId=0;
	
	/**
	 * The container associated with this Subscription processor
	 */
	private transient AbstractContainer subscriptionContainer = null;
	
	/**Constructor to create a Subscription processor
	 * 
	 * @param name String
	 */
	protected SubscriptionContainerProcessor(String name) {
		super(name);
	}
	
	/**Returns the Subject attribute for this processor.
	 * 
	 * @return Attribute
	 */
	protected Attribute getSubjectAttribute(){
		return SUBJECT;
	}
	
	/**Attach the container to this processor so the external world can communicate
	 * with this container.
	 * 
	 * @param subscriptionContainer
	 */
	protected void setContainer(AbstractContainer subscriptionContainer) {
		this.subscriptionContainer=subscriptionContainer;
		init();
	}
	
	/**
	 * Begins a transaction for the associated container
	 */
	final protected void begin(){
		transactionId = subscriptionContainer.agent().getNextTransactionID();
		subscriptionContainer.agent().beginTran(new TransactionEvent(subscriptionContainer.getName(), transactionId));
	}
	
	/**Method which receives all updates from an subscription service.
	 * 
	 * @param containerEntry
	 * @param substance
	 */
	final protected void update(ContainerEntry containerEntry,Substance substance){
		ContainerEvent updateEvent= new ContainerUpdateEvent(subscriptionContainer.getName(),
				containerEntry.getIdentitySequence(),this,substance,transactionId);
		subscriptionContainer.agent().entryUpdated(updateEvent);
	}

	/**
	 * Commits the transaction for the associated container.
	 */
	final protected void commit(){
		subscriptionContainer.agent().commitTran(new TransactionEvent(subscriptionContainer.getName(), transactionId));
	}

	/**
	 *Initialize the interaction with external world. 
	 */
	abstract protected void init();
	
	/**
	 *Disconnects interaction with with external world. 
	 */
	abstract protected void terminate();
}
