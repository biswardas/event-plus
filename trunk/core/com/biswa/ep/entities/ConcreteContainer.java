package com.biswa.ep.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.biswa.ep.entities.store.ContainerEntryStore;
import com.biswa.ep.entities.store.ContainerStoreFactory;
import com.biswa.ep.entities.store.PhysicalEntry;
import com.biswa.ep.entities.substance.Substance;
/**The schema which deals with hosting Container Entry.
 * 
 * @author biswa
 *
 */
public class ConcreteContainer extends CascadeContainer{	
	/**
	 *Concrete record creation factory. 
	 */
	private final ContainerEntryStore containerEntryStore;
	
	//The id generated for the entry in this container. When the entries are propagated
	//the internalIdentity becomes the external Identity for the sink container
	private final Map<Integer,Integer> identityMap;

	/**
	 * Default Constructor
	 * @param name String
	 * @param props Properties
	 */
	public ConcreteContainer(String name,Properties props) {
		super(name,props);
		containerEntryStore = ContainerStoreFactory.getContainerEntryStore(this);
		if(identityConflictResolution){
			identityMap = new HashMap<Integer,Integer>(expectedRowCount,memOptimize);
		}else{
			identityMap = null;
		}
	}
	
	@Override
	public void entryUpdated(ContainerEvent containerEvent){
		Attribute attribute = containerEvent.getAttribute();
		//Obtain the registered attribute
		Attribute notifyingAttribute = attribute.getRegisteredAttribute();
		//Obtain the existing Entry being updated
		ContainerEntry containerEntry = containerEntryStore.getEntry(containerEvent.getIdentitySequence());
		if(containerEntry!=null){
			//Extract the substance received
			Substance substance = containerEvent.getSubstance();

			//Update entry with INCOMING attribute & notify listeners
			if(attribute.hasMinor()){
				substance = containerEntry.silentUpdate(notifyingAttribute, substance,attribute.getMinor());
			}else{
				substance = containerEntry.silentUpdate(notifyingAttribute, substance);				
			}
			
			//Dispatch the entry with registered attribute not with guest attribute
			dispatchEntryUpdated(notifyingAttribute,substance,containerEntry);
			
			//Update dependent Attributes and notify listeners
			for (Attribute notifiedAttribute : notifyingAttribute.getDependents()) {
				processNotifiedAttribute(notifyingAttribute, containerEntry,
						notifiedAttribute);
			}
			//Perform the stateless attribution
			performPostUpdateStatelessAttribution(containerEntry);
		}else{
			assert log("Received update on non existent entry"+containerEntry);
		}
	}	
	
	@Override
	public void entryAdded(ContainerEvent ce){
		TransportEntry transportEntry = ce.getTransportEntry();
		ContainerEntry containerEntry = null;
		if((containerEntry=getConcreteEntry(ce.getIdentitySequence()))!=null){
			switch(duplicateInsertMode){
				case 2://Ignore
						return;
				case 1://Merge
						performMergeModeAttribution(transportEntry, containerEntry);
						performPostUpdateStatelessAttribution(containerEntry);
						break;
				case 0://Delete & Insert
						//Receiving a duplicate entry, remove the entry first before adding
						entryRemoved(ce);
						assert log("Adding Entry: "+ce.toString());
						regularInsert(transportEntry);
			}			
		}else{
			regularInsert(transportEntry);
		}
	}
	
	/**Method does a regular insert along with its follow up process.
	 * 1. Create & Initialize
	 * 2. Perform Attribution
	 * 3. Dispatch 
	 * @param transportEntry TransportEntry
	 */
	private void regularInsert(TransportEntry transportEntry) {
		ContainerEntry containerEntry;
		containerEntry = createAndInitialize(transportEntry);
		performAttributionOnInitialEntrySet(transportEntry, containerEntry);
		dispatchEntryAdded(containerEntry);
	}
	
	/** Method creates an entry in the container, allocates an identity if required
	 * 
	 * @param transportEntry TransportEntry
	 * @return ContainerEntry
	 */
	private ContainerEntry createAndInitialize(TransportEntry transportEntry) {
		//Creating the new container entry
		ContainerEntry containerEntry = containerEntryStore.create(transportEntry.getIdentitySequence());
		//Manage the internal identity for the concrete entry 
		storeInternalIdentity(containerEntry);
		//Initialize all zero dependency attributes & attribute the constants
		for (Attribute notifiedAttribute : getSubscribedAttributes()) {
			if(notifiedAttribute.shouldInitializeOnInsert()){
				Substance substance = notifiedAttribute.failSafeEvaluate(null, containerEntry);
				containerEntry.silentUpdate(notifiedAttribute, substance);
			}
		}
		return containerEntry;
	}
	
	/**Method performs the attribution on initial entry set. We do not track any stateless attributes
	 * as upon insert all stateless attributes are attributed. There may be some stateless attributes which 
	 * are not dependent on any stateful attribute will never be attributed otherwise. 
	 * 
	 * @param transportEntry TransportEntry
	 * @param containerEntry ContainerEntry
	 */
	private void performAttributionOnInitialEntrySet(
			TransportEntry transportEntry, ContainerEntry containerEntry) {
		if(transportEntry.getEntryQualifier()!=null){			
			//For each incoming attribute attribute the dependency
			for(Attribute attribute:transportEntry.getEntryQualifier().keySet()){
				attribute = attribute.getRegisteredAttribute();
				containerEntry.silentUpdate(attribute, transportEntry.getEntryQualifier().get(attribute));
				//Update dependent Attributes and notify listeners
				//TODO Can we update all and perform the attribution just once?
				for (Attribute notifiedAttribute : attribute.getDependents()) {
					if(!notifiedAttribute.isStateless()){
						Substance substance = notifiedAttribute.failSafeEvaluate(attribute, containerEntry); 
						containerEntry.silentUpdate(notifiedAttribute, substance);			
					}
				}
			}
		}
	}

	/**Method performs the attribution on initial entry set in merge mode. In merge mode the 
	 * the incoming record is not sent over but only the changes are sent. During attribution 
	 * any stateless attributes are encountered are deferred for stateless attribution cycle. 
	 * 
	 * @param transportEntry TransportEntry
	 * @param containerEntry ContainerEntry
	 */
	private void performMergeModeAttribution(
			TransportEntry transportEntry, ContainerEntry containerEntry) {
		if(transportEntry.getEntryQualifier()!=null){
			//For each incoming attribute attribute the dependency
			for(Attribute attribute:transportEntry.getEntryQualifier().keySet()){
				attribute = attribute.getRegisteredAttribute();
				Substance initialSubstance = containerEntry.silentUpdate(attribute, transportEntry.getEntryQualifier().get(attribute));
				dispatchEntryUpdated(attribute,initialSubstance,containerEntry);
				//Update dependent Attributes and notify listeners
				//TODO Can we update all and perform the attribution just once?
				for (Attribute notifiedAttribute : attribute.getDependents()) {
					processNotifiedAttribute(attribute,containerEntry,notifiedAttribute);
				}
			}
		}
	}
	
	@Override
	public void entryRemoved(ContainerEvent ce) {
		assert log("Removing Entry"+ce.toString());
		ContainerEntry removedContainerEntry = containerEntryStore.remove(ce.getIdentitySequence());
		if(removedContainerEntry!=null){
			removeInternalIdentity(removedContainerEntry);
			//Dispatch entry removed if it was existing before.
			dispatchEntryRemoved(removedContainerEntry);
			//Method performs any attribute specific cleanup for this container Entry.
			destroy(removedContainerEntry);
		}else{
			assert log("Received delete on non existent entry"+ce.getIdentitySequence());
		}
	}
	
	@Override
	public ContainerEntry[] getContainerEntries() {
		return containerEntryStore.getEntries();
	}
	
	@Override
	public void clear() {
		for (ContainerEntry containerEntry: getContainerEntries()){
			dispatchEntryRemoved(containerEntry);
		}
		containerEntryStore.clear();
	}
	
	@Override
	final protected PhysicalEntry[] getContainerDataEntries() {
		return containerEntryStore.getEntries();
	}
	
	@Override
	public final ContainerEntry getConcreteEntry(int id) {
		return containerEntryStore.getEntry(id);
	}
	
	public ContainerEntryStore getContainerEntryStore() {
		return containerEntryStore;
	}

	/**Returns the internal identity of the record.
	 * 
	 * @param containerEntry ContainerEntry
	 * @return Integer
	 */
	public Integer getInternalIdentity(ContainerEntry containerEntry){
		if(identityConflictResolution){
			return identityMap.get(containerEntry.getIdentitySequence());
		}else{
			return containerEntry.getIdentitySequence();
		}
	}
	
	/**
	 * Creates the internal identity if enabled for the container
	 * @param ContainerEntry
	 */
	private void storeInternalIdentity(ContainerEntry containerEntry){
		if(identityConflictResolution){
			identityMap.put(containerEntry.getIdentitySequence(),generateIdentity());
		}
	}
	
	/**Removes the internal identity from the container.
	 * 
	 * @param containerEntry ContainerEntry
	 */
	private void removeInternalIdentity(ContainerEntry containerEntry){
		if(identityConflictResolution){
			identityMap.remove(containerEntry.getIdentitySequence());
		}
	}

	final public int expectedColCount;
	final public int expectedRowCount;
	final public float memOptimize;
	final public boolean identityConflictResolution;
	final public byte duplicateInsertMode;
	/**
	 * Loads the tuning properties for the container.
	 */
	{
		String strExpectCount = getProperties().getProperty(EXPECTED_COLUMN_COUNT);
		if(strExpectCount!=null){
			expectedColCount = Integer.parseInt(strExpectCount);
		} else { 
			expectedColCount = 16;
		}
		assert log("Estimated number of Columns in "+getName()+ " "+expectedColCount);

		String strProperty = getProperties().getProperty(RESOLVE_IDENTITY_CONFLICTS);
		if(strProperty!=null){
			identityConflictResolution = Boolean.parseBoolean(strProperty);
		} else {
			identityConflictResolution = false;
		}
		assert log("Identity Conflict Resolution in "+getName()+ " to "+identityConflictResolution);

		String strExpectRowCount = getProperties().getProperty(EXPECTED_ROW_COUNT);
		if(strExpectRowCount!=null){
			expectedRowCount = Integer.parseInt(strExpectRowCount);
		}else{
			expectedRowCount=16;
		}
		assert log("Estimated number of Records in "+getName()+ " "+expectedRowCount);

		String strMemOptimize = getProperties().getProperty(MEM_OPTIMIZATION);
		if(strMemOptimize!=null){
			memOptimize = Float.parseFloat(strMemOptimize);
		}else{
			memOptimize = 0.75f;
		}
		assert log("Mem optimized in "+getName()+ " to "+memOptimize);

		String strDuplInsertMode = getProperties().getProperty(DUPLICATE_INSERT_MODE);
		if(strDuplInsertMode!=null){
			duplicateInsertMode = Byte.parseByte(strDuplInsertMode);
		}else{
			duplicateInsertMode = 0;
		}
		assert log("Dupl insert mode in "+getName()+ " to "+duplicateInsertMode);
	}
}