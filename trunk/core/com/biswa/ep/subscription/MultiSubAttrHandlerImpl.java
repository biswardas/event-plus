package com.biswa.ep.subscription;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.MultiSubstance;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

/**
 * Client side multi value subscription processor.
 * 
 * @author biswa
 * 
 */
@SuppressWarnings("unchecked")
public class MultiSubAttrHandlerImpl implements SubscriptionAttrHandler {

	@Override
	public Substance substitute(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {

		Collection<Object> data = (Collection<Object>) containerEntry
				.getSubstance(
						subscriptionAttribute.getDependsAttribute()
								.getRegisteredAttribute()).getValue();
		
		MultiSubstance currentSubscriptionSet = getMultiSubstance(
				subscriptionAttribute, containerEntry);
		
		if (data != null) {
			if (currentSubscriptionSet != null
					&& data.size() == currentSubscriptionSet.getValue().size()) {
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
					
					Substance substance = new ObjectSubstance(iter.next());
					
					SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
							substance, subscriptionAttribute.getSource(),
							subRequest);
					
					subscriptionAttribute.getSubAgent().substitute(
							subscriptionEvent);
					
					currentSubscriptionSet.addValue(minor++, substance);
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
	public MultiSubstance unsubscribe(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		MultiSubstance currentSubscriptionSet = getMultiSubstance(
				subscriptionAttribute, containerEntry);
		
		Attribute responseAttribute = subscriptionAttribute
				.getResponseAttribute();
		
		for (Entry<Integer, Object> oneEntry : currentSubscriptionSet
				.getValue().entrySet()) {
			LeafAttribute leafAttribute = new LeafAttribute(responseAttribute
					.getName(), oneEntry.getKey());
			
			SubscriptionRequest subRequest = new SubscriptionRequest(
					containerEntry.getContainer().getName(), containerEntry
							.getIdentitySequence(), leafAttribute);
			
			Substance substance = new ObjectSubstance(oneEntry.getValue());
			
			SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
					substance, subscriptionAttribute.getSource(), subRequest);
			
			subscriptionAttribute.getSubAgent().unsubscribe(subscriptionEvent);
		}
		// Return the empty multi substance
		currentSubscriptionSet.getValue().clear();
		
		return currentSubscriptionSet;
	}

	@Override
	public MultiSubstance subscribe(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		Collection<Object> data = (Collection<Object>) containerEntry
				.getSubstance(
						subscriptionAttribute.getDependsAttribute()
								.getRegisteredAttribute()).getValue();
		MultiSubstance multiSubstance = getMultiSubstance(
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
			
			Substance substance = new ObjectSubstance(iter.next());
			
			SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
					substance, subscriptionAttribute.getSource(), subRequest);
			
			subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
			
			multiSubstance.addValue(minor++, substance);
		}
		return multiSubstance;
	}

	private MultiSubstance getMultiSubstance(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		MultiSubstance currentSubscriptionSet = (MultiSubstance) containerEntry
				.getSubstance(subscriptionAttribute);
		return currentSubscriptionSet == null ? new MultiSubstance()
				: currentSubscriptionSet;
	}
}
