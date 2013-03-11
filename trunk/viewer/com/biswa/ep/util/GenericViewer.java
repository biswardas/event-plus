package com.biswa.ep.util;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.PivotContainer;
import com.biswa.ep.entities.spec.SortSpec.SortOrder;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;
public class GenericViewer extends PivotContainer {
	private JFrame jframe = null;
	private JTable jtable = null;
	private ViewerTableModel vtableModel = null;
	private SortOrder[] sortOrder = new SortOrder[0];

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
		vtableModel = new ViewerTableModel(GenericViewer.this);
		jtable = new JTable(vtableModel);
		jtable.setPreferredScrollableViewportSize(new Dimension(500, 200));
		jtable.setFillsViewportHeight(true);				
		JScrollPane jsc = new JScrollPane(jtable);
		jframe.add(jsc);
		jsc.revalidate();
		jframe.setVisible(true);
		jframe.setSize(new Dimension(1200, 500));
	}

	@Override
	public void disconnected(ConnectionEvent connectionEvent) {
		jframe.dispose();
		System.exit(0);
	}
	
	@Override
	public void attributeAdded(final ContainerEvent ce) {
		super.attributeAdded(ce);
			((AbstractTableModel) jtable.getModel())
					.fireTableStructureChanged();
	}

	@Override
	public void attributeRemoved(final ContainerEvent ce) {
		super.attributeRemoved(ce);
			((AbstractTableModel) jtable.getModel())
					.fireTableStructureChanged();
	}

	@Override
	public void commitTran(){
			((AbstractTableModel) jtable.getModel())
					.fireTableDataChanged();
		jframe.setTitle(getName()+"/"+jtable.getRowCount()+"--"+GenericViewer.this.getCurrentTransactionID());
	}

	public void sortIt() {
		if (sortOrder.length > 0) {
			int lastColumnIndex = -1;
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
					jtable.getModel());
			List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
			for (SortOrder sortAttrOrder: sortOrder) {
				Attribute sortAttr = sortAttrOrder.getAttribute();
				for (int i = 0; i < getSubscribedAttributes().length; i++) {
					if (sortAttr.getName().equalsIgnoreCase(
							getSubscribedAttributes()[i].getName())) {
						sortKeys.add(new RowSorter.SortKey(i + 1,
								sortAttrOrder.isDescending()?javax.swing.SortOrder.DESCENDING:javax.swing.SortOrder.ASCENDING));
						lastColumnIndex = i;
						break;
					}
				}
			}
			sorter.setComparator(lastColumnIndex + 1,
					new Comparator<CellValue>() {
						@Override
						public int compare(CellValue o1,
								CellValue o2) {
							return o1.compareTo(o2);
						}
					});
			sorter.setSortKeys(sortKeys);
			jtable.setRowSorter(sorter);
		}
	}

	@Override
	public void applySort(final SortOrder ... sortOrder) {
		GenericViewer.this.sortOrder=sortOrder;
		sortIt();
	}

	@Override
	public void dispatchAttributeAdded(Attribute requestedAttribute) {
	}

	@Override
	public void dispatchAttributeRemoved(Attribute requestedAttribute) {
	}

	@Override
	public void dispatchEntryAdded(ContainerEntry ce) {
	}

	@Override
	public void dispatchEntryRemoved(ContainerEntry ce) {
	}

	@Override
	public void dispatchEntryUpdated(Attribute attribute, Substance substance, ContainerEntry ce) {
	}

	class ViewerTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4352652252566957454L;
		private GenericViewer cs;

		public ViewerTableModel(GenericViewer cs) {
			this.cs = cs;
		}

		public String getColumnName(int col) {
			if(col==0) return "Identity";
			else return cs.getSubscribedAttributes()[col-1].getName();
		}

		@Override
		public int getColumnCount() {
			int cnt = cs.getSubscribedAttributes().length;
			return cnt+1;
		}

		@Override
		public int getRowCount() {
			int cnt = cs.getContainerEntries().length;
			return cnt;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex==0) return cs.getContainerEntries()[rowIndex].getIdentitySequence();
			else 
			return new CellValue(cs.getContainerEntries()[rowIndex].getSubstance(cs
					.getSubscribedAttributes()[columnIndex-1]));
		}
	}
	class CellValue implements Comparable<CellValue>{
		private Substance substance;
		public CellValue(Substance substance) {
			if(substance!=null){
				this.substance = substance;
			}else{
				this.substance = new ObjectSubstance("");
			}
		}
		@Override
		public int compareTo(CellValue cellValue) {
			Substance otherSubstance = cellValue.substance;
			int result =  this.substance.getValue().toString().compareTo(otherSubstance.getValue().toString());
			if(result==0){
				if(this.substance.isAggr() && !otherSubstance.isAggr()){
					return -1;
				} else if(!this.substance.isAggr() && otherSubstance.isAggr()){
					return 1;
				}
			}
			return result;
		}	
		@Override
		public String toString(){
			return substance!=null?this.substance.toString():"";
		}
	}
}