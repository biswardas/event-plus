package com.biswa.ep.discovery;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.biswa.ep.entities.LightWeightEntry;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.spec.FilterSpec;

public interface EntryReader extends Remote{
	int[] getIDs() throws RemoteException;
	TransportEntry getByID(int id) throws RemoteException;
	TransportEntry[] getByID(int[] ids) throws RemoteException;
	TransportEntry[] getByFilter(FilterSpec filterSpec) throws RemoteException;
	int getEntryCount(String name,int isolation) throws RemoteException;
	LightWeightEntry getSortedEntry(String name,int id,int isolation) throws RemoteException;
	String[] getAttributes() throws RemoteException;
}
