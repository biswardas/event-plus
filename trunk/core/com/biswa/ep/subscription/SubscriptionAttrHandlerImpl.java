package com.biswa.ep.subscription;

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
	private boolean isMulti(Object data) {
		return data != null && data.getClass().isArray();
	}

	@Override
	public Object substitute(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		Object data = containerEntry.getSubstance(subscriptionAttribute
				.getDependsAttribute().getRegisteredAttribute());
		if (isMulti(data)) {
			return substituteMultiValue(subscriptionAttribute, containerEntry,
					(Object[]) data);
		} else {
			return substituteSingleValue(subscriptionAttribute, containerEntry,
					data);
		}
	}

	private Object substituteSingleValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry
				.getContainer().getName(),
				containerEntry.getIdentitySequence(),
				subscriptionAttribute.getResponseAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(data,
				subscriptionAttribute.getSource(), subRequest);
		subscriptionAttribute.getSubAgent().substitute(subscriptionEvent);
		return data;
	}

	private Object substituteMultiValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object[] data) {
		Object[] currentSubscriptionSet = (Object[]) getMultiSubstance(
				subscriptionAttribute, containerEntry,0);		
		if (data.length == currentSubscriptionSet.length) {
			return substituteMultiValue(subscriptionAttribute, containerEntry, data);
		} else {
			unsubscribe(subscriptionAttribute, containerEntry);

			return subscribe(subscriptionAttribute, containerEntry);
		}
	}

	@Override
	public Object unsubscribe(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		Object data = containerEntry.getSubstance(subscriptionAttribute
				.getDependsAttribute().getRegisteredAttribute());
		if (isMulti(data)) {
			return unsubscribeMultiValue(subscriptionAttribute, containerEntry,
					(Object[]) data);
		} else {
			return unsubscribeSingleValue(subscriptionAttribute,
					containerEntry, data);
		}

	}

	private Object unsubscribeSingleValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry
				.getContainer().getName(),
				containerEntry.getIdentitySequence(),
				subscriptionAttribute.getResponseAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(data,
				subscriptionAttribute.getSource(), subRequest);
		subscriptionAttribute.getSubAgent().unsubscribe(subscriptionEvent);
		return data;
	}

	private Object[] unsubscribeMultiValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data[]) {
		Object[] currentSubscriptionSet = getMultiSubstance(subscriptionAttribute, containerEntry,0);

		Attribute responseAttribute = subscriptionAttribute
				.getResponseAttribute();

		for (int minor=0;minor<currentSubscriptionSet.length;minor++) {
			LeafAttribute leafAttribute = new LeafAttribute(
					responseAttribute.getName(), minor);

			SubscriptionRequest subRequest = new SubscriptionRequest(
					containerEntry.getContainer().getName(),
					containerEntry.getIdentitySequence(), leafAttribute);

			SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
					currentSubscriptionSet[minor], subscriptionAttribute.getSource(), subRequest);

			subscriptionAttribute.getSubAgent().unsubscribe(subscriptionEvent);
			currentSubscriptionSet[minor]=null;
		}
		return currentSubscriptionSet;
	}

	@Override
	public Object subscribe(SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry) {
		Object data = containerEntry.getSubstance(subscriptionAttribute
				.getDependsAttribute().getRegisteredAttribute());
		if (isMulti(data)) {
			return subscribeMultiValue(subscriptionAttribute, containerEntry,
					(Object[]) data);
		} else {
			return subscribeSingleValue(subscriptionAttribute, containerEntry,
					data);
		}
	}

	private Object subscribeSingleValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object data) {
		SubscriptionRequest subRequest = new SubscriptionRequest(containerEntry
				.getContainer().getName(),
				containerEntry.getIdentitySequence(),
				subscriptionAttribute.getResponseAttribute());
		SubscriptionEvent subscriptionEvent = new SubscriptionEvent(data,
				subscriptionAttribute.getSource(), subRequest);
		subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
		return data;
	}

	private Object[] subscribeMultiValue(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, Object[] data) {
		Attribute responseAttribute = subscriptionAttribute
				.getResponseAttribute();
		for (int minor = 0; minor < data.length; minor++) {
			LeafAttribute leafAttribute = new LeafAttribute(
					responseAttribute.getName(), minor);
			SubscriptionRequest subRequest = new SubscriptionRequest(
					containerEntry.getContainer().getName(),
					containerEntry.getIdentitySequence(), leafAttribute);

			SubscriptionEvent subscriptionEvent = new SubscriptionEvent(
					data[minor], subscriptionAttribute.getSource(), subRequest);

			subscriptionAttribute.getSubAgent().subscribe(subscriptionEvent);
		}
		return data;
	}

	private Object[] getMultiSubstance(
			SubscriptionAttribute subscriptionAttribute,
			ContainerEntry containerEntry, int length) {
		Object[] currentSubscriptionSet = (Object[]) containerEntry
				.getSubstance(subscriptionAttribute);
		return currentSubscriptionSet == null ? new Object[length]
				: currentSubscriptionSet;
	}
}
