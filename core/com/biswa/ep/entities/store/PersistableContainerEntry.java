package com.biswa.ep.entities.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.TransportEntry;

class PersistableContainerEntry extends AbstractPhysicalEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = -919932327675986409L;
	private final PhysicalEntry proxyEntry = (PhysicalEntry) Proxy.newProxyInstance(
			PhysicalEntry.class.getClassLoader(),
			new Class[] { PhysicalEntry.class }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					lastAccessed = System.currentTimeMillis();
					if (underlyingEntry == proxyEntry) {
						try {
							underlyingEntry = readFromDisk((PassivableContainerEntryStore) getContainer()
									.getContainerEntryStore());
						} catch (Exception e) {
							throw new RuntimeException("Unable to restore passivated entry");
						}
					}
					return method.invoke(underlyingEntry, args);
				}
			});
	protected PhysicalEntry underlyingEntry;
	
	private long lastAccessed = System.currentTimeMillis();

	public PersistableContainerEntry(PhysicalEntry containerEntry) {
		super(containerEntry.getIdentitySequence());
		this.underlyingEntry = containerEntry;
	}

	final public long getLastAccessed() {
		return lastAccessed;
	}

	final protected boolean passivate(PassivableContainerEntryStore passiveStore) {
		if (underlyingEntry != proxyEntry) {
			try {
				writeToDisk(passiveStore);
				underlyingEntry = proxyEntry;
				return true;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	private void writeToDisk(PassivableContainerEntryStore passiveStore)
			throws Exception {
		ObjectOutputStream oos = null;
		try {
			File sourceFile = new File(passiveStore.getDirectory(), String
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

	private ConcreteContainerEntry readFromDisk(
			PassivableContainerEntryStore passiveStore) throws Exception {
		ObjectInputStream ois = null;
		File sourceFile = null;
		try {
			sourceFile = new File(passiveStore.getDirectory(), String
					.valueOf(externalIdentity));
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(sourceFile));
			ois = new ObjectInputStream(bis);
			ConcreteContainerEntry readObject = (ConcreteContainerEntry) ois
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

	@Override
	public Object getSubstance(Attribute attribute) {
		return underlyingEntry.getSubstance(attribute);
	}

	@Override
	public Object silentUpdate(Attribute attribute, Object substance) {
		return underlyingEntry.silentUpdate(attribute, substance);
	}

	@Override
	public Object silentUpdate(Attribute attribute, Object substance, int minor) {
		return underlyingEntry.silentUpdate(attribute, substance, minor);
	}

	@Override
	public void remove(Attribute attribute) {
		underlyingEntry.remove(attribute);
	}

	@Override
	public void remove(Attribute attribute, int minor) {
		underlyingEntry.remove(attribute, minor);
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

	@Override
	public String toString() {
		return underlyingEntry.toString();
	}
}
