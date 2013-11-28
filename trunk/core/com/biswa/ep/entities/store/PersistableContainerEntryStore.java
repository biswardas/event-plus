package com.biswa.ep.entities.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
class PersistableContainerEntryStore extends ConcreteContainerEntryStore{
	private final File directory;
	private final int passivation_idle_period;
	public PersistableContainerEntryStore(ConcreteContainer concreteContainer, int passivation_idle_period) {
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
				PersistableContainerEntryStore.this.passivate();
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
				 if(perEntry.passivate()){
					 i++;
				 }
			 }
		}
		long end = System.currentTimeMillis();
		System.out.println("Took " +(end-begin)+" milliseconds to passivate " +i+ " records");
	}

	protected void store(PhysicalEntry underlyingEntry)
			throws Exception {
		ObjectOutputStream oos = null;
		try {
			File sourceFile = new File(getDirectory(underlyingEntry.getIdentitySequence()), String
					.valueOf(underlyingEntry.getIdentitySequence()));
			BufferedOutputStream boos = new BufferedOutputStream(
					new FileOutputStream(sourceFile));
			oos = new ObjectOutputStream(boos);
			oos.writeObject(underlyingEntry);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected PhysicalEntry load(int externalIdentity) throws Exception {
		ObjectInputStream ois = null;
		File sourceFile = null;
		try {
			sourceFile = new File(getDirectory(externalIdentity), String
					.valueOf(externalIdentity));
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(sourceFile));
			ois = new ObjectInputStream(bis);
			PhysicalEntry readObject = (PhysicalEntry) ois
					.readObject();
			return readObject;
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sourceFile.delete();
			}
		}
	}
	private File getDirectory(int index) {
		StringBuilder sb = new StringBuilder();
		while(index>0){
			sb.insert(0, '/').insert(0, index%10);
			index=index/10;
		}
		File file = new File(directory,sb.toString());
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}
}