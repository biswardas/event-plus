package com.biswa.ep.entities.predicate;

import java.util.Map;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.substance.Substance;

public class MyContainerEntry implements ContainerEntry {
	protected int id;
	//Each Attribute and substance are kept here for this record
	protected Map<Attribute, Substance> containerEntryStore;
	public MyContainerEntry(int id,Map<Attribute, Substance> hm){
		this.id=id;
		this.containerEntryStore = hm;
	}
	@Override
	public int getToClient() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFiltered(int agentPrimeIdentity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFiltered(int agentPrimeIdentity, boolean filteredResult) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLeftTrueRightFalse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLeftTrueRightFalse(boolean leftTrueRightFalse) {
		// TODO Auto-generated method stub

	}

	@Override
	public Substance getSubstance(Attribute attribute) {
		return containerEntryStore.get(attribute);
	}

	@Override
	public ConcreteContainer getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance) {
		containerEntryStore.put(attribute,substance);
		return substance;
	}
	
	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance,int minor) {
		containerEntryStore.put(attribute,substance);
		return substance;
	}

	@Override
	public void remove(Attribute attribute) {
		// TODO Auto-generated method stub

	}
	@Override
	public void remove(Attribute attribute,int minor) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getIdentitySequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean markedAdded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean markedRemoved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean markedDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void markAdded(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markRemoved(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markDirty(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public TransportEntry cloneConcrete() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int touchMode() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getInternalIdentity() {
		// TODO Auto-generated method stub
		return 0;
	}
}
