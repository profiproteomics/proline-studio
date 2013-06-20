package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.pattern.DataboxManager;
import fr.proline.studio.pattern.ParameterDistance;
import fr.proline.studio.utils.DecoratedMarkerTable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;



/**
 *
 * @author JM235353
 */
public class DataBoxChooserDialog extends DefaultDialog {

    
    private DataBoxTable m_dataBoxTable = null;
    
    public DataBoxChooserDialog(Window parent, ArrayList<GroupParameter> outParameters) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setSize(800,320);
        
        setButtonVisible(BUTTON_DEFAULT, false);
        setResizable(true);
        
        TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap = DataboxManager.getDataboxManager().findCompatibleStartingDataboxList(outParameters);

        initDialog(dataBoxMap);
    }
    
    public DataBoxChooserDialog(Window parent, AbstractDataBox previousDatabox) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setSize(800,320);
        
        setButtonVisible(BUTTON_DEFAULT, false);
        
        TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap = DataboxManager.getDataboxManager().findCompatibleDataboxList(previousDatabox);

        initDialog(dataBoxMap);
    }

    
    public void pack() {
        // forbid back by overloading the method
    }
            

    

    
    private void initDialog(TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap) {
        setTitle("Select a Data View");   
        

        JPanel internalPanel = new JPanel(new GridBagLayout());

        // create objects
        JScrollPane scrollPane = new JScrollPane();
        m_dataBoxTable = new DataBoxTable();
        m_dataBoxTable.setModel(new DataBoxTableModel(dataBoxMap));
        
        scrollPane.setViewportView(m_dataBoxTable);
        m_dataBoxTable.setFillsViewportHeight(true);
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(scrollPane, c);

        setInternalComponent(internalPanel);
    }
 
    @Override
    protected boolean okCalled() {
        
        int selectedRow = m_dataBoxTable.getSelectedRow();
        
        if (selectedRow == -1) {
             setStatus(true, "You must select a Data Window");
             highlight(m_dataBoxTable);
             return false;
        }

        return true;
        
       
        
    }
    
    public AbstractDataBox getSelectedDataBox() {
        int selectedRow = m_dataBoxTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        
        selectedRow = m_dataBoxTable.convertRowIndexToModel(selectedRow);
        
        return ((DataBoxTableModel)m_dataBoxTable.getModel()).getDataBox(selectedRow);
    }

    
    private class DataBoxTable extends DecoratedMarkerTable {

        public DataBoxTable() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        

    }
    
    private static class DataBoxTableModel extends AbstractTableModel {

        public static final int COLTYPE_NAME = 0;
        public static final int COLTYPE_DESCRIPTION = 1;
        private static final String[] columnNames = {"name", "description"};
        private ArrayList<AbstractDataBox> m_databoxList = null;


        public DataBoxTableModel(TreeMap<ParameterDistance, AbstractDataBox> dataBoxMap) {
            
            m_databoxList = new ArrayList<>(dataBoxMap.size());
            Iterator<ParameterDistance> it = dataBoxMap.descendingKeySet().iterator();
            while (it.hasNext()) {
                m_databoxList.add(dataBoxMap.get(it.next()));
            }
            
        }

        public AbstractDataBox getDataBox(int row) {
            if ((row<0) || (row>=m_databoxList.size())) {
                return null;
            }
            return m_databoxList.get(row);
        }
        
        
        @Override
        public Class getColumnClass(int col) {
           return String.class;
        }

        @Override
        public int getRowCount() {
            if (m_databoxList == null) {
                return 0;
            }
            return m_databoxList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AbstractDataBox dataBox = m_databoxList.get(rowIndex);
            switch (columnIndex) {
                case COLTYPE_NAME: {
                    return dataBox.getName();
                }
                case COLTYPE_DESCRIPTION: {
                    return dataBox.getDescription();
                }


            }
            return null; // should not happen
        }
    }
    
}
