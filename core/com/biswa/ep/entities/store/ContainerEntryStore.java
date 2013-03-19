package com.biswa.ep.entities.store;


/**
 * Container Entry creater factory
 * @author biswa
 *
 */
public interface ContainerEntryStore{
	PhysicalEntry create(int id);
	void clear();
	PhysicalEntry getEntry(int id);
	PhysicalEntry remove(int id);
	void save(PhysicalEntry containerEntry);
	PhysicalEntry[] getEntries();
	PhysicalEntry getDefaultEntry();
}