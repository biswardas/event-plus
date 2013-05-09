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
	private final boolean eager;
	private final int passivation_idle_period;
	public PassivableContainerEntryStore(ConcreteContainer concreteContainer, int passivation_idle_period, boolean eager) {
		super(concreteContainer);
		this.passivation_idle_period = passivation_idle_period;
		this.eager = eager;
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
		PhysicalEntry containerEntry =  buildPassiveEntry(super.create(id));
		save(containerEntry);
		return containerEntry;
	}
	
	@Override
	public PhysicalEntry[] getEntries() {
		PhysicalEntry[] maybePassive = super.getEntries();
		return eager?wakeUp(maybePassive):maybePassive;
	}
	
	@Override
	public PhysicalEntry getEntry(int id) {
		PhysicalEntry singleEntry = super.getEntry(id);
		if(singleEntry!=null && eager){
			PhysicalEntry[] containerEntry = {singleEntry};
			wakeUp(containerEntry);
		}
		return singleEntry;		
	}

	private void passivate() {
		long begin = System.currentTimeMillis();
		int i = 0;
		System.out.println("Passivation Triggered");
		for(ContainerEntry persistEntry:super.getEntries()){
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

	private PhysicalEntry[] wakeUp(PhysicalEntry[] maybePassive) {
		for(ContainerEntry oneEntry:maybePassive){
			PersistableContainerEntry perEntry = (PersistableContainerEntry)oneEntry;
			perEntry.activate(this);
		}
		return maybePassive;
	}

	protected PhysicalEntry buildPassiveEntry(PhysicalEntry containerEntry) {
		return eager?new EagerContainerEntry(containerEntry):new LazyContainerEntry(containerEntry);
	}

	public File getDirectory() {
		return directory;
	}
}