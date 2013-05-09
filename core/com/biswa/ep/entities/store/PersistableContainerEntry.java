package com.biswa.ep.entities.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.biswa.ep.ClientToken;

abstract class PersistableContainerEntry extends AbstractPhysicalEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = -919932327675986409L;
	protected PhysicalEntry underlyingEntry;
	private long lastAccessed = System.currentTimeMillis();

	public PersistableContainerEntry(PhysicalEntry containerEntry) {
		super(containerEntry.getIdentitySequence());
		this.underlyingEntry = containerEntry;
	}
	
	final public long getLastAccessed() {
		return lastAccessed;
	}
	
	final protected boolean passivate(PassivableContainerEntryStore passiveStore){
		if(!markedPassivated()){
			try{
				writeToDisk(passiveStore);
				underlyingEntry = null;
				markPassivated(true);
				return true;
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		return false;
	}
	
	private void writeToDisk(PassivableContainerEntryStore passiveStore) throws Exception {
		ObjectOutputStream oos = null;
		try {
			File sourceFile = new File(passiveStore.getDirectory(),String.valueOf(underlyingEntry.getIdentitySequence()));
			BufferedOutputStream boos = new BufferedOutputStream(new FileOutputStream(sourceFile));
			oos = new ObjectOutputStream(boos);
			oos.writeObject(underlyingEntry);
		} finally{
			if(oos!=null){
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	final protected void activate(PassivableContainerEntryStore passiveStore){
		if(markedPassivated()){
			try{
				underlyingEntry = readFromDisk(passiveStore);
				markPassivated(false);
			}catch(Exception e){
				throw new RuntimeException("Unable to restore passivated entry");
			}	
		}
		lastAccessed = System.currentTimeMillis();
	}

	private ConcreteContainerEntry readFromDisk(PassivableContainerEntryStore passiveStore) throws Exception {
		ObjectInputStream ois = null;
		try {
			File sourceFile = new File(passiveStore.getDirectory(),String.valueOf(externalIdentity));
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
			ois = new ObjectInputStream(bis);
			ConcreteContainerEntry readObject = (ConcreteContainerEntry)ois.readObject();
			sourceFile.delete();
			return readObject;
		} finally {
			if(ois!=null){
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	

	final public boolean markedPassivated() {
		return (metainfo&MARKED_PASSIVATED)>0;
	}
	
	final public void markPassivated(boolean flag) {
		if(flag){
			metainfo=metainfo|MARKED_PASSIVATED;
		}else{			
			metainfo=metainfo&(ClientToken.ALL_AVAILABLE^MARKED_PASSIVATED);			
		}		
	}

	@Override
	public String toString() {
		return String.valueOf(underlyingEntry);
	}
	
}
