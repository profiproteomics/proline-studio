package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.node.RSMTransferable;
import fr.proline.studio.utils.DecoratedMarkerTable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class RsetAllPanel extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private ResultSetTable m_resultSetTable;
    
    public RsetAllPanel() {
        initComponents();

    }
    
      private void initComponents() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // create objects
        JScrollPane scrollPane = new JScrollPane();
        m_resultSetTable = new ResultSetTable();
        m_resultSetTable.setModel(new ResultSetTableModel());
        
        MarkerContainerPanel markerContainerPanel = new MarkerContainerPanel(scrollPane, m_resultSetTable);
        
        scrollPane.setViewportView(m_resultSetTable);
	m_resultSetTable.setFillsViewportHeight(true);
	m_resultSetTable.setViewport(scrollPane.getViewport());
        


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        add(markerContainerPanel, c);
        

        
    }
    
    public void setData(long taskId, ArrayList<ResultSet> resultSetList) {
        ((ResultSetTableModel) m_resultSetTable.getModel()).setData(resultSetList);
        
        // select the first row
        if ((resultSetList != null) && (resultSetList.size() > 0)) {
            m_resultSetTable.getSelectionModel().setSelectionInterval(0, 0);
        }

    }
    
    public ResultSet getSelectedResultSet() {

        // Retrieve Selected Row
        int selectedRow = m_resultSetTable.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = m_resultSetTable.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinSet selected
        ResultSetTableModel tableModel = (ResultSetTableModel) m_resultSetTable.getModel();
        return tableModel.getResultSet(selectedRow);
    }
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    
    
    private class ResultSetTable extends DecoratedMarkerTable {
        
        public ResultSetTable() {
            setDragEnabled(true);
            TableTransferHandler handler = new TableTransferHandler();
            setTransferHandler(handler);
        }
        
        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);



            m_dataBox.propagateDataChanged(ResultSet.class);

        }
    }
    
    
    private static class ResultSetTableModel extends AbstractTableModel {

        public static final int COLTYPE_RSET_ID = 0;
        public static final int COLTYPE_RSET_FILE_NAME = 1;
        public static final int COLTYPE_RSET_FILE_PATH = 2;
        private static final String[] columnNames = { "id", "File Name", "File Path" };
        
        private ArrayList<ResultSet> m_resultSetList = null;
        
        public void setData(ArrayList<ResultSet> resultSetList) {
            m_resultSetList = resultSetList;
            fireTableDataChanged();

        }
        
        public ResultSet getResultSet(int row) {
            if (m_resultSetList == null) {
                return null;
            }
            if (m_resultSetList.size()<=row) {
                return null;
            }
            return m_resultSetList.get(row);
        }
        
        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLTYPE_RSET_ID:
                    return Integer.class;
                case COLTYPE_RSET_FILE_NAME:
                case COLTYPE_RSET_FILE_PATH:
                    return String.class;
            }
            return null; // should not happen
        }
        
        @Override
        public int getRowCount() {
            if (m_resultSetList == null) {
                return 0;
            }
            return m_resultSetList.size();
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
            ResultSet rset = m_resultSetList.get(rowIndex);
            switch (columnIndex) {
                case COLTYPE_RSET_ID: {
                    return rset.getId();
                }
                case COLTYPE_RSET_FILE_NAME: {
                    MsiSearch msiSearch = rset.getMsiSearch();
                    if (msiSearch == null) {
                        return "";
                    }
                    return msiSearch.getResultFileName();
                }
                case COLTYPE_RSET_FILE_PATH: {
                    MsiSearch msiSearch = rset.getMsiSearch();
                    if (msiSearch == null) {
                        return "";
                    }
                    return msiSearch.getResultFileDirectory();
                }
            }
            return null; // should not happen
        }
        
    }
    
    public class TableTransferHandler extends TransferHandler {
        
        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY;
        }
        
        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return false;
        }
        
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            
            ResultSetTable table = (ResultSetTable) c;
            ResultSetTableModel model = (ResultSetTableModel) table.getModel();
            
            int[] rows = table.getSelectedRows();
            int nbRows = rows.length;
        
            if (nbRows == 0) {
                return null;
            }
            
            ArrayList<ResultSet> resultSetList = new ArrayList<>(nbRows);
            
            for (int i=0;i<nbRows;i++) {
                int row = table.convertRowIndexToModel(rows[i]);
                ResultSet rset = model.getResultSet(row);
                resultSetList.add(rset);
                

            }
            
            RSMTransferable.TransferData data = new RSMTransferable.TransferData();
            data.setResultSetList(resultSetList);
            Integer transferKey =  RSMTransferable.register(data);

            
            
            return new RSMTransferable(transferKey, m_dataBox.getProjectId());
            
        }
    }
}
