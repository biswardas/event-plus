package com.biswa.ep.discovery;

import java.rmi.RemoteException;

public class EntryReaderTest {
public static void main(String[] args) throws RemoteException {
	EntryReader er = RegistryHelper.getEntryReader("Pivot.LInputStocks");
	System.out.println(er.getByID(10001));
}
}
