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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.PivotContainer;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.spec.AggrSpec;
import com.biswa.ep.entities.spec.CollapseSpec;
import com.biswa.ep.entities.spec.PivotSpec;
import com.biswa.ep.entities.spec.SortSpec;
public class GenericViewer extends PivotContainer {
	private JFrame jframe = null;
	private JTable jtable = null;
	private ViewerTableModel vtableModel = null;

	public GenericViewer(String name) {
		super(name,new Properties(){
			private static final long serialVersionUID = 1L;

			{
				put("concurrent","-1");
			}
		});
		ContainerContext.initialize(GenericViewer.this);
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
		vtableModel = new ViewerTableModel(GenericViewer.this);
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
		addPivotControl(jPanel);
		addAggrControl(jPanel);
		addSortControl(jPanel);
		addCollapsingControl(jPanel);
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
				GenericViewer.this.agent().applySpec(pivotSpec);
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
					aggrSpec.add(new LeafAttribute(oneAttribute[0]), Aggregators.valueOf(oneAttribute[1]).AGGR);
				}
				GenericViewer.this.agent().applySpec(aggrSpec);
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
				GenericViewer.this.agent().applySpec(sortSpec);
			}
		});
	}
	private void addCollapsingControl(JPanel jPanel) {
		final JTextField collapserTextField = new JTextField();	
		final JButton collapserButton = new JButton("Apply Collapse");	
		jPanel.add(collapserTextField);
		jPanel.add(collapserButton);
		collapserButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] oneNode = collapserTextField.getText().split(":");
				boolean order = oneNode.length>1?Boolean.parseBoolean(oneNode[1]):true;
				CollapseSpec collapseSpec = new CollapseSpec(Integer.parseInt(oneNode[0]),order);
				GenericViewer.this.agent().applySpec(collapseSpec);
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

	class ViewerTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4352652252566957454L;
		private GenericViewer cs;
		private ContainerEntry[] containerEntries = null;
		private Attribute[] attributes = null;
		public ViewerTableModel(GenericViewer cs) {
			this.cs = cs;
			this.attributes = cs.getSubscribedAttributes();
			this.containerEntries = cs.getContainerEntries();
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
			return cs.getEntryCount();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex==0) return containerEntries[rowIndex].getIdentitySequence();
			else 
			return containerEntries[rowIndex].getSubstance(attributes[columnIndex-1]);
		}
		@Override
		public void fireTableDataChanged(){
			this.containerEntries = cs.getContainerEntries();
			super.fireTableDataChanged();
		}
		@Override
		public void fireTableStructureChanged(){
			this.attributes = cs.getSubscribedAttributes();
			super.fireTableStructureChanged();
		}
	}
}