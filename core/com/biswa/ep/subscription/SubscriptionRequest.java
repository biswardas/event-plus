package com.biswa.ep.subscription;

import java.io.Serializable;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.transaction.Agent;
/**The Subscription request object encompasses the information required for the subscription to
 * perform.
 * 
 * @author biswa
 *
 */
public class SubscriptionRequest implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8491410731474765649L;
	
	/**
	 * The entity id of the subscriber
	 */
	final private int id;
	
	/**
	 * The subscriber expects the update as following name.
	 */
	final private Attribute attribute;
	
	/**
	 * The sink which is subscribing
	 */
	final private String sink;
	
	/**
	 * Used to attach agent to this subscription.  
	 */
	transient private Agent agent;
	
	/**Constructor to create a subscription request object.
	 * 
	 * @param sink
	 * @param id
	 * @param attribute
	 */
	public SubscriptionRequest(String sink,int id,Attribute attribute){
		this.id=id;
		this.sink=sink;
		this.attribute=attribute;
		
	}
	/**
	 * Entity Id of the entry
	 * @return int
	 */
	public int getId() {
		return id;
	}
	
	/**The name by which the response is received.
	 * 
	 * @return Attribute
	 */
	public Attribute getAttribute() {
		return attribute;
	}
	
	/**Sink name associated with this subscription
	 * 
	 * @return String
	 */
	public String getSink() {
		return sink;
	}
	
	/**Attaches agent to the SubscriptionRequest
	 * 
	 * @param agent Agent
	 */
	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	/**Agent of this subscription.
	 * 
	 * @return Agent
	 */
	public Agent getAgent() {
		return agent;
	}
	
	@Override
	public String toString() {
		return "SubscriptionRequest [id=" + id + ", attribute=" + attribute
				+ ", sink=" + sink + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + id;
		result = prime * result + ((sink == null) ? 0 : sink.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubscriptionRequest other = (SubscriptionRequest) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (id != other.id)
			return false;
		if (sink == null) {
			if (other.sink != null)
				return false;
		} else if (!sink.equals(other.sink))
			return false;
		return true;
	}
}
