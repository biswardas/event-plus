package com.biswa.ep.subscription;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerDeleteEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerInsertEvent;
import com.biswa.ep.entities.ContainerUpdateEvent;
import com.biswa.ep.entities.TransportEntry;
/**Concrete Subscription handler which manages the subscriptions on behalf of the container.
 * Subscription handler can be initialized in two modes.
 * Proxy mode: In proxy mode this handler behaves like a pass through container.<br>
 * Non-Proxy mode: In non proxy mode this handler acts on behalf of a feedback container where it 
 * collects the updates and wait till the underlying container instructs to relay it.<br>
 * @author biswa
 *
 */
public class SubscriptionContainerHandler implements SubscriptionSupport {
	
	/**
	 * Processor associated with this container.
	 */
	private SubscriptionContainerProcessor processor;
	
	/**
	 * Each subject maps to an entry in this container.
	 */
	private Map<Object,Integer> subjectEntryMap =  new HashMap<Object,Integer>();
	
	/**
	 * Each subject can be subscribed by multiple Subscribers.
	 */
	private Map<Integer,SubscriptionRequest[]> entrySubscriptionRequestsMap =  new HashMap<Integer,SubscriptionRequest[]>();
	
	/**
	 *Each Subscriber can subscribe to a subject just once. 
	 */
	private Map<SubscriptionRequest,Object> subscriberSubjectMap =  new HashMap<SubscriptionRequest,Object>();

	//Transient subject which is being worked on.Used while a new subscription is made 
	//or an subject being unsubscribed.
	private Object tempSubject;
	
	/**
	 * Associated container which this handler is tied to 
	 */
	private AbstractContainer subsContainerSupport;	
		
	/**Creates a Subscription Handler.
	 * 
	 * @param container
	 */
	public SubscriptionContainerHandler(AbstractContainer container) {
		this.subsContainerSupport=container;
	}	
	
	@Override
	public void subscribe(SubscriptionEvent subscriptionEvent) {
		tempSubject = subscriptionEvent.getSubject();
		SubscriptionRequest subRequest = subscriptionEvent.getSubscriptionRequest();
		assert subsContainerSupport.log("Subscribing:"+subRequest+" to " +tempSubject);
		subscriberSubjectMap.put(subRequest, tempSubject);
		Integer conEntryId = subjectEntryMap.get(tempSubject);
		if(conEntryId==null){
			assert subsContainerSupport.log(tempSubject + " subject creation called");
			int identity = subsContainerSupport.generateIdentity();
			//Create Container entry
			Map<Attribute,Object> entryQualifier = new HashMap<Attribute,Object>();
			entryQualifier.put(processor.getSubjectAttribute(),tempSubject);
			ContainerEvent insertEvent = new ContainerInsertEvent(subsContainerSupport.getName(), new TransportEntry(identity,entryQualifier),0);
			subsContainerSupport.entryAdded(insertEvent);
			conEntryId = subjectEntryMap.get(tempSubject);
			addNewSubscriber(subRequest, conEntryId);
		}else{
			addNewSubscriber(subRequest, conEntryId);
			ContainerEntry conEntry = subsContainerSupport.getConcreteEntry(conEntryId);
			dispatchEntryUpdated(subRequest,conEntry.getSubstance(processor));
		}
	}

	
	/**Adds new subscriber to the the given subject.
	 * 
	 * @param subRequest SubscriptionRequest
	 * @param conEntryId Integer
	 * @return SubscriptionRequest[]
	 */
	private SubscriptionRequest[] addNewSubscriber(SubscriptionRequest subRequest,
			Integer conEntryId) {
		//Attach agent to the subscription request
		subRequest.setAgent(subsContainerSupport.getFliterAgent(subRequest.getSink()).agent);
		//Add subscription to the list
		SubscriptionRequest[] existingSubscribers = entrySubscriptionRequestsMap.get(conEntryId);
		SubscriptionRequest[] newSubscribers = new SubscriptionRequest[existingSubscribers.length+1];
		newSubscribers[0] = subRequest;
		System.arraycopy(existingSubscribers, 0, newSubscribers, 1, existingSubscribers.length);
		entrySubscriptionRequestsMap.put(conEntryId,newSubscribers);
		return newSubscribers;
	}	
	@Override
	public void unsubscribe(SubscriptionEvent subscriptionEvent) {
		tempSubject = subscriptionEvent.getSubject();
		Integer conEntryId = subjectEntryMap.get(tempSubject);
		if(conEntryId!=null){
			SubscriptionRequest[] subscribers=entrySubscriptionRequestsMap.get(conEntryId);
			SubscriptionRequest subRequest = subscriptionEvent.getSubscriptionRequest();
			assert subsContainerSupport.log("UnSubscribing:"+subRequest+" to " +tempSubject);
			subscriberSubjectMap.remove(subRequest);
			subscribers=removeSubscriber(subRequest,conEntryId);
			if(subscribers.length==0){
				ContainerEvent deleteEvent = new ContainerDeleteEvent(subsContainerSupport.getName(), conEntryId,0);
				subsContainerSupport.entryRemoved(deleteEvent);
				subjectEntryMap.remove(tempSubject);
			}
		}
	}
	
	/**Removes the subscriber from the given subject.
	 * 
	 * @param subRequest SubscriptionRequest
	 * @param conEntryId Integer
	 * @return SubscriptionRequest[]
	 */
	private SubscriptionRequest[] removeSubscriber(SubscriptionRequest subRequest,
			Integer conEntryId) {
		SubscriptionRequest[] existingSubscribers = entrySubscriptionRequestsMap.get(conEntryId);
		int index = 0;
		boolean found = false;
		for(index=0;index<existingSubscribers.length;index++){
			if(existingSubscribers[index].equals(subRequest)){
				found = true;
				break;
			}
		}
		SubscriptionRequest[] newSubscribers;
		if(!found){
			newSubscribers = existingSubscribers;
		}else{
			newSubscribers = new SubscriptionRequest[existingSubscribers.length-1];
			//Copy till index
			System.arraycopy(existingSubscribers, 0, newSubscribers, 0, index);
			//Skip and compact
			System.arraycopy(existingSubscribers, index+1, newSubscribers, index, newSubscribers.length-index);
		}
		entrySubscriptionRequestsMap.put(conEntryId,newSubscribers);
		return newSubscribers;
	}

	@Override
	public void substitute(SubscriptionEvent subscriptionEvent) {
		SubscriptionRequest subRequest = subscriptionEvent.getSubscriptionRequest();
		Object substance = subscriberSubjectMap.get(subRequest);
		if(substance!=null){
			SubscriptionEvent adjSubscriptionEvent = new SubscriptionEvent(substance, subsContainerSupport.getName(), subRequest);
			unsubscribe(adjSubscriptionEvent);
		}
		subscribe(subscriptionEvent);
	}
	
	/**Subscription handler does not propagate any attribute additions.
	 * 
	 * @param requestedAttribute Attribute
	 */
	public void dispatchAttributeAdded(Attribute requestedAttribute) {
		if(requestedAttribute instanceof SubscriptionContainerProcessor){
			processor=(SubscriptionContainerProcessor) requestedAttribute;
			//Set the container in which the subscription is operating
			processor.setContainer(subsContainerSupport);
		}
		
	}
	
	/**This is the call back from the associated container in the event of an entry update.
	 * 
	 * @param attribute Attribute
	 * @param substance Substance
	 * @param containerEntry ContainerEntry
	 */
	public void dispatchEntryUpdated(Attribute attribute, Object substance,
			ContainerEntry containerEntry) {
		SubscriptionRequest[] subscribers = entrySubscriptionRequestsMap.get(containerEntry.getIdentitySequence());
		for(SubscriptionRequest subrequest:subscribers){
			dispatchEntryUpdated(subrequest, substance);	
		}
	}
	
	/**This is the call back from the associated container in the process to throttle updates.
	 * 
	 * @param attribute Attribute
	 * @param substance Substance
	 * @param containerEntry ContainerEntry
	 */
	public void collectUpdates(Attribute attribute, Object substance,
		ContainerEntry containerEntry) {
		containerEntry.markDirty(true);
	}
	
	/**This method does the actual hand over of entry update to interested subscriber.
	 * 
	 * @param subscriptionRequest SubscriptionRequest 
	 * @param substance Substance
	 */
	private void dispatchEntryUpdated(final SubscriptionRequest subscriptionRequest,Object substance) {
		final ContainerEvent containerEvent = new ContainerUpdateEvent(subsContainerSupport.getName(),subscriptionRequest.getId(),subscriptionRequest.getAttribute(),substance,subsContainerSupport.getCurrentTransactionID());
		subsContainerSupport.getEventDispatcher().submit(new Runnable(){
			public void run(){
				subscriptionRequest.getAgent().entryUpdated(containerEvent);
			}
		});
	}
	
	/**Registers the entry with this handler. this is invoked wrt to an entry created
	 * as result of a client subscription.
	 * 
	 * @param containerEntry
	 */
	public void register(ContainerEntry containerEntry) {
		subjectEntryMap.put(tempSubject, containerEntry.getIdentitySequence());
		entrySubscriptionRequestsMap.put(containerEntry.getIdentitySequence(), new SubscriptionRequest[0]);
	}
	
	/**Unregisters the entry from this handler. This is invoked in response to a clean unsubscription.
	 * 
	 * @param containerEntry
	 */
	public void unregister(ContainerEntry containerEntry) {
		//Notify the processor
		entrySubscriptionRequestsMap.remove(containerEntry.getIdentitySequence());
	}
	
	/**
	 *Dispatch all the collected updates to respective Subscribers. 
	 */
	public void processCollectedUpdates(){
		for(ContainerEntry entry:subsContainerSupport.getContainerEntries()){
			if(entry.markedDirty()){
				Object substance = entry.getSubstance(processor);
				SubscriptionRequest[] subscribers = entrySubscriptionRequestsMap.get(entry.getIdentitySequence());
				for(SubscriptionRequest subrequest:subscribers){
					dispatchEntryUpdated(subrequest, substance);
				}
				entry.markDirty(false);
			}
		}
	}
	
	/**Remove all subscriptions associated with this sink.
	 * 
	 * @param connectionEvent
	 */
	public void disconnect(ConnectionEvent connectionEvent) {
		String sink = connectionEvent.getSink();
		Iterator<SubscriptionRequest> iter = subscriberSubjectMap.keySet().iterator();
		while(iter.hasNext()){
			SubscriptionRequest subRequest = iter.next();
			if (sink.equals(subRequest.getSink())){
				Object subject = subscriberSubjectMap.get(subRequest);
				assert subsContainerSupport.log("Unsubscribing:"+subRequest+" from " +subject);
				iter.remove();
				Integer conEntryId = subjectEntryMap.get(subject);
				if(conEntryId!=null){
					SubscriptionRequest[] subscribers=removeSubscriber(subRequest,conEntryId);
					if(subscribers.length==0){
						ContainerEvent deleteEvent = new ContainerDeleteEvent(subsContainerSupport.getName(), conEntryId,0);
						subsContainerSupport.entryRemoved(deleteEvent);
						subjectEntryMap.remove(subject);
					}
				}
			}
		}
		assert subsContainerSupport.log("SubjectEntryMap Size:"+subjectEntryMap.size());
		assert subsContainerSupport.log("EntrySubscriberMap Size:"+entrySubscriptionRequestsMap.size());
		assert subsContainerSupport.log("SubscriberSubjectMap Size:"+subscriberSubjectMap.size());
	}

	public void terminate() {
		processor.terminate();
	}
}
