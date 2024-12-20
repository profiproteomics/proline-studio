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



import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.BookmarkMarker;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.*;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.ProteinSetTableModel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.ImportTableSelectionInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.List;

/**
 * In : Window which display Protein Sets of a Result Summary
 * - Panel used to display Protein Sets (at the top)
 * 
 * @author JM235353
 */
public class RsmProteinSetPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    
    private JScrollPane m_proteinSetScrollPane;
    private ProteinSetTable m_proteinSetTable;

    private MarkerContainerPanel m_markerContainerPanel;
    
    private final boolean m_firstPanel;
    private JButton m_decoyButton;

    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    
    private boolean m_hideFirstTime = true; // to initialize visible column on first display (then user cfg will be used)

    /**
     * Creates new form RsmProteinSetPanel
     */
    public RsmProteinSetPanel(boolean firstPanel) {
        
        m_firstPanel = firstPanel;
        
        initComponents();

    }

    public void setData(Long taskId, DProteinSet[] proteinSets, boolean finished) {
        
        // update toolbar
        boolean mergedData = false;
        ResultSummary rsm = (ResultSummary) m_dataBox.getData(ResultSummary.class);
        if (rsm != null) {

            if (m_firstPanel) {
                m_decoyButton.setEnabled(rsm.getDecoyResultSummary() != null);
            }
            ResultSet.Type rsType = rsm.getResultSet().getType();
            mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
        }
 
        ProteinSetTableModel  tableModel = (ProteinSetTableModel) ((CompoundTableModel)m_proteinSetTable.getModel()).getBaseModel();
        tableModel.setData(taskId, proteinSets, mergedData);

        // select the first row
        if ((proteinSets != null) && (proteinSets.length > 0)) {
            m_proteinSetTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(proteinSets.length);
            
            if (!m_firstPanel) {
                m_markerContainerPanel.removeAllMarkers();
            }

            if(m_hideFirstTime){
                // hide the geneName columns
                List<Integer> listIdsToHide = tableModel.getDefaultColumnsToHide();
                for (Integer id : listIdsToHide) {
                    m_proteinSetTable.getColumnExt(m_proteinSetTable.convertColumnIndexToView(id)).setVisible(false);
                }
                m_hideFirstTime = false;
            }
        }
        
        m_infoToggleButton.updateInfo();
        
        if (finished) {
            m_proteinSetTable.setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_proteinSetTable.dataUpdated(subTask, finished);
        if (m_hideFirstTime) {
            // hide geneName column
            List<Integer> listIdsToHide = ((ProteinSetTableModel) ((CompoundTableModel)m_proteinSetTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
            for (Integer id : listIdsToHide) {
                m_proteinSetTable.getColumnExt(m_proteinSetTable.convertColumnIndexToView(id)).setVisible(false);
            }
            m_hideFirstTime = false;

        }
    }

    public DProteinSet getSelectedProteinSet() {

        // Retrieve Selected Row
        int selectedRow = m_proteinSetTable.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;
        }

        // convert according to the sorting
        selectedRow = m_proteinSetTable.convertRowIndexToModel(selectedRow);

        CompoundTableModel compoundTableModel = ((CompoundTableModel)m_proteinSetTable.getModel());
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

        // Retrieve ProteinSet selected
        ProteinSetTableModel tableModel = (ProteinSetTableModel) compoundTableModel.getBaseModel();
        return tableModel.getProteinSet(selectedRow);
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
        getGlobalTableModelInterface().addSingleValue(v);
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_proteinSetTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_proteinSetTable;
    }
    
    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_proteinSetTable;
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

    private String getTopComponentName() {

        Container c = getParent();
        while ((c != null) && !(c instanceof AbstractTopPanel)) {
            c = c.getParent();
        }
        if ((c != null) && (c instanceof AbstractTopPanel)) {
            return ((AbstractTopPanel) c).getTitle();
        }
        return "";
    }

    
    
    private void initComponents() {


        setLayout(new BorderLayout());

        final JPanel proteinSetPanel = createProteinSetPanel();


        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                proteinSetPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(proteinSetPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);  
        layeredPane.add(m_searchToggleButton.getSearchPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER+1));


    }
    
    
    private JPanel createProteinSetPanel() {
        
        JPanel proteinSetPanel = new JPanel();
        proteinSetPanel.setBounds(0, 0, 500, 400);
        proteinSetPanel.setLayout(new BorderLayout());
        
        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        proteinSetPanel.add(toolbar, BorderLayout.WEST);
        proteinSetPanel.add(internalPanel, BorderLayout.CENTER);

        return proteinSetPanel;
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        if (m_firstPanel) {
            
            // Decoy Button
            m_decoyButton = new JButton(IconManager.getIcon(IconManager.IconType.DATASET_RSM_DECOY));
            m_decoyButton.setToolTipText("Display Decoy Data");
            m_decoyButton.setEnabled(false);

            m_decoyButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    ResultSummary rsm = (ResultSummary) m_dataBox.getData(ResultSummary.class);
                    ResultSummary decoyRsm = rsm.getDecoyResultSummary();
                    if (decoyRsm == null) {
                        return;
                    }

                    String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);
                    
                    AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(savedWindow);
                    WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, false, WindowSavedManager.SAVE_WINDOW_FOR_RSM);
                    wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);

                    // open a window to display the window box
                    DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                    WindowManager.getDefault().getMainWindow().displayWindow(win);

                }
            });

           
            toolbar.add(m_decoyButton);
            
        }
        
        m_settingsButton = new SettingsButton(((ProgressInterface) m_proteinSetTable.getModel()), m_proteinSetTable);
        
        
        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_proteinSetTable, m_proteinSetTable, ((CompoundTableModel) m_proteinSetTable.getModel()));
        toolbar.add(m_searchToggleButton);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_proteinSetTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_proteinSetTable.getModel()), "Protein Sets", m_proteinSetTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_proteinSetTable.getModel())) {
           
            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getUserName(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i!=null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataAnalyzerWindowBoxManager.addTableInfo(tableInfo);
            }
        };
        toolbar.add(m_addCompareDataButton);
        
        m_infoToggleButton = new InfoToggleButton(m_proteinSetTable, m_proteinSetTable);
        toolbar.add(m_infoToggleButton);
        
        return toolbar;
    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // create objects
        m_proteinSetScrollPane = new JScrollPane();
        
        m_proteinSetTable = new ProteinSetTable();
        m_proteinSetTable.setModel(new CompoundTableModel(new ProteinSetTableModel((LazyTable)m_proteinSetTable), true));
        // hide the id column
        m_proteinSetTable.getColumnExt(m_proteinSetTable.convertColumnIndexToView(ProteinSetTableModel.COLTYPE_PROTEIN_SET_ID)).setVisible(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_proteinSetScrollPane, (ProteinSetTable) m_proteinSetTable);
        
        m_proteinSetScrollPane.setViewportView(m_proteinSetTable);
        m_proteinSetTable.setFillsViewportHeight(true);
        m_proteinSetTable.setViewport(m_proteinSetScrollPane.getViewport());
        
        //m_proteinSetTable.displayColumnAsPercentage(ProteinSetTableModel.COLTYPE_PROTEIN_SCORE);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        return internalPanel;
    }                 
    
    public void selectData(HashSet data) {
        m_proteinSetTable.importSelection(data);
    }

    
    private class ProteinSetTable extends LazyTable implements ImportTableSelectionInterface, CrossSelectionInterface, InfoInterface  {

        
        public ProteinSetTable() {
            super(m_proteinSetScrollPane.getVerticalScrollBar() );

        }
        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
        /** 
         * Called whenever the value of the selection changes.
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            
            super.valueChanged(e);
            
            if (selectionWillBeRestored) {
                return;
            }
            
            if (e.getValueIsAdjusting()) {
                // value is adjusting, so valueChanged will be called again
                return;
            }
 
            m_dataBox.addDataChanged(DProteinSet.class);
            m_dataBox.propagateDataChanged();

        }
        
        /*public boolean selectProteinSet(Long proteinSetId, String searchText) {
            ProteinSetTableModel tableModel = (ProteinSetTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(proteinSetId);
            if (row == -1) {
                return false;
            }
            row = ((CompoundTableModel)getModel()).convertBaseModelRowToCompoundRow(row);
            if (row == -1) {
                return false;
            }
            
            // JPM.hack we need to keep the search text
            // to be able to give it if needed to the panel
            // which display proteins of a protein set
            //m_searchTextBeingDone = searchText;
            
            // must convert row index if there is a sorting
            row = convertRowIndexToView(row);
            
            // select the row
            getSelectionModel().setSelectionInterval(row, row);
            
            // scroll to the row
            scrollRowToVisible(row);

            //m_searchTextBeingDone = null;
            
            return true;
        }
        //private String m_searchTextBeingDone = null;*/


        public void dataUpdated(SubTask subTask, boolean finished) {
            
            LastAction keepLastAction = m_lastAction;
            try {
            
            
            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((ProteinSetTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
            } finally {
                selectionWillBeRestored(false);
            }

            
            
            // restore selected row
            if (rowSelectedInModel != -1) {
                int rowSelectedInView = convertRowIndexToView(rowSelectedInModel);
                //getSelectionModel().setSelectionInterval(rowSelectedInView, rowSelectedInView);
                setSelection(rowSelectedInView);

                
                // if the subtask correspond to the loading of the data of the sorted column,
                // we keep the row selected visible
                if (((keepLastAction == LastAction.ACTION_SELECTING ) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
                    scrollRowToVisible(rowSelectedInView);
                }
                    
            }

            } finally {

                m_lastAction = keepLastAction;
 
            }
            
            if (finished) {
                setSortable(true);
            }
        }

        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        @Override
        public void importSelection(HashSet selectedData) {
            
            ListSelectionModel selectionTableModel = getSelectionModel();
            selectionTableModel.clearSelection();
            
            int firstRow = -1;
            ProteinSetTableModel model = (ProteinSetTableModel) ((CompoundTableModel) m_proteinSetTable.getModel()).getBaseModel();
            int rowCount = model.getRowCount();
            for (int i=0;i<rowCount;i++) {
                Object v = model.getValueAt(i, ProteinSetTableModel.COLTYPE_PROTEIN_SET_ID);
                if (selectedData.remove(v)) {
                    if (firstRow == -1) {
                        firstRow = i;
                    }
                    selectionTableModel.addSelectionInterval(i, i);
                        BookmarkMarker marker = new BookmarkMarker(i);
                        m_markerContainerPanel.addMarker(marker);
                    if (selectedData.isEmpty()) {
                        break;
                    }
                }
            }
            
            // scroll to the first row
            if (firstRow != -1) {
                final int row = firstRow;
                scrollToVisible(row);
            }
            
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(RsmProteinSetPanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
            return count+((count>1) ? " Proteins Sets" : " Protein Set");
        }

        
        
    }
    

    
}