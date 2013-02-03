package com.biswa.ep.entities.store;

import com.biswa.ep.entities.ConcreteContainer;

/**
 * D3 ContainerEntry Creater.
 * @author biswa
 *
 */
class D3ContainerEntryStore extends ConcreteContainerEntryStore{
	public D3ContainerEntryStore(ConcreteContainer concreteContainer) {
		super(concreteContainer);
	}
	
	@Override
	public PhysicalEntry create(int id){
		PhysicalEntry containerEntry = new D3ContainerEntry(id);
		save(containerEntry);
		return containerEntry;
	}
}