package com.biswa.ep.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.discovery.Connector;
import com.biswa.ep.discovery.EntryReader;
import com.biswa.ep.discovery.RMIAccepterImpl;
import com.biswa.ep.discovery.RegistryHelper;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.TransportEntry;
public class ViewPortViewer extends ConcreteContainer {
	final String sourceContextName;
	final String sourceContainerName;
	private JFrame jframe = null;
	private JTable jtable = null;
	private ViewerTableModel vtableModel = null;
	private String sourceName;

	public ViewPortViewer(final String sourceContextName,final String sourceContainerName) {
		super("Viewer-"+sourceContextName+"."+sourceContainerName+"("+new Date().toString().replaceAll("\\s+", "").replaceAll(":","_")+")",new Properties(){
			private static final long serialVersionUID = 1L;

			{
				put("concurrent","-1");
			}
		});
		ContainerContext.initialize(ViewPortViewer.this);
		
		
		this.sourceContextName=sourceContextName;
		this.sourceContainerName=sourceContainerName;
		this.sourceName = sourceContextName+"."+sourceContainerName;
		agent().addSource(new ConnectionEvent(sourceName, getName()));
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
		
		

		jframe = new JFrame(getName()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -2211995623288725113L;

			{
				this.addWindowListener(new WindowAdapter(){
					@Override
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}							
				});
			}
		};
		jframe.setLayout(new BorderLayout());
		vtableModel = new ViewerTableModel(ViewPortViewer.this);
		jtable = new JTable(vtableModel);
		jtable.setPreferredScrollableViewportSize(new Dimension(500, 200));
		jtable.setFillsViewportHeight(true);				
		JScrollPane jsc = new JScrollPane(jtable);
		jframe.add(jsc,BorderLayout.CENTER);
		jsc.revalidate();
		jframe.setVisible(true);
		jframe.setSize(new Dimension(1200, 500));
	}
	
	@Override
	public void attributeAdded(final ContainerEvent ce) {
		super.attributeAdded(ce);
		if(vtableModel!=null)vtableModel.fireTableStructureChanged();
	}

	@Override
	public void attributeRemoved(final ContainerEvent ce) {
		super.attributeRemoved(ce);
		if(vtableModel!=null)vtableModel.fireTableStructureChanged();
	}
	
	@Override
	public void commitTran(){
		super.commitTran();
		vtableModel.fireTableDataChanged();
		jframe.setTitle(getName()+"/"+jtable.getRowCount()+"--"+ViewPortViewer.this.getCurrentTransactionID());
	}

	class ViewerTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4352652252566957454L;
		private ViewPortViewer cs;
		private final EntryReader er;
		private final TransportEntry DEF = new TransportEntry(0,new HashMap<Attribute,Object>());
		private WeakHashMap<Integer, TransportEntry> cachedEntries= new WeakHashMap<Integer,TransportEntry>(){
			public TransportEntry get(Object key){
				TransportEntry tEntry = null;
				try {
					tEntry = super.get(key);
					if(tEntry==null){
						//System.out.println("Requesting record:"+key);
						tEntry = er.getSortedEntry((Integer) key);
						put((Integer)key,tEntry);
					}
				} catch (RemoteException e) {
					tEntry = DEF;
				}
				return tEntry;
			}
		};
		private Attribute[] attributes = null;
		public ViewerTableModel(ViewPortViewer cs) {
			this.cs = cs;
			this.attributes = cs.getSubscribedAttributes();
			er = RegistryHelper.getEntryReader(sourceName);
		}

		public TransportEntry getRecord(int key){
			return cachedEntries.get(key);
		}
		public String getColumnName(int col) {
			if(col==0) return "Identity";
			else return attributes[col-1].getName();
		}

		@Override
		public int getColumnCount() {
			int cnt = attributes.length;
			return cnt+1;
		}

		@Override
		public int getRowCount() {
			try {
				return er.getEntryCount();
			} catch (RemoteException e) {
				return 0;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex==0) return cachedEntries.get(rowIndex).getIdentitySequence();
			else 
			return cachedEntries.get(rowIndex).getEntryQualifier().get(attributes[columnIndex-1]);
		}
		@Override
		public void fireTableDataChanged(){
			cachedEntries.clear();
			super.fireTableDataChanged();
		}
		@Override
		public void fireTableStructureChanged(){
			this.attributes = cs.getSubscribedAttributes();
			super.fireTableStructureChanged();
		}
	}

	@Override
	public void disconnect(ConnectionEvent containerEvent) {
		try {
			Connector connecter = RegistryHelper.getConnecter(sourceName);
			connecter.disconnect(sourceName, this.getName());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void main(final String[] args){
		if(args.length<2){
			System.out.println("Usage java Viewer $ContextName $ContainerName");
		}else{
			try {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run(){
						ViewPortViewer rv = new ViewPortViewer(args[0],args[1]);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}