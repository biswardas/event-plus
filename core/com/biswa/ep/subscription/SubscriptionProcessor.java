package com.biswa.ep.subscription;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerUpdateEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.TransactionEvent;

public abstract class SubscriptionProcessor extends Subscription {
	/**
	 * The name by which the updates are sent to the underlying container.
	 */
	private static final String ANONYMOUS = "ANONYMOUS";
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
	private transient int transactionId=-1;
	
	/**
	 * The container associated with this Subscription processor
	 */
	private transient AbstractContainer channelContainer = null;
	
	/**Constructor to create a Subscription processor
	 * 
	 * @param name String
	 */
	protected SubscriptionProcessor(String name) {
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
	 * @param channelContainer
	 */
	protected void setContainer(AbstractContainer channelContainer) {
		this.channelContainer=channelContainer;
		init();
	}
	
	/**
	 * Begins a transaction for the associated container
	 */
	final protected void begin(){
		transactionId = channelContainer.agent().getNextTransactionID();
		channelContainer.agent().beginTran(new TransactionEvent(ANONYMOUS, transactionId));
	}
	
	/**Method which receives all updates from an subscription service.
	 * 
	 * @param containerEntry
	 * @param substance
	 */
	final protected void update(ContainerEntry containerEntry,Substance substance){
		ContainerEvent updateEvent= new ContainerUpdateEvent(ANONYMOUS,
				containerEntry.getIdentitySequence(),this,substance,transactionId);
		channelContainer.agent().entryUpdated(updateEvent);
	}

	/**
	 * Commits the transaction for the associated container.
	 */
	final protected void commit(){
		channelContainer.agent().commitTran(new TransactionEvent(ANONYMOUS, transactionId));
	}
	
	/**
	 *Initialize the interaction with external world. 
	 */
	abstract protected void init();
}
