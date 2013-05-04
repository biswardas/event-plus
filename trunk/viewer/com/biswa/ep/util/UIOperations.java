package com.biswa.ep.util;

import com.biswa.ep.entities.LightWeightEntry;
import com.biswa.ep.entities.spec.Spec;

public interface UIOperations {
	LightWeightEntry getLightWeightEntry(int id);
	int getSortedEntryCount();
	void applySpecInSource(Spec spec);
	void addCompiledAttributeToSource(String data);
	void addScriptAttributeToSource(String data);
	void removeEntryFromSource(String data);
	String[] getAttributes();
	void disConnectFromSource();
}
