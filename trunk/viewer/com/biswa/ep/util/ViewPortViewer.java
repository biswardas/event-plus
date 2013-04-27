package com.biswa.ep.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.discovery.Connector;
import com.biswa.ep.discovery.EntryReader;
import com.biswa.ep.discovery.RMIAccepterImpl;
import com.biswa.ep.discovery.RMIListener;
import com.biswa.ep.discovery.RegistryHelper;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerDeleteEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.spec.AggrSpec;
import com.biswa.ep.entities.spec.CollapseSpec;
import com.biswa.ep.entities.spec.PivotSpec;
import com.biswa.ep.entities.spec.SortSpec;
import com.biswa.ep.provider.CompiledAttributeProvider;
import com.biswa.ep.provider.ScriptEngineAttributeProvider;
public class ViewPortViewer extends ConcreteContainer {
	final String sourceContextName;
	final String sourceContainerName;
	private JFrame jframe = null;
	private JTable jtable = null;
	private ViewerTableModel vtableModel = null;
	private String sourceName;
	private final RMIListener er;

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
		er = RegistryHelper.getRMIListener(sourceName);
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
		addControls();
		jsc.revalidate();
		jframe.setVisible(true);
		jframe.setSize(new Dimension(1200, 500));
	}


	private void addControls() {
		JPanel jPanel = new JPanel(new GridLayout(0,2));
		addCompiledExpression(jPanel);
		addScriptExpression(jPanel);
		addPivotControl(jPanel);
		addAggrControl(jPanel);
		addSortControl(jPanel);
		addCollapsingControl(jPanel);
		addRemoveControl(jPanel);
		jframe.add(jPanel,BorderLayout.SOUTH);
	}

	private void addPivotControl(JPanel jPanel) {
		final JTextField pivotTextField = new JTextField();	
		final JButton pivotButton = new JButton("Apply Pivot");	
		jPanel.add(pivotTextField);
		jPanel.add(pivotButton);
		pivotButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				StringTokenizer stk = new StringTokenizer(pivotTextField.getText(),",");
				List<Attribute> list = new ArrayList<Attribute>();
				while(stk.hasMoreTokens()){
					list.add(new LeafAttribute(stk.nextToken()));
				}
				PivotSpec pivotSpec = new PivotSpec(list.toArray(new Attribute[0]));
				try {
					getDataOperation().applySpec(pivotSpec);
				} catch (RemoteException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}
	private void addAggrControl(JPanel jPanel) {
		final JTextField aggrTextField = new JTextField();	
		final JButton aggrButton = new JButton("Apply Aggr");	
		jPanel.add(aggrTextField);
		jPanel.add(aggrButton);
		aggrButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				AggrSpec aggrSpec = new AggrSpec();
				StringTokenizer stk = new StringTokenizer(aggrTextField.getText(),",");
				while(stk.hasMoreTokens()){
					String[] oneAttribute = stk.nextToken().split(":");
					aggrSpec.add(Aggregators.valueOf(oneAttribute[1]).newInstance(oneAttribute[0]));
				}
				try {
					getDataOperation().applySpec(aggrSpec);
				} catch (RemoteException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}
	private void addSortControl(JPanel jPanel) {
		final JTextField sortTextField = new JTextField();	
		final JButton sortButton = new JButton("Apply Sort");	
		jPanel.add(sortTextField);
		jPanel.add(sortButton);
		sortButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				StringTokenizer stk = new StringTokenizer(sortTextField.getText(),",");
				SortSpec sortSpec = new SortSpec();
				while(stk.hasMoreTokens()){
					String[] oneAttribute = stk.nextToken().split(":");
					boolean order = oneAttribute.length>1?Boolean.parseBoolean(oneAttribute[1]):true;
					sortSpec.addSortOrder(new LeafAttribute(oneAttribute[0]),order);
				}
				try {
					getDataOperation().applySpec(sortSpec);
				} catch (RemoteException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}
	private void addCollapsingControl(JPanel jPanel) {
		final JTextField collapserTextField = new JTextField();	
		final JButton collapserButton = new JButton("Collapse");
		jPanel.add(collapserTextField);	
		jPanel.add(collapserButton);
		collapserButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] oneNode = collapserTextField.getText().split(":");
				boolean order = oneNode.length>1?Boolean.parseBoolean(oneNode[1]):true;
				CollapseSpec collapseSpec = new CollapseSpec(Integer.parseInt(oneNode[0]),order);
				try {
					getDataOperation().applySpec(collapseSpec);
				} catch (RemoteException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}
	private void addRemoveControl(JPanel jPanel) {
		final JTextField collapserTextField = new JTextField();	
		final JButton removeButton = new JButton("Remove");	
		jPanel.add(collapserTextField);
		jPanel.add(removeButton);
		removeButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					getDataOperation().entryRemoved(new ContainerDeleteEvent(getName(), Integer.parseInt(collapserTextField.getText()), 0));
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}
	private void addCompiledExpression(JPanel jPanel) {
		final JTextField collapserTextField = new JTextField();	
		final JButton removeButton = new JButton("Add Compiled Expression");	
		jPanel.add(collapserTextField);
		jPanel.add(removeButton);
		removeButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					getDataOperation().addCompiledAttribute(collapserTextField.getText());
				} catch (RemoteException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}
	private void addScriptExpression(JPanel jPanel) {
		final JTextField collapserTextField = new JTextField();	
		final JButton removeButton = new JButton("Add Script Expression");	
		jPanel.add(collapserTextField);
		jPanel.add(removeButton);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					getDataOperation().addScriptAttribute(collapserTextField.getText());
				} catch (RemoteException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
	}


	private RMIListener getDataOperation() {
		return (RMIListener)er;
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