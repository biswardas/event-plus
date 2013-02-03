package com.biswa.ep.entities;

import java.util.HashMap;
import java.util.Map;

import com.biswa.ep.entities.substance.Substance;

final public class StatelessContainerEntry implements ContainerEntry {
	
	/**
	 *
	 */
	private static final long serialVersionUID = -2520794970456968995L;
	private ContainerEntry containerEntry;
	private Map<Attribute,Substance> storage = new HashMap<Attribute,Substance>();

	public void setStatelessContainerEntry(ContainerEntry containerEntry){
		this.containerEntry=containerEntry;
		storage.clear();
	}
	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance) {
		storage.put(attribute,substance);
		return substance;
	}
	
	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance,
			int minor) {
		return silentUpdate(attribute,substance);
	}
	
	@Override
	public Substance getSubstance(Attribute attribute) {
		if(attribute.isStateless()){
			return storage.get(attribute);
		}else{
			return containerEntry.getSubstance(attribute);
		}
	}

	@Override
	public void remove(Attribute attribute) {
		storage.remove(attribute);
	}

	@Override
	public void remove(Attribute attribute, int minor) {
		remove(attribute);
	}
	
	@Override
	public int getToClient() {
		return containerEntry.getToClient();
	}

	@Override
	public boolean isFiltered(int agentPrimeIdentity) {
		return containerEntry.isFiltered(agentPrimeIdentity);
	}

	@Override
	public void setFiltered(int agentPrimeIdentity, boolean filteredResult) {
		containerEntry.setFiltered(agentPrimeIdentity,filteredResult);
	}

	@Override
	public boolean isLeftTrueRightFalse() {
		return containerEntry.isLeftTrueRightFalse();
	}

	@Override
	public void setLeftTrueRightFalse(boolean leftTrueRightFalse) {
		containerEntry.setLeftTrueRightFalse(leftTrueRightFalse);
	}
	
	@Override
	public ConcreteContainer getContainer() {
		return containerEntry.getContainer();
	}

	@Override
	public int getIdentitySequence() {
		return containerEntry.getIdentitySequence();
	}

	@Override
	public boolean markedAdded() {
		return containerEntry.markedAdded();
	}

	@Override
	public boolean markedRemoved() {
		return containerEntry.markedRemoved();
	}

	@Override
	public boolean markedDirty() {
		return containerEntry.markedDirty();
	}

	@Override
	public void markAdded(boolean flag) {
		containerEntry.markAdded(flag);
	}

	@Override
	public void markRemoved(boolean flag) {
		containerEntry.markRemoved(flag);
	}

	@Override
	public void markDirty(boolean flag) {
		containerEntry.markDirty(flag);
	}

	@Override
	public void reset() {
		containerEntry.reset();
	}

	@Override
	public int touchMode() {
		return containerEntry.touchMode();
	}

	@Override
	public TransportEntry cloneConcrete() {
		return containerEntry.cloneConcrete();
	}

	@Override
	public int getInternalIdentity() {
		return containerEntry.getInternalIdentity();
	}
}
