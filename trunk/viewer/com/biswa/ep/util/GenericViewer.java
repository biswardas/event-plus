package com.biswa.ep.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
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
import javax.swing.table.AbstractTableModel;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerDeleteEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.LightWeightEntry;
import com.biswa.ep.entities.Predicate;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.spec.AggrSpec;
import com.biswa.ep.entities.spec.CollapseSpec;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.PivotSpec;
import com.biswa.ep.entities.spec.SortSpec;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.provider.CompiledAttributeProvider;
import com.biswa.ep.provider.PredicateBuilder;
import com.biswa.ep.provider.ScriptEngineAttributeProvider;
public class GenericViewer extends ConcreteContainer {
	private JFrame jframe = null;
	private JTable jtable = null;
	private ViewerTableModel vtableModel = null;
	private Agent sourceAgent;
	public GenericViewer(String name) {
		super(name,new Properties(){
			private static final long serialVersionUID = 1L;

			{
				put("concurrent","-1");
			}
		});
		ContainerContext.initialize(GenericViewer.this);
	}

	@Override
	public void connected(ConnectionEvent connectionEvent) {
		super.connected(connectionEvent);

		jframe = new JFrame(getName()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -2211995623288725113L;

			{
				this.addWindowListener(new WindowAdapter(){
					private int x;

					@Override
					public void windowClosing(WindowEvent e) {
						if(++x==2) System.exit(0);
						try{
							disconnect(null);
						}finally{
							super.windowClosed(e);
						}
					}							
				});
			}
		};
		jframe.setLayout(new BorderLayout());
		vtableModel = new ViewerTableModel();
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
		addFilterControl(jPanel);
		addCompiledExpression(jPanel);
		addScriptExpression(jPanel);
		addPivotControl(jPanel);
		addAggrControl(jPanel);
		addSortControl(jPanel);
		addCollapsingControl(jPanel);
		addRemoveControl(jPanel);
		jframe.add(jPanel,BorderLayout.SOUTH);
	}
	private void addFilterControl(JPanel jPanel) {
		final JTextField collapserTextField = new JTextField();
		final JButton removeButton = new JButton("Add Filter Expression");
		jPanel.add(collapserTextField);
		jPanel.add(removeButton);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Predicate pred = PredicateBuilder.buildPredicate(collapserTextField.getText());
				getSourceAgent().applySpec(new FilterSpec(getName(),pred));
			}
		});
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
				PivotSpec pivotSpec = new PivotSpec(getName(),list.toArray(new Attribute[0]));
				getSourceAgent().applySpec(pivotSpec);
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
				AggrSpec aggrSpec = new AggrSpec(getName());
				StringTokenizer stk = new StringTokenizer(aggrTextField.getText(),",");
				while(stk.hasMoreTokens()){
					String[] oneAttribute = stk.nextToken().split(":");
					aggrSpec.add(Aggregators.valueOf(oneAttribute[1]).newInstance(oneAttribute[0]));
				}
				getSourceAgent().applySpec(aggrSpec);
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
				SortSpec sortSpec = new SortSpec(getName());
				while(stk.hasMoreTokens()){
					String[] oneAttribute = stk.nextToken().split(":");
					boolean order = oneAttribute.length>1?Boolean.parseBoolean(oneAttribute[1]):true;
					sortSpec.addSortOrder(new LeafAttribute(oneAttribute[0]),order);
				}
				getSourceAgent().applySpec(sortSpec);
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
				CollapseSpec collapseSpec = new CollapseSpec(getName(),Integer.parseInt(oneNode[0]),order);
				getSourceAgent().applySpec(collapseSpec);
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
				getSourceAgent().entryRemoved(new ContainerDeleteEvent(getName(), Integer.parseInt(collapserTextField.getText()), 0));
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
				com.biswa.ep.entities.Attribute schemaAttribute = new CompiledAttributeProvider().getAttribute(collapserTextField.getText());
				ContainerEvent ce = new ContainerStructureEvent(getName(),schemaAttribute);
				getSourceAgent().attributeRemoved(ce);
				getSourceAgent().attributeAdded(ce);
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
				com.biswa.ep.entities.Attribute schemaAttribute = new ScriptEngineAttributeProvider().getAttribute(collapserTextField.getText());
				ContainerEvent ce = new ContainerStructureEvent(getName(),schemaAttribute);
				getSourceAgent().attributeRemoved(ce);
				getSourceAgent().attributeAdded(ce);
			}
		});
	}

	@Override
	public void disconnected(ConnectionEvent connectionEvent) {
		jframe.dispose();
		System.exit(0);
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
		jframe.setTitle(getName()+"/"+jtable.getRowCount()+"--"+GenericViewer.this.getCurrentTransactionID());
	}

	public Agent getSourceAgent() {
		return sourceAgent;
	}
	
	public void setSourceAgent(Agent sourceAgent) {
		this.sourceAgent=sourceAgent;
	}
	
	class ViewerTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4352652252566957454L;
		private WeakHashMap<Integer, LightWeightEntry> cachedEntries = new WeakHashMap<Integer, LightWeightEntry>() {
			public LightWeightEntry get(Object key) {
				LightWeightEntry tEntry = super.get(key);
					if (tEntry == null) {
						// System.out.println("Requesting record:"+key);
						tEntry = getSourceAgent().getSortedEntry(getName(), (Integer) key);
						put((Integer) key, tEntry);
					}
				return tEntry;
			}
		};
		private String[] attributes = null;

		public ViewerTableModel() {
			this.attributes = getSourceAgent().getAttributes();
		}

		public String getColumnName(int col) {
			if (col == 0)
				return "Identity";
			else
				return attributes[col - 1];
		}

		@Override
		public int getColumnCount() {
			int cnt = attributes.length;
			return cnt + 1;
		}

		@Override
		public int getRowCount() {
			int rowCnt = getSourceAgent().getEntryCount(getName());
			return rowCnt;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return cachedEntries.get(rowIndex).id;
			else
				return cachedEntries.get(rowIndex).substances[columnIndex - 1];
		}

		@Override
		public void fireTableDataChanged() {
			cachedEntries.clear();
			super.fireTableDataChanged();
		}

		@Override
		public void fireTableStructureChanged() {
			this.attributes = getSourceAgent().getAttributes();
			super.fireTableStructureChanged();
		}
	}
}