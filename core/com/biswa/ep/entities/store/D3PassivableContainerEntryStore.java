package com.biswa.ep.entities.store;

import com.biswa.ep.entities.ConcreteContainer;

/**
 * D3 ContainerEntry Creater.
 * @author biswa
 *
 */
class D3PassivableContainerEntryStore extends PassivableContainerEntryStore{
	public D3PassivableContainerEntryStore(ConcreteContainer concreteContainer, int passivation_idle_period, boolean eager) {
		super(concreteContainer,passivation_idle_period,eager);
	}
	
	@Override
	public PhysicalEntry create(int id){
		PhysicalEntry containerEntry = buildPassiveEntry(new D3ContainerEntry(id));
		save(containerEntry);
		return containerEntry;
	}
}