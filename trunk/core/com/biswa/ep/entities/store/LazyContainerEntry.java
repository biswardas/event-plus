package com.biswa.ep.entities.store;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.substance.Substance;

class LazyContainerEntry extends PersistableContainerEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = -919932327675986409L;

	public LazyContainerEntry(PhysicalEntry containerEntry) {
		super(containerEntry);
	}

	@Override
	public Substance getSubstance(Attribute attribute) {
		wakeUp();
		return underlyingEntry.getSubstance(attribute);
	}

	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance) {
		wakeUp();
		return underlyingEntry.silentUpdate(attribute, substance);
	}

	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance,int minor) {
		wakeUp();
		return underlyingEntry.silentUpdate(attribute, substance, minor);
	}

	@Override
	public void remove(Attribute attribute) {
		wakeUp();
		underlyingEntry.remove(attribute);
	}

	@Override
	public void remove(Attribute attribute,int minor) {
		wakeUp();
		underlyingEntry.remove(attribute,minor);
	}
	
	@Override
	public TransportEntry cloneConcrete() {
		wakeUp();
		return underlyingEntry.cloneConcrete();
	}
	
	@Override
	public void reallocate(int size) {
		wakeUp();
		underlyingEntry.reallocate(size);
	}

	private void wakeUp() {
		if(markedPassivated()){
			activate((PassivableContainerEntryStore)getContainer().getContainerEntryStore());
		}
	}
}
