package com.biswa.ep.subscription;

import java.util.HashMap;
import java.util.Map.Entry;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
/**
 * Default Client side subscription processor.
 * 
 * @author biswa
 *
 */
public class SubscriptionAttrHandlerImpl implements SubscriptionAttrHandler {
	private boolean isMulti(Object data){
		return data!=null && data.getClass().isArray();
	}
	
	@Override
	public Object substitute(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		Object data = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		if(isMulti(data)){
			return substituteMultiValue(subscriptionAttribute, containerEntry, (Object[])data);
		}else{
			return substituteSingleValue(subscriptionAttribute, containerEntry, data);
		}
	}
	
	private Object substituteSingleValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(data,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().substitute(subscriptionEvent);
		return data;
	}

	private Object substituteMultiValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object[] data) {
		HashMap<Integer,Object> currentSubscriptionSet =(HashMap<Integer,Object>)getMultiSubstance(
				subscriptionAttribute, containerEntry);
		
		if (data != null) {
			if (currentSubscriptionSet != null
					&& data.length == currentSubscriptionSet.size()) {
				Attribute responseAttribute = subscriptionAttribute
						.getResponseAttribute();
				
				int minor = 0;
				
				for (Object substance:data) {
					LeafAttribute leafAttribute = new LeafAttribute(
							responseAttribute.getName(), minor);
					
					SubscriptionRequest subRequest = new SubscriptionRequest(
							containerEntry.getContainer().getName(),
							containerEntry.getIdentitySequence(), leafAttribute);
					
					
					SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
							substance, subscriptionAttribute.getSource(),
							subRequest);
					
					subscriptionAttribute.getSubAgent().substitute(
							subscriptionEvent);
					
					currentSubscriptionSet.put(minor++, substance);
				}
			} else {
				unsubscribe(subscriptionAttribute,
						containerEntry);
				
				return subscribe(subscriptionAttribute,
						containerEntry);
			}
		}
		return currentSubscriptionSet;
	}
	
	@Override
	public Object unsubscribe(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		Object data = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		if(isMulti(data)){
			return unsubscribeMultiValue(subscriptionAttribute, containerEntry, (Object[])data);
		}else{
			return unsubscribeSingleValue(subscriptionAttribute, containerEntry,data);
		}
		
	}

	private Object unsubscribeSingleValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(data,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().unsubscribe(subscriptionEvent);
		return data;
	}

	private HashMap<Integer, Object> unsubscribeMultiValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data[]) {
		HashMap<Integer,Object> currentSubscriptionSet = getMultiSubstance(
				subscriptionAttribute, containerEntry);
		
		Attribute responseAttribute = subscriptionAttribute
				.getResponseAttribute();
		
		for (Entry<Integer, Object> oneEntry : currentSubscriptionSet.entrySet()) {
			LeafAttribute leafAttribute = new LeafAttribute(responseAttribute
					.getName(), oneEntry.getKey());
			
			SubscriptionRequest subRequest = new SubscriptionRequest(
					containerEntry.getContainer().getName(), containerEntry
							.getIdentitySequence(), leafAttribute);
			
			Object substance =oneEntry.getValue();
			
			SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
					substance, subscriptionAttribute.getSource(), subRequest);
			
			subscriptionAttribute.getSubAgent().unsubscribe(subscriptionEvent);
		}
		// Return the empty multi substance
		currentSubscriptionSet.clear();
		
		return currentSubscriptionSet;
	}
	@Override
	public Object subscribe(SubscriptionAttribute subscriptionAttribute,ContainerEntry containerEntry) {
		Object data = containerEntry.getSubstance(subscriptionAttribute.getDependsAttribute().getRegisteredAttribute());
		if(isMulti(data)){
			return subscribeMultiValue(subscriptionAttribute, containerEntry, (Object[])data);
		}else{
			return subscribeSingleValue(subscriptionAttribute, containerEntry, data);
		}
	}
	private Object subscribeSingleValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry.getContainer().getName(), containerEntry.getIdentitySequence(), subscriptionAttribute.getResponseAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(data,subscriptionAttribute.getSource(),subRequest);
		subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
		return data;
	}

	private HashMap<Integer, Object> subscribeMultiValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object[] data) {
		HashMap<Integer,Object> multiSubstance = getMultiSubstance(
				subscriptionAttribute, containerEntry);
		Attribute responseAttribute = subscriptionAttribute
				.getResponseAttribute();
		int minor = 0;
		
		for (Object substance:data) {
			LeafAttribute leafAttribute = new LeafAttribute(responseAttribute
					.getName(), minor);
			SubscriptionRequest subRequest = new SubscriptionRequest(
					containerEntry.getContainer().getName(), containerEntry
							.getIdentitySequence(), leafAttribute);
			
			
			SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
					substance, subscriptionAttribute.getSource(), subRequest);
			
			subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
			
			multiSubstance.put(minor++, substance);
		}
		return multiSubstance;
	}
	@SuppressWarnings("unchecked")
	private HashMap<Integer,Object> getMultiSubstance(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		HashMap<Integer,Object> currentSubscriptionSet = (HashMap<Integer,Object>) containerEntry
				.getSubstance(subscriptionAttribute);
		return currentSubscriptionSet == null ? new HashMap<Integer,Object>()
				: currentSubscriptionSet;
	}
}
