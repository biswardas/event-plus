package com.biswa.ep.entities.store;

import bak.pcj.map.IntKeyMap;
import bak.pcj.map.IntKeyOpenHashMap;

import com.biswa.ep.entities.ConcreteContainer;

/**
 * Concrete container entry creater.
 * @author biswa
 *
 */
class ConcreteContainerEntryStore implements ContainerEntryStore{
	private PhysicalEntry defaultEntry = null;
	/**
	 * Record Store which keeps the concrete records.
	 */
	final IntKeyMap containerDataEntries;
	final ConcreteContainer concreteContainer;
	public ConcreteContainerEntryStore(ConcreteContainer concreteContainer) {
		containerDataEntries = new IntKeyOpenHashMap(concreteContainer.expectedRowCount,concreteContainer.memOptimize);
		this.concreteContainer=concreteContainer;
	}

	@Override
	public PhysicalEntry getDefaultEntry() {
		return defaultEntry==null?(defaultEntry=new ConcreteContainerEntry()):defaultEntry;
	}
	
	@Override
	public PhysicalEntry create(int id){
		PhysicalEntry containerEntry =  new ConcreteContainerEntry(id);
		save(containerEntry);
		return containerEntry;
	}

	@Override
	public PhysicalEntry remove(int id) {
		return (PhysicalEntry) containerDataEntries.remove(id);
	}

	@Override
	public void clear() {
		containerDataEntries.clear();		
	}

	@Override
	public void save(PhysicalEntry containerEntry) {
		containerDataEntries.put(containerEntry.getIdentitySequence(), containerEntry);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public PhysicalEntry[] getEntries() {
		return (PhysicalEntry[]) containerDataEntries.values().toArray(new PhysicalEntry[0]);
	}
	
	@Override
	public PhysicalEntry getEntry(int id) {
		return (PhysicalEntry) containerDataEntries.get(id);
	}
}