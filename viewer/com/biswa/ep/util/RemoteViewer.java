package com.biswa.ep.util;

import java.rmi.RemoteException;
import java.util.Date;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.discovery.Connector;
import com.biswa.ep.discovery.RMIAccepterImpl;
import com.biswa.ep.discovery.RegistryHelper;
import com.biswa.ep.entities.ConnectionEvent;

public class RemoteViewer extends GenericViewer {
	final String sourceContextName;
	final String sourceContainerName;
	public RemoteViewer(final String sourceContextName,final String sourceContainerName) {
		super("Viewer-"+sourceContextName+"."+sourceContainerName+" ("+new Date()+")");
		this.sourceContextName=sourceContextName;
		this.sourceContainerName=sourceContainerName;
		agent().addSource(new ConnectionEvent(sourceContextName+"."+sourceContainerName, getName()));
		final Accepter accepter = new RMIAccepterImpl(new ContainerManager());
		accepter.publish(this);
		//Invoke local
		accepter.listen(new Listen(){
			@Override
			public String getContainer() {
				return sourceContainerName;
			}

			@Override
			public String getContext() {
				return sourceContextName;
			}			
		}, this);
		Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Cleanup Thread"){
			public void run(){
				accepter.unpublish(RemoteViewer.this);
			}
		});
	}

	@Override
	public void disconnect(ConnectionEvent containerEvent) {
		String sourceName = sourceContextName+"."+sourceContainerName;
		Connector connecter = RegistryHelper.getConnecter(sourceName);
		try {
			connecter.disconnect(sourceName, this.getName());
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args){
		if(args.length<2){
			System.out.println("Usage java Viewer $ContextName $ContainerName");
		}else{
			RemoteViewer v = new RemoteViewer(args[0],args[1]);
			v.start();
		}
	}
}

