/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.Peaklist;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseRsetProperties;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.*;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.gui.model.properties.IdentificationPropertiesTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.TimestampRenderer;
import fr.proline.studio.rsmexplorer.tree.identification.IdTransferable;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.*;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Panel to display all Search Results (rset) imported
 *
 * @author JM235353
 */
public class RsetAllPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private ResultSetTable m_resultSetTable;

    private MarkerContainerPanel m_markerContainerPanel;

    private SearchToggleButton m_searchToggleButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;

    private JScrollPane m_scrollPane;

    public RsetAllPanel() {

        setLayout(new BorderLayout());

        final JPanel resultSetPanel = this.createResultSetPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                resultSetPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                layeredPane.revalidate();
                layeredPane.repaint();

            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        add(layeredPane, BorderLayout.CENTER);

        layeredPane.add(resultSetPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), JLayeredPane.PALETTE_LAYER);

    }
    
    private JPanel createResultSetPanel(){
        JPanel panel = new JPanel();
        
        panel.setLayout(new BorderLayout());
        panel.setBounds(0, 0, 500, 400);
        
        JPanel internalPanel = initInternalPanel();
        panel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        panel.add(toolbar, BorderLayout.WEST);
        
        return panel;
    }

    private JPanel initInternalPanel() {

        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        m_scrollPane = new JScrollPane();
        m_resultSetTable = new ResultSetTable();
        ResultSetTableModel resultSetTableModel = new ResultSetTableModel();

        m_resultSetTable.setModel(new CompoundTableModel((GlobalTableModelInterface) resultSetTableModel, true));

        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, m_resultSetTable);

        m_scrollPane.setViewportView(m_resultSetTable);
        m_resultSetTable.setFillsViewportHeight(true);
        m_resultSetTable.setViewport(m_scrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        JButton refreshButton = new JButton(IconManager.getIcon(IconManager.IconType.REFRESH));
        refreshButton.setToolTipText("Refresh");
        refreshButton.addActionListener(e -> m_dataBox.dataChanged());

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_resultSetTable, m_resultSetTable, ((CompoundTableModel) m_resultSetTable.getModel()));

        // Filter Button
        m_filterButton = new FilterButton(((CompoundTableModel) m_resultSetTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.dataChanged();
            }

        };

        // Export Button
        m_exportButton = new ExportButton( m_resultSetTable, "All Imported Sets", m_resultSetTable);

        toolbar.add(refreshButton);
        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);

        return toolbar;
    }

    public void setData(long taskId, ArrayList<ResultSet> resultSetList) {
        CompoundTableModel compoundTableModel = (CompoundTableModel) m_resultSetTable.getModel();
        ResultSetTableModel resultSetTableModel = (ResultSetTableModel) compoundTableModel.getBaseModel();

        resultSetTableModel.setData(resultSetList);

        // select the first row
        if ((resultSetList != null) && (resultSetList.size() > 0)) {
            m_resultSetTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(resultSetList.size());
        }

        m_resultSetTable.setSortable(true);
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

    @Override
    public void addSingleValue(Object v) {
        ((CompoundTableModel) m_resultSetTable.getModel()).addSingleValue(v);
    }

    private class ResultSetTable extends LazyTable implements ProgressInterface {

        public ResultSetTable() {
            super(m_scrollPane.getVerticalScrollBar());

            setDragEnabled(true);
            TableTransferHandler handler = new TableTransferHandler();
            setTransferHandler(handler);

            setDefaultRenderer(Timestamp.class, new TimestampRenderer(getDefaultRenderer(String.class)));

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

                    if (e.isControlDown()) {
                        m_resultSetTable.addRowSelectionInterval(row, row);
                    } else if (e.isShiftDown()) {
                        int minIndex = m_resultSetTable.getSelectionModel().getMinSelectionIndex();
                        if ((minIndex == -1) || (row < minIndex)) {
                            minIndex = row;
                        }
                        int maxIndex = m_resultSetTable.getSelectionModel().getMaxSelectionIndex();
                        if ((maxIndex == -1) || (row > maxIndex)) {
                            maxIndex = row;
                        }
                        m_resultSetTable.setRowSelectionInterval(minIndex, maxIndex);
                    } else {
                        m_resultSetTable.setRowSelectionInterval(row, row);
                    }

                    getTransferHandler().exportAsDrag(m_resultSetTable, e, TransferHandler.COPY);

                }
            });

            // put back mouse listeners
            for (final MouseListener l : ls) {
                addMouseListener(l);
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            if (e.getValueIsAdjusting()) {
                // value is adjusting, so valueChanged will be called again
                return;
            }
            
            m_dataBox.addDataChanged(ResultSet.class);
            m_dataBox.propagateDataChanged();

        }

        @Override
        public TablePopupMenu initPopupMenu() {

            TablePopupMenu popupMenu = new TablePopupMenu();
            popupMenu.addAction(new PropertiesFromTableAction());

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        @Override
        public boolean isLoaded() {
            return true;
            //return m_dataBox.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
            //return m_dataBox.getLoadingPercentage();
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

    private class PropertiesFromTableAction extends AbstractTableAction {

        public PropertiesFromTableAction() {
            super("Properties");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {

            int nbResultSet = selectedRows.length;

            ResultSetTableModel rsetModel = (ResultSetTableModel) ((CompoundTableModel) m_resultSetTable.getModel()).getBaseModel();

            final long projectId = m_dataBox.getProjectId();

            ArrayList<ResultSet> resultSetList = new ArrayList<>(nbResultSet);

            for (int i = 0; i < nbResultSet; i++) {
                int rowInModel = m_resultSetTable.convertRowIndexToModel(selectedRows[i]);

                ResultSet rset = rsetModel.getResultSet(rowInModel);
                resultSetList.add(rset);
            }

            String rsetFileName = resultSetList.get(0).getMsiSearch().getResultFileName();
            if (rsetFileName != null) {
                int index = rsetFileName.lastIndexOf('.');
                if (index != -1) {
                    rsetFileName = rsetFileName.substring(0, index);
                }
            } else {
                rsetFileName = ""; // should not happen
            }

            String dialogName = "Properties " + rsetFileName;

            // new Properties window
            WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(dialogName, "Properties", IconManager.IconType.DOCUMENT_LIST, true);
            final IdentificationPropertiesTableModel _model = new IdentificationPropertiesTableModel();
            windowBox.setEntryData(-1L, _model);
            DataBoxViewerTopPanel win2 = new DataBoxViewerTopPanel(windowBox);
            WindowManager.getDefault().getMainWindow().displayWindow(win2);


            //JPM.HACK ! Impossible to set the max number of lines differently in this case
            DataboxGeneric databoxGeneric = ((DataboxGeneric) windowBox.getEntryBox());
            final GenericPanel genericPanel = (GenericPanel) databoxGeneric.getPanel();

            final int loadingId = databoxGeneric.setLoading();

            // load data for properties
            final RsetCallback dataLoadedCallback2 = new RsetCallback(nbResultSet) {

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    m_nbDataToLoad--;
                    if (m_nbDataToLoad == 0) {

                        databoxGeneric.setLoaded(loadingId);

                        _model.setData(projectId, resultSetList);
                        genericPanel.setMaxLineNumber(_model.getRowCount());

                    }
                }

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }



            };

            for (int i = 0; i < nbResultSet; i++) {

                ResultSet rset = resultSetList.get(i);
                DatabaseRsetProperties task = new DatabaseRsetProperties(dataLoadedCallback2, projectId, rset, rset.getName());
                task.setPriority(AbstractDatabaseTask.Priority.HIGH_3); // highest priority

                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }

        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(selectedRows.length > 0);
        }

    }
  



    public abstract static class RsetCallback extends AbstractDatabaseCallback {

        protected int m_nbDataToLoad;
        
        public RsetCallback(int nbDataToLoad) {
            m_nbDataToLoad = nbDataToLoad;
        }

    };
    
    private static class ResultSetTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

        public static final int COLTYPE_RSET_ID = 0;
        public static final int COLTYPE_RSET_NAME = 1;
        public static final int COLTYPE_PEAKLIST_PATH = 2;
        public static final int COLTYPE_MSISEARCH_FILE_NAME = 3;
        public static final int COLTYPE_MSISEARCH_FILE_DIRECTORY = 4;
        public static final int COLTYPE_MSISEARCH_SEARCH_DATE = 5;
        private static final String[] columnNames = {"id", "Search Result Name", "Peaklist Path", "MSISearch File Name", "MSISearch File Directory", "Search Date"};

        private ArrayList<ResultSet> m_resultSetList = null;

        public void setData(ArrayList<ResultSet> resultSetList) {
            m_resultSetList = resultSetList;
            fireTableDataChanged();

        }

        public ResultSet getResultSet(int row) {
            if (m_resultSetList == null) {
                return null;
            }
            if (m_resultSetList.size() <= row) {
                return null;
            }
            return m_resultSetList.get(row);
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLTYPE_RSET_ID:
                    return Long.class;
                case COLTYPE_MSISEARCH_SEARCH_DATE:
                    return Timestamp.class;
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
                        return null;
                    }
                    return msiSearch.getDate();
                }
            }
            return null; // should not happen
        }

        @Override
        public String getToolTipForHeader(int col) {
            return getColumnName(col);
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            return null;
        }

        @Override
        public GlobalTableModelInterface getFrozzenModel() {
            return this;
        }

        @Override
        public Long getTaskId() {
            return -1L; // not needed
        }

        @Override
        public LazyData getLazyData(int row, int col) {
            return null;
        }

        @Override
        public void givePriorityTo(Long taskId, int row, int col) {
            // nothing to do
        }

        @Override
        public void sortingChanged(int col) {
            // nothing to do
        }

        @Override
        public int getSubTaskId(int col) {
            return -1;
        }

        @Override
        public String getDataColumnIdentifier(int columnIndex) {
            return getColumnName(columnIndex);
        }

        @Override
        public Class getDataColumnClass(int columnIndex) {
            return getColumnClass(columnIndex);
        }

        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            return getValueAt(rowIndex, columnIndex);
        }

        @Override
        public int[] getKeysColumn() {
            return null;
        }

        @Override
        public int getInfoColumn() {
            return -1;
        }

        @Override
        public void setName(String name) {
            // nothing to do
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Map<String, Object> getExternalData() {
            return null;
        }

        @Override
        public PlotInformation getPlotInformation() {
            return null;
        }

        @Override
        public ArrayList<ExtraDataType> getExtraDataTypes() {
            return null;
        }

        @Override
        public Object getValue(Class c) {
            return getSingleValue(c);
        }

        @Override
        public Object getRowValue(Class c, int row) {
            return null;
        }

        @Override
        public Object getColValue(Class c, int col) {
            return null;
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

            ConvertValueInterface longConverter = new ConvertValueInterface() {
                @Override
                public Object convertValue(Object o) {
                    if (o == null) {
                        return null;
                    }

                    return Integer.parseInt(o.toString());
                }

            };

            filtersMap.put(ResultSetTableModel.COLTYPE_RSET_ID, new IntegerFilter(getColumnName(ResultSetTableModel.COLTYPE_RSET_ID), longConverter, ResultSetTableModel.COLTYPE_RSET_ID));

            filtersMap.put(ResultSetTableModel.COLTYPE_RSET_NAME, new StringDiffFilter(getColumnName(ResultSetTableModel.COLTYPE_RSET_NAME), null, ResultSetTableModel.COLTYPE_RSET_NAME));
            filtersMap.put(ResultSetTableModel.COLTYPE_PEAKLIST_PATH, new StringDiffFilter(getColumnName(ResultSetTableModel.COLTYPE_PEAKLIST_PATH), null, ResultSetTableModel.COLTYPE_PEAKLIST_PATH));
            filtersMap.put(ResultSetTableModel.COLTYPE_MSISEARCH_FILE_NAME, new StringDiffFilter(getColumnName(ResultSetTableModel.COLTYPE_MSISEARCH_FILE_NAME), null, ResultSetTableModel.COLTYPE_MSISEARCH_FILE_NAME));
            filtersMap.put(ResultSetTableModel.COLTYPE_MSISEARCH_FILE_DIRECTORY, new StringDiffFilter(getColumnName(ResultSetTableModel.COLTYPE_MSISEARCH_FILE_DIRECTORY), null, ResultSetTableModel.COLTYPE_MSISEARCH_FILE_DIRECTORY));

            ConvertValueInterface dateConverter = new ConvertValueInterface() {
                @Override
                public Object convertValue(Object o) {
                    if (o == null) {
                        return null;
                    }
                    Timestamp timestamp = Timestamp.valueOf(o.toString());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp.getTime());

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

                    return dateFormatter.format(timestamp).toString();
                }

            };

            filtersMap.put(ResultSetTableModel.COLTYPE_MSISEARCH_SEARCH_DATE, new StringDiffFilter(getColumnName(ResultSetTableModel.COLTYPE_MSISEARCH_SEARCH_DATE), dateConverter, ResultSetTableModel.COLTYPE_MSISEARCH_SEARCH_DATE));
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

        @Override
        public PlotType getBestPlotType() {
            return null;
        }

        @Override
        public int[] getBestColIndex(PlotType plotType) {
            return null;
        }


        @Override
        public String getExportRowCell(int row, int col) {
            return ExportModelUtilities.getExportRowCell(this, row, col);
        }
        
        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return null;
        }

        @Override
        public String getExportColumnName(int col) {
            return getColumnName(col);
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
            CompoundTableModel compoundTableModel = (CompoundTableModel) table.getModel();
            ResultSetTableModel model = (ResultSetTableModel) compoundTableModel.getBaseModel();

            int[] rows = table.getSelectedRows();
            int nbRows = rows.length;

            if (nbRows == 0) {
                return null;
            }

            ArrayList<ResultSet> resultSetList = new ArrayList<>(nbRows);

            for (int i = 0; i < nbRows; i++) {
                int row = table.convertRowIndexToNonFilteredModel(rows[i]);
                ResultSet rset = model.getResultSet(row);
                resultSetList.add(rset);

            }

            IdTransferable.TransferData data = new IdTransferable.TransferData();
            data.setResultSetList(resultSetList);
            Integer transferKey = IdTransferable.register(data);

            return new IdTransferable(transferKey, m_dataBox.getProjectId());

        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {

            // clean all transferred data
            IdTransferable.clearRegisteredData();
        }
    }

}
