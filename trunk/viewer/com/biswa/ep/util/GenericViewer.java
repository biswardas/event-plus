package com.biswa.ep.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerTask;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.aggregate.Aggregators;
import com.biswa.ep.entities.spec.AggrSpec;
import com.biswa.ep.entities.spec.CollapseSpec;
import com.biswa.ep.entities.spec.PivotSpec;
import com.biswa.ep.entities.spec.SortSpec;
public class GenericViewer extends AbstractViewer {
        private static final String SWING_1 = "-1";
        private JFrame jframe = null;
        private JTable jtable = null;
        private ViewerTableModel vtableModel = null;
        public GenericViewer(String name) {
                super(name,SWING_1);
        }
        
        @Override
        public void postConnected(ConnectionEvent connectionEvent) {
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
                                                        disConnectFromSource();
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
                                String filter = collapserTextField.getText().trim();
                                applyFilter(filter);                            
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
                                applySpecInSource(pivotSpec);
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
                                applySpecInSource(aggrSpec);
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
                                applySpecInSource(sortSpec);
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
                                applySpecInSource(collapseSpec);
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
                                removeEntryFromSource(collapserTextField.getText());
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
                                addCompiledAttributeToSource(collapserTextField.getText());
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
                                addScriptAttributeToSource(collapserTextField.getText());
                        }
                });
        }

        @Override
        public void postDisconnected(ConnectionEvent connectionEvent) {
                jframe.dispose();
                System.exit(0);
        }
        
        @Override
        public void postAttributeAdded(final ContainerEvent ce) {
                if(vtableModel!=null)vtableModel.fireTableStructureChanged();
        }

        @Override
        public void postAttributeRemoved(final ContainerEvent ce) {
                if(vtableModel!=null)vtableModel.fireTableStructureChanged();
        }
        private boolean paintDirty = false;
        private int tranId = 0;
        @Override
        public void preCommitTran(){
                tranId = GenericViewer.this.getCurrentTransactionID();
                if(!paintDirty){
                        paintDirty=true;
                        agent().invokeOperation(new ContainerTask() {                           
                                /**
                                 * 
                                 */
                                private static final long serialVersionUID = 697340123919646656L;

                                @Override
                                protected void runtask() throws Throwable { 
                                        vtableModel.fireTableDataChanged();
                                        jframe.setTitle(getName()+"/"+jtable.getRowCount()+"--"+tranId);
                                        paintDirty=false;
                                }
                        });
                }
        }

        class ViewerTableModel extends AbstractTableModel {
                /**
                 * 
                 */
                private static final long serialVersionUID = 4352652252566957454L;
                
                private String[] attributes = null;

                public ViewerTableModel() {
                        this.attributes = getAttributes();
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
                        return getSortedEntryCount();
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                        Object objectToDisplay = null;
                        if (columnIndex == 0)
                                objectToDisplay = getCachedEntries(rowIndex).id;
                        else
                                objectToDisplay = getCachedEntries(rowIndex).substance(columnIndex - 1);
                        if(objectToDisplay!=null && objectToDisplay.getClass().isArray()){
                                if(objectToDisplay.getClass().getComponentType().isPrimitive()){
                                        try {
                                                Method m =Arrays.class.getMethod("toString",objectToDisplay.getClass());
                                                objectToDisplay = m.invoke(Arrays.class, objectToDisplay);
                                        } catch (Exception e) {
                                        }
                                }else{
                                        objectToDisplay = Arrays.toString((Object[])objectToDisplay);
                                }
                        }
                        return objectToDisplay;
                }

                @Override
                public void fireTableDataChanged() {
                        clearCachedEntries();
                        super.fireTableDataChanged();
                }

                @Override
                public void fireTableStructureChanged() {
                        this.attributes = getAttributes();
                        super.fireTableStructureChanged();
                }
        }

    	public static int launchViewer() {
    		final AbstractContainer cs = ContainerContext.CONTAINER.get();
    		final String name = cs.getName() + "-Viewer";
    		SwingUtilities.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				GenericViewer viewer = new GenericViewer(name) {
    					@Override
    					public void disconnect(ConnectionEvent connectionEvent) {
    						cs.agent().disconnect(
    								new ConnectionEvent(cs.getName(), name));
    					}
    				};
    				viewer.setSourceAgent(cs.agent());
    				cs.agent()
    						.connect(
    								new ConnectionEvent(cs.getName(), name, viewer
    										.agent()));
    			}
    		});
    		return 0;
    	}
}