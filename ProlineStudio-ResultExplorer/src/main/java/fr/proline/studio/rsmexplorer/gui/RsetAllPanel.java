package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.Peaklist;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseRsetProperties;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.PropertiesTopComponent;
import fr.proline.studio.rsmexplorer.actions.identification.PropertiesAction;
import fr.proline.studio.rsmexplorer.tree.identification.IdTransferable;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.utils.PropertiesProviderInterface;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Panel to display all Search Results (rset) imported
 * @author JM235353
 */
public class RsetAllPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private ResultSetTable m_resultSetTable;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
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
        
        m_markerContainerPanel = new MarkerContainerPanel(scrollPane, m_resultSetTable);
        
        scrollPane.setViewportView(m_resultSetTable);
	m_resultSetTable.setFillsViewportHeight(true);
	m_resultSetTable.setViewport(scrollPane.getViewport());
        


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        add(m_markerContainerPanel, c);
        

        
    }
    
    public void setData(long taskId, ArrayList<ResultSet> resultSetList) {
        ((ResultSetTableModel) m_resultSetTable.getModel()).setData(resultSetList);
        
        // select the first row
        if ((resultSetList != null) && (resultSetList.size() > 0)) {
            m_resultSetTable.getSelectionModel().setSelectionInterval(0, 0);
            
            m_markerContainerPanel.setMaxLineNumber(resultSetList.size());
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
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }
    
    private class ResultSetTable extends DecoratedMarkerTable {

        
        private JPopupMenu m_popup = null;
        
        public ResultSetTable() {
            setDragEnabled(true);
            TableTransferHandler handler = new TableTransferHandler();
            setTransferHandler(handler);
            
            // remove mouse listeners and put them back later
            // (our listener must be executed first so the selection
            // of nodes have not been already managed
            final MouseListener[] ls = getMouseListeners();
            for (final MouseListener l : ls) {
                removeMouseListener(l);
            }
            
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        getPopup().show(e.getComponent(), e.getX(), e.getY());
                    }
                    
                    
                }
                
                @Override
                public void mousePressed(MouseEvent e) {
                    // Need to explicitly start a drag operation when the mouse is pressed.
                    // Otherwise drags are only started *after* the user has clicked once
                    // on the JTable (this could be down to the L&F not doing the right thing).

                    
                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    
                    
                    // hack to avoid a conflict with selection manager
                    // --- begin
                    int row = m_resultSetTable.rowAtPoint(e.getPoint());
                    if (row == -1) {
                        return;
                    }
                    
                    int[] selectedRows = m_resultSetTable.getSelectedRows();

                    for (int i = 0; i < selectedRows.length; i++) {
                        if (selectedRows[i] == row) {
                            return;
                        }
                    }
                    // -- end


                        
                    getTransferHandler().exportAsDrag(m_resultSetTable, e, TransferHandler.COPY);

                }
            });
            
            // put back mouse listeners
            for (final MouseListener l : ls) {
                addMouseListener(l);
            }
        }
        
        private JPopupMenu getPopup() {
            if (m_popup == null) {
                m_popup = new JPopupMenu();
                
                // create the actions
                ArrayList<PropertiesFromTableAction> popupActions = new ArrayList<>(1);  // <--- get in sync

                PropertiesFromTableAction propertiesAction = new PropertiesFromTableAction();
                popupActions.add(propertiesAction);


                // add actions to popup
                for (int i = 0; i < popupActions.size(); i++) {
                    PropertiesFromTableAction action = popupActions.get(i);
                    if (action == null) {
                        m_popup.addSeparator();
                    } else {
                        m_popup.add(action.getPopupPresenter());
                    }
                }
                
            }
            return m_popup;
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
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }
    
    
    private class PropertiesFromTableAction extends AbstractAction {

        public PropertiesFromTableAction() {
            super(NbBundle.getMessage(PropertiesAction.class, "CTL_PropertiesAction"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {

            ResultSetTableModel model = (ResultSetTableModel) m_resultSetTable.getModel();
            
            long projectId = m_dataBox.getProjectId();
            
            int[] selectedRows = m_resultSetTable.getSelectedRows();
            int nbSelectedRset = selectedRows.length;
            final ResultsetPropertiesProvider[] resultPropertiesProviderArray = new ResultsetPropertiesProvider[nbSelectedRset];
            for (int i = 0; i < nbSelectedRset; i++) {
                int rowInModel = m_resultSetTable.convertRowIndexToModel(selectedRows[i]);
                
                ResultSet rset = model.getResultSet(rowInModel);
                resultPropertiesProviderArray[i] = new ResultsetPropertiesProvider(rset, projectId, rset.getName());
            }
            
            String rsetFileName = resultPropertiesProviderArray[0].getResultset().getMsiSearch().getResultFileName();
            if (rsetFileName != null) {
                int index = rsetFileName.lastIndexOf('.');
                if (index != -1) {
                    rsetFileName = rsetFileName.substring(0, index);
                }
            } else {
                rsetFileName = ""; // should not happen
            }

            String dialogName = "Properties "+rsetFileName;

            final PropertiesTopComponent win = new PropertiesTopComponent(dialogName);
            win.open();
            win.requestActive();


            // load data for properties
            DataLoadedCallback dataLoadedCallback = new DataLoadedCallback(nbSelectedRset) {

                @Override
                public void run() {
                    m_nbDataToLoad--;
                    if (m_nbDataToLoad == 0) {

                        win.setProperties(resultPropertiesProviderArray);


                    }
                }
            };

            int nbDataToLoad = resultPropertiesProviderArray.length;
            for (int i = 0; i < nbDataToLoad; i++) {
                resultPropertiesProviderArray[i].loadDataForProperties(dataLoadedCallback);
            }
        }

        public JMenuItem getPopupPresenter() {
            return new JMenuItem(this);
        }
    }
    
    public abstract class DataLoadedCallback implements Runnable {

        protected int m_nbDataToLoad = 0;

        public DataLoadedCallback(int nb) {
            m_nbDataToLoad = nb;
        }

        @Override
        public abstract void run();
    }
    
    public class ResultsetPropertiesProvider implements PropertiesProviderInterface {

        private ResultSet m_rset = null;
        private String m_name = null;
        private long m_projectId = -1;

        public ResultsetPropertiesProvider(ResultSet rset, long projectId, String name) {
            m_rset = rset;
            m_name = name;
            m_projectId = projectId;
        }

        @Override
        public Sheet createSheet() {
            return PropertiesAction.createSheet(null, m_rset, null);
        }

        @Override
        public void loadDataForProperties(final Runnable callback) {


            AbstractDatabaseCallback taskCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    callback.run();


                }
            };



            //Load ResultSet Extra Data
            DatabaseRsetProperties task = new DatabaseRsetProperties(taskCallback, m_projectId, m_rset, m_name);
            task.setPriority(AbstractDatabaseTask.Priority.HIGH_3); // highest priority



            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
        
        public ResultSet getResultset() {
            return m_rset;
        }
    }


    
    private static class ResultSetTableModel extends DecoratedTableModel {

        public static final int COLTYPE_RSET_ID = 0;
        public static final int COLTYPE_RSET_NAME = 1;
        public static final int COLTYPE_PEAKLIST_PATH = 2;
        public static final int COLTYPE_MSISEARCH_FILE_NAME = 3;
        public static final int COLTYPE_MSISEARCH_FILE_DIRECTORY = 4;
        public static final int COLTYPE_MSISEARCH_SEARCH_DATE = 5;
        private static final String[] columnNames = { "id", "Search Result Name", "Peaklist Path", "MSISearch File Name", "MSISearch File Directory", "Search Date" };
        
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
                    return Long.class;
                default:
                    return String.class;
            }
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
                    return Long.valueOf(rset.getId());
                }
                case COLTYPE_RSET_NAME: {
                    return rset.getName();
                }
                case COLTYPE_PEAKLIST_PATH: {
                    MsiSearch msiSearch = rset.getMsiSearch();
                    if (msiSearch == null) {
                        return "";
                    }
                    Peaklist peaklist = msiSearch.getPeaklist();
                    if (peaklist == null) {
                        return "";
                    }
                    return peaklist.getPath();
                }
                case COLTYPE_MSISEARCH_FILE_NAME: {
                    MsiSearch msiSearch = rset.getMsiSearch();
                    if (msiSearch == null) {
                        return "";
                    }
                    return msiSearch.getResultFileName();
                }
                case COLTYPE_MSISEARCH_FILE_DIRECTORY: {
                    MsiSearch msiSearch = rset.getMsiSearch();
                    if (msiSearch == null) {
                        return "";
                    }
                    return msiSearch.getResultFileDirectory();
                }
                case COLTYPE_MSISEARCH_SEARCH_DATE: {
                    MsiSearch msiSearch = rset.getMsiSearch();
                    if (msiSearch == null) {
                        return "";
                    }
                    Timestamp timeStamp = msiSearch.getDate();
                    return m_df.format(timeStamp);

                }
            }
            return null; // should not happen
        }

        @Override
        public String getToolTipForHeader(int col) {
            return getColumnName(col);
        }
        
    }
    private static final DateFormat m_df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
    
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
            
            IdTransferable.TransferData data = new IdTransferable.TransferData();
            data.setResultSetList(resultSetList);
            Integer transferKey =  IdTransferable.register(data);

            
            
            return new IdTransferable(transferKey, m_dataBox.getProjectId());
            
        }
        
        /*@Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            super.exportAsDrag(comp, e, action);

        }*/

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {

            // clean all transferred data
            IdTransferable.clearRegisteredData();
        }
    }
    

    
    
}
