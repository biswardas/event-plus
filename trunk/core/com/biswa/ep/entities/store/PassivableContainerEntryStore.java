package com.biswa.ep.entities.store;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerTask;

/**
 * Concrete container entry creater.
 * @author biswa
 *
 */
class PassivableContainerEntryStore extends ConcreteContainerEntryStore{
	private final File directory;
	private final int passivation_idle_period;
	public PassivableContainerEntryStore(ConcreteContainer concreteContainer, int passivation_idle_period) {
		super(concreteContainer);
		this.passivation_idle_period = passivation_idle_period;
		directory = new File(UUID.randomUUID().toString());
		directory.mkdir();
		final ContainerTask containerTask = new ContainerTask(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -2190184742017542467L;

			@Override
			public void runtask() {
				PassivableContainerEntryStore.this.passivate();
			}
		};
		concreteContainer.invokePeriodically(containerTask, passivation_idle_period, passivation_idle_period, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public PhysicalEntry create(int id){
		PhysicalEntry containerEntry =  new PersistableContainerEntry(super.create(id));
		save(containerEntry);
		return containerEntry;
	}
	
	private void passivate() {
		long begin = System.currentTimeMillis();
		int i = 0;
		System.out.println("Passivation Triggered");
		for(ContainerEntry persistEntry:getEntries()){
			 PersistableContainerEntry perEntry=(PersistableContainerEntry)persistEntry;
			 if((System.currentTimeMillis()-perEntry.getLastAccessed())>passivation_idle_period){
				 if(perEntry.passivate(this)){
					 i++;
				 }
			 }
		}
		long end = System.currentTimeMillis();
		System.out.println("Took " +(end-begin)+" milliseconds to passivate " +i+ " records");
	}

	public File getDirectory() {
		return directory;
	}
}