package com.biswa.ep.subscription;


import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.transaction.SubscriptionAgent;
/**
 * Client side subscription object for this container.
 * @author biswa
 *
 */
final public class SubscriptionAttribute extends Subscription {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7094648212902888346L;
	
	/**
	 * Handler dealing with subscription
	 */
	private SubscriptionAttrHandler attSubscription;
	/**
	 * Agent dealing with subscription
	 */
	private SubscriptionAgent subAgent;
	
	/**
	 * Name of the source to which the subscription event is being sent.
	 */
	private String source;
	
	private String depends;
	private String result;
	/**Constructor to create an subscription attribute.
	 * 
	 * @param depends String
	 * @param result String
	 * @param source String
	 * @param subAgent SubscriptionAgent
	 */
	public SubscriptionAttribute(String depends,String result,String source,SubscriptionAgent subAgent,SubscriptionAttrHandler attSubscription) {
		super(depends+result);
		this.source=source;
		this.subAgent = subAgent; 
		this.attSubscription=attSubscription;
		this.depends=depends;
		this.result=result;
		addDependency(new LeafAttribute(depends));
	}

	@Override
	final public boolean isSubscription() {
		return true;
	}
	
	@Override
	public Object subscribe(Attribute attribute,
			ContainerEntry containerEntry) throws Exception {
		return attSubscription.substitute(this,containerEntry);
	}
	
	/**Performs the unsubscription related activity in the client side and triggered upon
	 * the removal on the container entry.
	 * 
	 * @param containerEntry
	 */
	public void unsubscribe(ContainerEntry containerEntry) {
		attSubscription.unsubscribe(this,containerEntry);
	}
		
	public SubscriptionAgent getSubAgent() {
		return subAgent;
	}

	public Attribute getDependsAttribute() {
		return ContainerContext.CONTAINER.get().getAttributeByName(depends);
	}

	public String getSource() {
		return source;
	}

	public String getResult() {
		return result;
	}

	public Attribute getResponseAttribute() {
		return ContainerContext.CONTAINER.get().getAttributeByName(result);
	}
	
	public Object getLastSubscribed(ContainerEntry containerEntry){
		return containerEntry.getSubstance(this);
	}
}
