package com.biswa.ep.util;

import java.rmi.RemoteException;
import java.util.Date;

import javax.swing.SwingUtilities;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.discovery.RMIAccepterImpl;
import com.biswa.ep.discovery.RMIListener;
import com.biswa.ep.discovery.RegistryHelper;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerDeleteEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LightWeightEntry;
import com.biswa.ep.entities.spec.Spec;

public class ViewPortViewer extends GenericViewer {
	final String sourceContextName;
	final String sourceContainerName;
	private String sourceName;
	private final RMIListener sourceAgent;

	public ViewPortViewer(final String sourceContextName,
			final String sourceContainerName) {
		super("Viewer-"
				+ sourceContextName
				+ "."
				+ sourceContainerName
				+ "("
				+ new Date().toString().replaceAll("\\s+", "")
						.replaceAll(":", "_") + ")");

		this.sourceContextName = sourceContextName;
		this.sourceContainerName = sourceContainerName;
		this.sourceName = sourceContextName + "." + sourceContainerName;
		sourceAgent = RegistryHelper.getRMIListener(sourceName);
		agent().addSource(new ConnectionEvent(sourceName, getName()));
		final Accepter accepter = new RMIAccepterImpl(new ContainerManager());
		accepter.publish(this);
		// Invoke local
		accepter.listen(new Listen() {
			@Override
			public String getContainer() {
				return sourceContainerName;
			}

			@Override
			public String getContext() {
				return sourceContextName;
			}
		}, this);

	}

	@Override
	public int getSortedEntryCount() {
		try {
			if(isPivot()){
				return sourceAgent.getEntryCount(getName(), 2);
			}else{
				return getEntryCount();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public LightWeightEntry getLightWeightEntry(int id) {
		try {
			if(isPivot()){
				return sourceAgent.getSortedEntry(getName(), id, 2);
			}else{
				ContainerEntry conEntry = getContainerEntries()[id];
				return new LightWeightEntry(conEntry.getIdentitySequence(),conEntry.getSubstancesAsArray());
			}
		} catch (RemoteException e) {
			return new LightWeightEntry(getDefaultEntry()
					.getIdentitySequence(),getDefaultEntry().getSubstancesAsArray());
		
		}
	}
	
	@Override
	public void applySpecInSource(Spec spec) {
		try {
			sourceAgent.applySpec(spec);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void addCompiledAttributeToSource(String data) {
		try {
			sourceAgent.addCompiledAttribute(data);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void addScriptAttributeToSource(String data) {
		try {
			sourceAgent.addScriptAttribute(data);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void removeEntryFromSource(String data) {
		try {
			sourceAgent.entryRemoved(new ContainerDeleteEvent(getName(), Integer.parseInt(data), 0));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	@Override
	public String[] getAttributes(){
		try {
			if(isPivot()){
				return sourceAgent.getAttributes();
			}else{
				return getSubscribedAttrStrNames();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	@Override
	public void disConnectFromSource() {
		try {
			sourceAgent.disconnect(sourceName, getName());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean isPivot(){
		return true;
	}
	public static void main(final String[] args) {
		if (args.length < 2) {
			System.out.println("Usage java Viewer $ContextName $ContainerName");
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						ViewPortViewer rv = new ViewPortViewer(args[0], args[1]);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}