package com.biswa.ep.entities.store;

import com.biswa.ep.entities.ContainerEntry;

public interface PhysicalEntry extends ContainerEntry {
	void reallocate(int size);
}
