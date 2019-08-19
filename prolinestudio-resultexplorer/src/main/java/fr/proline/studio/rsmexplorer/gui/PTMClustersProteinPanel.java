package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
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
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.ImportTableSelectionInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinPTMClusterTableModel;
import java.util.List;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author JM235353
 */
public class PTMClustersProteinPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {
    
    private AbstractDataBox m_dataBox;
    
    private JScrollPane m_scrollPane;
    private PTMClusterProteinTable m_ptmClusterProteinTable;
    
    private JTextField m_countModificationTextField;

    private MarkerContainerPanel m_markerContainerPanel;

    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    
    /**
     * Creates new form PTMProteinSitePanel
     */
    public PTMClustersProteinPanel() {

        initComponents();

    }

    
    private boolean m_hideFirstTime = true;
    public void setData(Long taskId, ArrayList<PTMCluster> ptmClusters, boolean finished) {


        ProteinPTMClusterTableModel model = ((ProteinPTMClusterTableModel) ((CompoundTableModel)m_ptmClusterProteinTable.getModel()).getBaseModel());        
        model.setData(taskId,ptmClusters);

        // select the first row
        if ((ptmClusters != null) && (ptmClusters.size() > 0)) {
            m_ptmClusterProteinTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(ptmClusters.size());
            if (m_hideFirstTime) {
                setColumnsVisibility();
                m_hideFirstTime = false;
            }

        }
        
        m_infoToggleButton.updateInfo();
        
        m_countModificationTextField.setText(model.getModificationsInfo());
        
        
        if (finished) {
            m_ptmClusterProteinTable.setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_ptmClusterProteinTable.dataUpdated(subTask, finished);
        ProteinPTMClusterTableModel model = ((ProteinPTMClusterTableModel) ((CompoundTableModel)m_ptmClusterProteinTable.getModel()).getBaseModel());        
        m_countModificationTextField.setText(model.getModificationsInfo());
    }


    
    public PTMCluster getSelectedProteinPTMCluster() {

        // Retrieve Selected Row
        int selectedRow = m_ptmClusterProteinTable.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = m_ptmClusterProteinTable.convertRowIndexToModel(selectedRow);

        CompoundTableModel compoundTableModel = ((CompoundTableModel)m_ptmClusterProteinTable.getModel());
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

        // Retrieve ProteinPTMSite selected
        ProteinPTMClusterTableModel tableModel = (ProteinPTMClusterTableModel) compoundTableModel.getBaseModel();    
        return tableModel.getProteinPTMCluster(selectedRow);
    }
    
     public List<PTMCluster> getSelectedProteinPTMClusters() {

        // Retrieve Selected Row
        int[] selectedRows = m_ptmClusterProteinTable.getSelectedRows();
        int nbSelectedRows = selectedRows.length;
        List<PTMCluster> selectedPtmCluser= new ArrayList<>(nbSelectedRows);
        // nothing selected
        if (nbSelectedRows == 0) {
            return selectedPtmCluser;
        }
       
        // convert according to the sorting
        CompoundTableModel compoundTableModel = ((CompoundTableModel)m_ptmClusterProteinTable.getModel());        
        ProteinPTMClusterTableModel tableModel = (ProteinPTMClusterTableModel) compoundTableModel.getBaseModel();           
        for(int i=0; i<nbSelectedRows; i++){
            int rowModelIndex  = m_ptmClusterProteinTable.convertRowIndexToModel(selectedRows[i]);            
            int convertedSelectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(rowModelIndex);
            
            // Retrieve ProteinPTMSite selected
            selectedPtmCluser.add(tableModel.getProteinPTMCluster(convertedSelectedRow));
        }
        
        return selectedPtmCluser;
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
        return (GlobalTableModelInterface) m_ptmClusterProteinTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_ptmClusterProteinTable;
    }
    
    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_ptmClusterProteinTable;
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
        List<Integer> listIdsToHide = ((ProteinPTMClusterTableModel) ((CompoundTableModel) m_ptmClusterProteinTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
        List<TableColumn> columns = m_ptmClusterProteinTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_ptmClusterProteinTable.getColumnExt(m_ptmClusterProteinTable.convertColumnIndexToView(id)).setVisible(false);
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

        m_settingsButton = new SettingsButton(((ProgressInterface) m_ptmClusterProteinTable.getModel()), m_ptmClusterProteinTable);
        
        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_ptmClusterProteinTable, m_ptmClusterProteinTable, ((CompoundTableModel) m_ptmClusterProteinTable.getModel()));
        toolbar.add(m_searchToggleButton);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_ptmClusterProteinTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);
                m_infoToggleButton.updateInfo();
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_ptmClusterProteinTable.getModel()), "Protein Sets", m_ptmClusterProteinTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_ptmClusterProteinTable.getModel())) {
           
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
        
        m_infoToggleButton = new InfoToggleButton(m_ptmClusterProteinTable, m_ptmClusterProteinTable);
        
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
        
        m_ptmClusterProteinTable = new PTMClusterProteinTable();
        ProteinPTMClusterTableModel model = new ProteinPTMClusterTableModel((LazyTable)m_ptmClusterProteinTable);
        m_ptmClusterProteinTable.setModel(new CompoundTableModel(model, true));
        
        // hide the id column
        m_ptmClusterProteinTable.getColumnExt(m_ptmClusterProteinTable.convertColumnIndexToView(ProteinPTMClusterTableModel.COLTYPE_PROTEIN_ID)).setVisible(false);
        //m_ptmProteinSiteTable.displayColumnAsPercentage(PTMProteinSiteTableModel.COLTYPE_PEPTIDE_SCORE);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_ptmClusterProteinTable.getModel());
        m_ptmClusterProteinTable.setRowSorter(sorter);
            
//        sorter.setComparator(ProteinPTMClusterTableModel.COLTYPE_MODIFICATION_LOC, new Comparator<String>() {
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
//        sorter.setComparator(ProteinPTMClusterTableModel.COLTYPE_PROTEIN_LOC, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer s1, Integer s2) {
//                return s1-s2;
//            }
// 
//                
//        });
        

        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, (PTMClusterProteinTable) m_ptmClusterProteinTable);
        
        m_scrollPane.setViewportView(m_ptmClusterProteinTable);
        m_ptmClusterProteinTable.setFillsViewportHeight(true);
        m_ptmClusterProteinTable.setViewport(m_scrollPane.getViewport());

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
    
    

    
    private class PTMClusterProteinTable extends LazyTable implements ImportTableSelectionInterface, CrossSelectionInterface, InfoInterface  {

        
        public PTMClusterProteinTable() {
            super(m_scrollPane.getVerticalScrollBar() );

        }
        
        @Override
        public void tableChanged(TableModelEvent e) {
            super.tableChanged(e);
           
            if ((m_ptmClusterProteinTable != null) && (m_countModificationTextField != null)) {

                CompoundTableModel model = (CompoundTableModel)m_ptmClusterProteinTable.getModel();
                if (model != null) {

                    // prepare a lost of current filtered ProteinPTMSite 
                    // with no redundancy

                    ArrayList<PTMCluster> proteinPTMCluster = new ArrayList<>();

                    int nbRows = model.getRowCount(); // loop through filtered DProteinPTMSite
                    for (int i = 0; i < nbRows; i++) {
                        proteinPTMCluster.add((PTMCluster) model.getRowValue(PTMCluster.class, i));
                    }  

//                    String modification =  ProteinPTMSiteTableModelProcessing.calculateDataWORedundance(model, new ArrayList<>(), new ArrayList<>(), new HashMap<>(), proteinPTMCluster,  new HashMap<>());        
//                    m_countModificationTextField.setText(modification); 
                }

            }

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
            //VDS: Order is important ! To be corrected using set of class for propagation
            //m_dataBox.propagateDataChanged(PTMSite.class);
            //m_dataBox.propagateDataChanged(DPeptideMatch.class);
            m_dataBox.propagateDataChanged(DProteinSet.class);
            m_dataBox.propagateDataChanged(DProteinMatch.class);
            m_dataBox.propagateDataChanged(PTMPeptideInstance.class);

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
                ((ProteinPTMClusterTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
                if (((keepLastAction == LazyTable.LastAction.ACTION_SELECTING ) || (keepLastAction == LazyTable.LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
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
            ProteinPTMClusterTableModel model = (ProteinPTMClusterTableModel) ((CompoundTableModel) m_ptmClusterProteinTable.getModel()).getBaseModel();
            
            int rowCount = model.getRowCount();
            for (int i=0;i<rowCount;i++) {
                Object v = model.getValueAt(i, ProteinPTMClusterTableModel.COLTYPE_PROTEIN_ID);
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
            m_popupMenu = new DisplayTablePopupMenu(PTMClustersProteinPanel.this);

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
            ProteinPTMClusterTableModel model = (ProteinPTMClusterTableModel) ((CompoundTableModel) m_ptmClusterProteinTable.getModel()).getBaseModel();            
            String modifInfo = model.getModificationsInfo();
            return count+((count>1) ? " PTM Clusters" : " PTM Cluster")+" \n "+modifInfo;
        }

        
        
    }
    

    
}
