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


import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.data.SelectLevelEnum;
import fr.proline.studio.dam.tasks.DatabaseDatasetPTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.extendedtablemodel.*;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.BookmarkMarker;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.DataBoxViewerManager;
import fr.proline.studio.rsmexplorer.actions.ViewColocalizedPTMClustersAction;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.PTMClusterTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.RendererMouseCallback;
import fr.proline.studio.rsmexplorer.gui.renderer.SelectLevelRenderer;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.ImportTableSelectionInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class PTMClustersPanel extends HourglassPanel implements RendererMouseCallback, DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private AbstractDataBox m_dataBox;
    
    private JScrollPane m_scrollPane;
    private PTMClusterTable m_ptmClusterTable;
    private PTMClusterTableModel m_ptmClusterTableModel;
    
    private JTextField m_countModificationTextField;

    private MarkerContainerPanel m_markerContainerPanel;
    private ModifyStatusDialog m_modifyStatusDialog;

    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;


    /**
     * Creates new form PTMProteinSitePanel
     */
    public PTMClustersPanel() {
        initComponents();
    }

    
    private boolean m_hideFirstTime = true;

    public void setData(Long taskId, ArrayList<PTMCluster> ptmClusters, boolean finished) {

        m_ptmClusterTableModel.setData(taskId,ptmClusters);

        // select the first row
        if ((ptmClusters != null) && (ptmClusters.size() > 0)) {
            m_ptmClusterTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(ptmClusters.size());
            if (m_hideFirstTime) {
                setColumnsVisibility();
                m_hideFirstTime = false;
            }

        }
        
        m_infoToggleButton.updateInfo();
        m_countModificationTextField.setText(m_ptmClusterTableModel.getModificationsInfo());
        
        if (finished) {
            m_ptmClusterTable.setSortable(true);
        }

    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_ptmClusterTable.dataUpdated(subTask, finished);
        m_countModificationTextField.setText(m_ptmClusterTableModel.getModificationsInfo());
    }


    
    public PTMCluster getSelectedProteinPTMCluster() {

        // Retrieve Selected Row
        int selectedRow = m_ptmClusterTable.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = m_ptmClusterTable.convertRowIndexToModel(selectedRow);

        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_ptmClusterTable.getModel());
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

        // Retrieve ProteinPTMSite selected
        PTMClusterTableModel tableModel = (PTMClusterTableModel) compoundTableModel.getBaseModel();
        return tableModel.getProteinPTMCluster(selectedRow);
    }
    
     public List<PTMCluster> getSelectedPTMClusters() {

        // Retrieve Selected Row
        int[] selectedRows = m_ptmClusterTable.getSelectedRows();
        int nbSelectedRows = selectedRows.length;
        List<PTMCluster> selectedPtmClusters= new ArrayList<>(nbSelectedRows);
        // nothing selected
        if (nbSelectedRows == 0) {
            return selectedPtmClusters;
        }
       
        // convert according to the sorting
        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_ptmClusterTable.getModel());

        for(int i=0; i<nbSelectedRows; i++){
            int rowModelIndex  = m_ptmClusterTable.convertRowIndexToModel(selectedRows[i]);
            int convertedSelectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(rowModelIndex);
            
            // Retrieve ProteinPTMSite selected
            selectedPtmClusters.add(m_ptmClusterTableModel.getProteinPTMCluster(convertedSelectedRow));
        }
        
        return selectedPtmClusters;
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
        return (GlobalTableModelInterface) m_ptmClusterTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_ptmClusterTable;
    }
    
    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_ptmClusterTable;
    }
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public void mouseAction(MouseEvent e) {
        int col = m_ptmClusterTable.columnAtPoint(e.getPoint());
        int row = m_ptmClusterTable.rowAtPoint(e.getPoint());
        if (row != -1) {
            int rowModelIndex = m_ptmClusterTable.convertRowIndexToModel(row);
            if (m_ptmClusterTable.convertColumnIndexToModel(col) == PTMClusterTableModel.COLTYPE_PTM_CLUSTER_SELECTION_LEVEL) {
                m_ptmClusterTable.getSelectionModel().setSelectionInterval(row, row);
                if (m_ptmClusterTableModel.isRowEditable(rowModelIndex)) {
                    if (m_modifyStatusDialog == null) {
                        m_modifyStatusDialog = new ModifyStatusDialog();
                    }
                    int[] selectedViewRows = new int[1];
                    selectedViewRows[0] = row;
                    m_modifyStatusDialog.setSelectedRows(selectedViewRows);
                    m_modifyStatusDialog.setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y);
                    m_modifyStatusDialog.setVisible(true);
                }
            }
        }
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    private void initComponents() {

        setLayout(new BorderLayout());

        final JPanel proteinPTMClusterPanel = createProteinPTMClusterPanel();
        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                proteinPTMClusterPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(proteinPTMClusterPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);  
        layeredPane.add(m_searchToggleButton.getSearchPanel(), new Integer(JLayeredPane.PALETTE_LAYER+1));
        setColumnsVisibility();
    }
    
    private void setColumnsVisibility() {
        // hide the rawAbundance  and selectionLevel columns
        List<Integer> listIdsToHide =m_ptmClusterTableModel.getDefaultColumnsToHide();
        List<TableColumn> columns = m_ptmClusterTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_ptmClusterTable.getColumnExt(m_ptmClusterTable.convertColumnIndexToView(id)).setVisible(false);
            }
        }
    }
    
    private JPanel createProteinPTMClusterPanel() {
        
        JPanel proteinPTMClusterPanel = new JPanel();
        proteinPTMClusterPanel.setBounds(0, 0, 500, 400);
        proteinPTMClusterPanel.setLayout(new BorderLayout());
        
        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        proteinPTMClusterPanel.add(toolbar, BorderLayout.WEST);
        proteinPTMClusterPanel.add(internalPanel, BorderLayout.CENTER);


        return proteinPTMClusterPanel;
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_ptmClusterTable.getModel()), m_ptmClusterTable);
        
        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_ptmClusterTable, m_ptmClusterTable, ((CompoundTableModel) m_ptmClusterTable.getModel()));
        toolbar.add(m_searchToggleButton);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_ptmClusterTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_ptmClusterTable.getModel()), "Protein Sets", m_ptmClusterTable);

        JButton saveButton = new JButton();
        saveButton.setIcon(IconManager.getIcon(IconManager.IconType.SAVE_SETTINGS));
        saveButton.setToolTipText("Save Annotated Modifications Clusters");
        saveButton.addActionListener(e -> {
            PTMDataset ptmDataset = (PTMDataset) m_dataBox.getData(PTMDataset.class);
            DatabaseDatasetPTMsTask task = new  DatabaseDatasetPTMsTask(null);
            task.initAddAnnotatedPTMDataset(m_dataBox.getProjectId(),  ptmDataset);
            task.fetchData();

            m_infoToggleButton.updateInfo();

            DataBoxViewerManager.loadedDataModified(m_dataBox.getProjectId(), m_dataBox.getRsetId(), m_dataBox.getRsmId(), PTMCluster.class,
                    new ArrayList(), DataBoxViewerManager.REASON_PTMDATASET_SAVED);
        });

        JButton mergeButton = new JButton();
        mergeButton.setIcon(IconManager.getIcon(IconManager.IconType.MERGE_PTM));
        mergeButton.setToolTipText("Merge selected clusters");
        mergeButton.addActionListener(e -> {
//            PTMClusterTableModel m = (PTMClusterTableModel) ((CompoundTableModel)m_ptmClusterTable.getModel()).getBaseModel();
            if(m_ptmClusterTableModel.getRowCount() > 0 && m_ptmClusterTable.getSelectedRowCount() > 1) {
                List<PTMCluster> clusters = getSelectedPTMClusters();
                m_logger.debug(" Merge "+ clusters.size()+" clusters  ");
                PTMDataset ptmDS = clusters.get(0).getPTMDataset();
                ptmDS.mergeClusters(clusters);

                m_ptmClusterTable.dataUpdated(null, true);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
                // propagate modifications to the previous views
                DataBoxViewerManager.loadedDataModified(m_dataBox.getProjectId(), m_dataBox.getRsetId(), m_dataBox.getRsmId(), PTMCluster.class,
                       new ArrayList(clusters), DataBoxViewerManager.REASON_PTMCLUSTER_MERGED);
            }
        });

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(saveButton);
        toolbar.add(mergeButton);

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_ptmClusterTable.getModel())) {
           
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
        
        m_infoToggleButton = new InfoToggleButton(m_ptmClusterTable, m_ptmClusterTable);
        
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
        m_scrollPane = new JScrollPane();
        
        m_ptmClusterTable = new PTMClusterTable();
        m_ptmClusterTableModel = new PTMClusterTableModel(m_ptmClusterTable, this);
        m_ptmClusterTable.setModel(new CompoundTableModel(m_ptmClusterTableModel, true));

        SelectLevelRenderer renderer = (SelectLevelRenderer) m_ptmClusterTableModel.getRenderer(0, PTMClusterTableModel.COLTYPE_PTM_CLUSTER_SELECTION_LEVEL);
        m_ptmClusterTable.addMouseListener(renderer);
        m_ptmClusterTable.addMouseMotionListener(renderer);

        // hide the id column
        m_ptmClusterTable.getColumnExt(m_ptmClusterTable.convertColumnIndexToView(PTMClusterTableModel.COLTYPE_PROTEIN_ID)).setVisible(false);
        //m_ptmProteinSiteTable.displayColumnAsPercentage(PTMProteinSiteTableModel.COLTYPE_PEPTIDE_SCORE);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_ptmClusterTable.getModel());
        m_ptmClusterTable.setRowSorter(sorter);
            
//        sorter.setComparator(PTMClusterTableModel.COLTYPE_MODIFICATION_LOC, new Comparator<String>() {
//            @Override
//            public int compare(String s1, String s2) {
//                int pos1;
//                if (s1.compareTo("N-term") == 0) {
//                    pos1 = -1;
//                } else if (s1.compareTo("C-term") == 0) {
//                    pos1 = Integer.MAX_VALUE;
//                } else {
//                    pos1 = Integer.valueOf(s1);
//                }
//                int pos2;
//                if (s2.compareTo("N-term") == 0) {
//                    pos2 = 0;
//                } else if (s2.compareTo("C-term") == 0) {
//                    pos2 = Integer.MAX_VALUE;
//                } else {
//                    pos2 = Integer.valueOf(s2);
//                }
//
//                return pos2-pos1;
//            }
// 
//                
//        });
//        
//        sorter.setComparator(PTMClusterTableModel.COLTYPE_PROTEIN_LOC, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer s1, Integer s2) {
//                return s1-s2;
//            }
// 
//                
//        });
        

        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, m_ptmClusterTable);
        
        m_scrollPane.setViewportView(m_ptmClusterTable);
        m_ptmClusterTable.setFillsViewportHeight(true);
        m_ptmClusterTable.setViewport(m_scrollPane.getViewport());

        m_countModificationTextField = new JTextField();
        m_countModificationTextField.setEditable(false);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        
        c.gridy++;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 1;
        internalPanel.add(m_countModificationTextField, c);
        
        return internalPanel;
    }                 
    
    

    
    private class PTMClusterTable extends LazyTable implements ImportTableSelectionInterface, CrossSelectionInterface, InfoInterface  {

       private boolean selectionWillBeRestored = false;
       private DisplayTablePopupMenu m_popupMenu;

        
        public PTMClusterTable() {
            super(m_scrollPane.getVerticalScrollBar() );
        }
        
//        @Override
//        public void tableChanged(TableModelEvent e) {
//            super.tableChanged(e);
//
//            if ((m_ptmClusterTable != null) && (m_countModificationTextField != null)) {
//
//                CompoundTableModel model = (CompoundTableModel) m_ptmClusterTable.getModel();
//                if (model != null) {
//
//                    // prepare a lost of current filtered ProteinPTMSite with no redundancy
//
//                    ArrayList<PTMCluster> proteinPTMCluster = new ArrayList<>();
//
//                    int nbRows = model.getRowCount(); // loop through filtered DProteinPTMSite
//                    for (int i = 0; i < nbRows; i++) {
//                        proteinPTMCluster.add((PTMCluster) model.getRowValue(PTMCluster.class, i));
//                    }
//                }
//            }
//        }
        
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
            m_dataBox.addDataChanged(DProteinMatch.class);
            m_dataBox.addDataChanged(PTMPeptideInstance.class, null);  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
            m_dataBox.propagateDataChanged();
            

        }

        public void dataUpdated(SubTask subTask, boolean finished) {
            
            LazyTable.LastAction keepLastAction = m_lastAction;
            try {

            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((PTMClusterTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
                setColumnsVisibility();
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
                if (((keepLastAction == LazyTable.LastAction.ACTION_SELECTING ) || (keepLastAction == LazyTable.LastAction.ACTION_SORTING)) && ((subTask != null) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) ) {
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
//            PTMClusterTableModel model = (PTMClusterTableModel) ((CompoundTableModel) m_ptmClusterTable.getModel()).getBaseModel();
            
            int rowCount = m_ptmClusterTableModel.getRowCount();
            for (int i=0;i<rowCount;i++) {
                Object v = m_ptmClusterTableModel.getValueAt(i, PTMClusterTableModel.COLTYPE_PROTEIN_ID);
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
                scrollToVisible(firstRow);
            }
            
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(PTMClustersPanel.this);
            m_popupMenu.addAction(new ViewColocalizedPTMClustersAction());
            return m_popupMenu;
        }


        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
//            PTMClusterTableModel model = (PTMClusterTableModel) ((CompoundTableModel) m_ptmClusterTable.getModel()).getBaseModel();
            String modifInfo = m_ptmClusterTableModel.getModificationsInfo();
            return count+((count>1) ? " PTM Clusters" : " PTM Cluster")+" \n "+modifInfo;
        }
        
    }

    private class ModifyStatusDialog extends DefaultDialog {

        private final String CMD_VALIDATED = "Validated";
        private final String CMD_INVALIDATED = "Invalidated";


        private SelectLevelRadioButtonGroup m_validRButton;
        private SelectLevelRadioButtonGroup m_invalidRButton;
        private ButtonGroup m_statusButtonGroup;
        private JFormattedTextField m_statusConfidenceLevelTF;
        private JTextField m_statusConfidenceInfoTF;
        private ArrayList<Integer> m_selectedRows = new ArrayList<>();

        public ModifyStatusDialog() {
            super(WindowManager.getDefault().getMainWindow(), Dialog.ModalityType.APPLICATION_MODAL);

            setTitle("Modify Modification Cluster Status");

            String help_text = "Change the status of selected modification Cluster. <br>" +
                    "Three status properties can be modified : <br>"
                    + "\t* the Status it-self : validated or invalidated<br> "
                    + "\t* the Status confidence : a positive number indicating the confidence you have in the current status.<br>"
                    + "\t* a free comment on current cluster status.";
            setHelpHeader("",help_text);
            setInternalComponent(createInternalPanel());
            setResizable(true);

            setButtonVisible(BUTTON_HELP, false);//use only cancel, ok button
        }

        private JPanel createInternalPanel(){
            JPanel p = new JPanel(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new java.awt.Insets(10, 10, 2, 5);
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;

            JLabel statusTextLabel = new JLabel("Modification Cluster Status:");
            p.add(statusTextLabel, c);

            c.gridy++;
            c.gridx++;
            c.fill = GridBagConstraints.NONE;
            c.insets = new java.awt.Insets(2, 2, 2, 2);
            m_validRButton = new SelectLevelRadioButtonGroup(p, c, CMD_VALIDATED,IconManager.getIcon(IconManager.IconType.VALIDATED));
            m_validRButton.getRadioButton().setActionCommand(CMD_VALIDATED);

            c.gridy++;
            c.gridx = 1;
            m_invalidRButton = new SelectLevelRadioButtonGroup(p, c, CMD_INVALIDATED,IconManager.getIcon(IconManager.IconType.INVALIDATED));
            m_invalidRButton.getRadioButton().setActionCommand(CMD_INVALIDATED);

            m_statusButtonGroup = new ButtonGroup();
            m_statusButtonGroup.add(m_validRButton.getRadioButton());
            m_statusButtonGroup.add(m_invalidRButton.getRadioButton());
            c.fill = GridBagConstraints.BOTH;
            c.gridy++;
            c.gridx = 0;
            c.weightx=0.2;
            c.insets = new java.awt.Insets(5, 10, 2, 5);
            JLabel statConfidenceLabel = new JLabel("Status Confidence level:");
            p.add(statConfidenceLabel, c);

            c.gridx++;
            c.gridwidth = 2;
            c.weightx=0.8;
            m_statusConfidenceLevelTF = new JFormattedTextField();
            m_statusConfidenceLevelTF.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));
            m_statusConfidenceLevelTF.setColumns(10);
            p.add(m_statusConfidenceLevelTF, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx=0.2;
            JLabel statConfidenceInfo = new JLabel("Status Confidence description:");
            p.add(statConfidenceInfo, c);

            c.gridx++;
            c.weightx=0.8;
            m_statusConfidenceInfoTF = new JTextField();
            p.add(m_statusConfidenceInfoTF, c);


            return p;
        }

        @Override
        protected boolean okCalled() {
            String command = m_statusButtonGroup.getSelection().getActionCommand();
            ArrayList<PTMCluster> clustersToModify = new ArrayList<>();
            m_selectedRows.forEach(r -> clustersToModify.add(m_ptmClusterTableModel.getProteinPTMCluster(r)));
            final boolean[] changedDone = new boolean[1];
            changedDone[0] = false;
            clustersToModify.forEach( cluster -> {
                if( command.equals(CMD_VALIDATED) && cluster.getSelectionLevel()<2) {
                    cluster.setSelectionLevel(3);
                    changedDone[0] = true;
                }

                if( command.equals(CMD_INVALIDATED) && cluster.getSelectionLevel()>=2) {
                  cluster.setSelectionLevel(0);
                    changedDone[0] = true;
                }

                //get confidence parameters values
                String confidenceDesc = m_statusConfidenceInfoTF.getText();
                Integer confidenceNotation = (m_statusConfidenceLevelTF.getValue() == null || (m_statusConfidenceLevelTF.getValue().toString().isEmpty()) ) ? null : new Integer(m_statusConfidenceLevelTF.getValue().toString());

                if( (confidenceDesc != null && !confidenceDesc.equals(cluster.getSelectionInfo())) || (confidenceDesc == null &&  cluster.getSelectionInfo() !=null))  {
                    cluster.setSelectionInfo(confidenceDesc);
                    changedDone[0] = true;
                }

                if( (confidenceNotation != null && !confidenceNotation.equals(cluster.getSelectionNotation())) || (confidenceNotation == null &&  cluster.getSelectionNotation() !=null)) {
                    cluster.setSelectionNotation(confidenceNotation);
                    changedDone[0] = true;
                }
            });

            if(changedDone[0])
                DataBoxViewerManager.loadedDataModified(m_dataBox.getProjectId(), m_dataBox.getRsetId(), m_dataBox.getRsmId(), PTMCluster.class, new ArrayList(clustersToModify), DataBoxViewerManager.REASON_PTMCLUSTER_MODIFIED);
            return true;
        }

        private void setSelectedRows(int[] selectedViewRows) {

            Integer commonNotation = null;
            boolean diffNotation = false;

            String commonDescription = null;
            boolean diffDescr = false;

            int commonStatus = SelectLevelEnum.RESET_AUTO.getIntValue(); // Use this value for not set yet !!
            m_selectedRows.clear();

            for (int i=0; i< selectedViewRows.length; i++) {
                int row = selectedViewRows[i];
                int modelIndex = m_ptmClusterTable.convertRowIndexToModel(row);
                PTMCluster nextCluster =m_ptmClusterTableModel.getProteinPTMCluster(modelIndex);
                if(i==0){
                    commonStatus = nextCluster.getSelectionLevel();
                    commonNotation = nextCluster.getSelectionNotation();
                    commonDescription= nextCluster.getSelectionInfo();
                } else {
                    //*** Test if same selection level
                    if (commonStatus != nextCluster.getSelectionLevel()) //  not same as previous value, set to Unknown
                        commonStatus = SelectLevelEnum.UNKNOWN.getIntValue();

                    //*** Test if same selection notation. If one is null, use other one
                    if(!diffNotation) {
                        if (commonNotation == null) // previous was nulll
                            commonNotation = nextCluster.getSelectionNotation();
                        else if (nextCluster.getSelectionNotation() != null && !commonNotation.equals(nextCluster.getSelectionNotation())) { // none null but different
                            commonNotation = null;
                            diffNotation = true;
                        }
                    }

                    //*** Test if same selection notation. If one is null, use other one
                    if(!diffDescr) {
                        if (commonDescription == null) // previous was nulll
                            commonDescription = nextCluster.getSelectionInfo();
                        else if (nextCluster.getSelectionInfo() != null && !commonDescription.equals(nextCluster.getSelectionInfo())) { // none null but different
                            commonDescription = null;
                            diffDescr = true;
                        }
                    }
                }
                m_selectedRows.add(modelIndex);
            }
            //Set selection level in Panel
            SelectLevelEnum val =SelectLevelEnum.valueOf(commonStatus);
            switch (val){
                case RESET_AUTO:
                case UNKNOWN:
                    break;
                case DESELECTED_AUTO:
                case DESELECTED_MANUAL:
                    m_invalidRButton.getRadioButton().setSelected( true);
                    break;

                case SELECTED_AUTO:
                case SELECTED_MANUAL:
                    m_validRButton.getRadioButton().setSelected( true);
                    break;
            }

            //Set description if common exist
            if(!diffDescr && commonDescription != null)
                m_statusConfidenceInfoTF.setText(commonDescription);

            //Set confidence notation if common exist
            if(!diffNotation && commonNotation != null)
                m_statusConfidenceLevelTF.setValue(commonNotation);

        }

    }

}
