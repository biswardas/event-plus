package com.biswa.ep.subscription;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;

/**
 * Client side multi value subscription processor.
 * 
 * @author biswa
 * 
 */
@SuppressWarnings("unchecked")
public class MultiSubAttrHandlerImpl implements SubscriptionAttrHandler {

	@Override
	public Object substitute(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {

		Collection<Object> data = (Collection<Object>) containerEntry
				.getSubstance(
						subscriptionAttribute.getDependsAttribute()
								.getRegisteredAttribute());
		
		HashMap<Integer,Object> currentSubscriptionSet =(HashMap<Integer,Object>)getMultiSubstance(
				subscriptionAttribute, containerEntry);
		
		if (data != null) {
			if (currentSubscriptionSet != null
					&& data.size() == currentSubscriptionSet.size()) {
				Attribute responseAttribute = subscriptionAttribute
						.getResponseAttribute();
				
				int minor = 0;
				Iterator<Object> iter = data.iterator();
				
				while (iter.hasNext()) {
					LeafAttribute leafAttribute = new LeafAttribute(
							responseAttribute.getName(), minor);
					
					SubscriptionRequest subRequest = new SubscriptionRequest(
							containerEntry.getContainer().getName(),
							containerEntry.getIdentitySequence(), leafAttribute);
					
					Object substance = iter.next();
					
					SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
							substance, subscriptionAttribute.getSource(),
							subRequest);
					
					subscriptionAttribute.getSubAgent().substitute(
							subscriptionEvent);
					
					currentSubscriptionSet.put(minor++, substance);
				}
			} else {
				currentSubscriptionSet = unsubscribe(subscriptionAttribute,
						containerEntry);
				
				currentSubscriptionSet = subscribe(subscriptionAttribute,
						containerEntry);
			}
		}
		return currentSubscriptionSet;
	}

	@Override
	public HashMap<Integer,Object> unsubscribe(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
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
	public HashMap<Integer,Object> subscribe(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		Collection<Object> data = (Collection<Object>) containerEntry
				.getSubstance(
						subscriptionAttribute.getDependsAttribute()
								.getRegisteredAttribute());
		HashMap<Integer,Object> multiSubstance = getMultiSubstance(
				subscriptionAttribute, containerEntry);
		Attribute responseAttribute = subscriptionAttribute
				.getResponseAttribute();
		int minor = 0;
		Iterator<Object> iter = data.iterator();
		while (iter.hasNext()) {
			LeafAttribute leafAttribute = new LeafAttribute(responseAttribute
					.getName(), minor);
			SubscriptionRequest subRequest = new SubscriptionRequest(
					containerEntry.getContainer().getName(), containerEntry
							.getIdentitySequence(), leafAttribute);
			
			Object substance = iter.next();
			
			SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
					substance, subscriptionAttribute.getSource(), subRequest);
			
			subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
			
			multiSubstance.put(minor++, substance);
		}
		return multiSubstance;
	}

	private HashMap<Integer,Object> getMultiSubstance(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		HashMap<Integer,Object> currentSubscriptionSet = (HashMap<Integer,Object>) containerEntry
				.getSubstance(subscriptionAttribute);
		return currentSubscriptionSet == null ? new HashMap<Integer,Object>()
				: currentSubscriptionSet;
	}
}
