package com.biswa.ep.entities.store;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.TransportEntry;

class EagerContainerEntry extends PersistableContainerEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = -919932327675986409L;

	public EagerContainerEntry(PhysicalEntry containerEntry) {
		super(containerEntry);
	}

	@Override
	public Object getSubstance(Attribute attribute) {
		return underlyingEntry.getSubstance(attribute);
	}

	@Override
	public Object silentUpdate(Attribute attribute, Object substance) {
		return underlyingEntry.silentUpdate(attribute, substance);
	}

	@Override
	public Object silentUpdate(Attribute attribute, Object substance,int minor) {
		return underlyingEntry.silentUpdate(attribute, substance, minor);
	}
	
	@Override
	public void remove(Attribute attribute) {
		underlyingEntry.remove(attribute);
	}
	
	@Override
	public void remove(Attribute attribute,int minor) {
		underlyingEntry.remove(attribute,minor);
	}

	@Override
	public TransportEntry cloneConcrete() {
		return underlyingEntry.cloneConcrete();
	}	
	
	@Override
	public void reallocate(int size) {
		underlyingEntry.reallocate(size);
	}

	@Override
	public Object[] getSubstancesAsArray() {
		return underlyingEntry.getSubstancesAsArray();
	}
}
