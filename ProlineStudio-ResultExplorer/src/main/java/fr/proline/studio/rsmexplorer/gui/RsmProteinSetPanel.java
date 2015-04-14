package fr.proline.studio.rsmexplorer.gui;



import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.comparedata.AddCompareDataButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.BookmarkMarker;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.*;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.model.ProteinSetTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ProteinCountRenderer;
import fr.proline.studio.search.AbstractSearch;
import fr.proline.studio.search.SearchFloatingPanel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.ImportTableSelectionInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.TopComponent;

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
    
    private boolean m_firstPanel;
    private JButton m_decoyButton;
    
    private SearchFloatingPanel m_searchPanel;
    private JToggleButton m_searchToggleButton;
    private Search m_search = null;
    
    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private AddCompareDataButton m_addCompareDataButton;
    
    /**
     * Creates new form RsmProteinSetPanel
     */
    public RsmProteinSetPanel(boolean firstPanel) {
        
        m_firstPanel = firstPanel;
        
        initComponents();

    }

    public void setData(Long taskId, DProteinSet[] proteinSets, boolean finished) {
        
        // update toolbar
        if (m_firstPanel) {
            ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
            if (rsm != null) {
                m_decoyButton.setEnabled(rsm.getDecotResultSummary() != null);
            }
        }
        
        
        ((ProteinSetTableModel) ((CompoundTableModel)m_proteinSetTable.getModel()).getBaseModel()).setData(taskId, proteinSets);

        // select the first row
        if ((proteinSets != null) && (proteinSets.length > 0)) {
            m_proteinSetTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(proteinSets.length);
            
            if (!m_firstPanel) {
                m_markerContainerPanel.removeAllMarkers();
            }
        }
        
        if (finished) {
            m_proteinSetTable.setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_proteinSetTable.dataUpdated(subTask, finished);
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
        while ((c != null) && !(c instanceof TopComponent)) {
            c = c.getParent();
        }
        if ((c != null) && (c instanceof TopComponent)) {
            return ((TopComponent) c).getName();
        }
        return "";
    }

    
    
    private void initComponents() {


        setLayout(new BorderLayout());

        m_search = new Search();
        m_searchPanel = new SearchFloatingPanel(m_search);
        final JPanel proteinSetPanel = createProteinSetPanel();
        m_searchPanel.setToggleButton(m_searchToggleButton);

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
        layeredPane.add(m_searchPanel, JLayeredPane.PALETTE_LAYER);


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

                    ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
                    ResultSummary decoyRsm = rsm.getDecotResultSummary();
                    if (decoyRsm == null) {
                        return;
                    }

                    String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);
                    
                    AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(savedWindow);
                    WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, true);
                    wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);

                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();

                }
            });

            // Search Button
            m_searchToggleButton = new SearchToggleButton(m_searchPanel);

            toolbar.add(m_decoyButton);
            toolbar.add(m_searchToggleButton);
        }
        
        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_proteinSetTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_proteinSetTable.getModel()), "Protein Sets", m_proteinSetTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);

        m_addCompareDataButton = new AddCompareDataButton(((CompoundTableModel) m_proteinSetTable.getModel()), (CompareDataInterface) m_proteinSetTable.getModel()) {

            @Override
            public void actionPerformed(CompareDataInterface compareDataInterface) {
                compareDataInterface.setName(m_dataBox.getFullName());
                DataMixerWindowBoxManager.addCompareTableModel(compareDataInterface);
            }
        };
        toolbar.add(m_addCompareDataButton);
        
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
        m_proteinSetTable.getColumnExt(ProteinSetTableModel.COLTYPE_PROTEIN_SET_ID).setVisible(false);
        

        m_markerContainerPanel = new MarkerContainerPanel(m_proteinSetScrollPane, (ProteinSetTable) m_proteinSetTable);
        
        m_proteinSetScrollPane.setViewportView(m_proteinSetTable);
        m_proteinSetTable.setFillsViewportHeight(true);
        m_proteinSetTable.setViewport(m_proteinSetScrollPane.getViewport());
        
        m_proteinSetTable.displayColumnAsPercentage(ProteinSetTableModel.COLTYPE_PROTEIN_SCORE);



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

    
    private class ProteinSetTable extends LazyTable implements ImportTableSelectionInterface, CrossSelectionInterface  {

        
        public ProteinSetTable() {
            super(m_proteinSetScrollPane.getVerticalScrollBar() );
            
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            
            setDefaultRenderer(ProteinSetTableModel.ProteinCount.class, new ProteinCountRenderer());



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
 
            m_dataBox.propagateDataChanged(DProteinSet.class);

        }
        
        public boolean selectProteinSet(Long proteinSetId, String searchText) {
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
        //private String m_searchTextBeingDone = null;


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
        
        @Override
        public void sortingChanged(int col) {
            m_search.reinitSearch();
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
            TablePopupMenu popupMenu = new TablePopupMenu();

            popupMenu.addAction(new RestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.propagateDataChanged(CompareDataInterface.class);
                }
            });
            popupMenu.addAction(new ClearRestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.propagateDataChanged(CompareDataInterface.class);
                }
            });

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        
        
    }
    

    
    private class Search extends AbstractSearch {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Long> proteinSetIds = new ArrayList<>();

        @Override
        public void reinitSearch() {
            if (proteinSetIds.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((ProteinSetTableModel) ((CompoundTableModel) m_proteinSetTable.getModel()).getBaseModel()).sortAccordingToModel(proteinSetIds, (CompoundTableModel) m_proteinSetTable.getModel());
        }

        @Override
        public void doSearch(String text) {
            final String searchText = text.trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                
                int checkLoopIndex = -1;
                while (true) {
                    // search already done, display next result
                    searchIndex++;
                    if (searchIndex >= proteinSetIds.size()) {
                        searchIndex = 0;
                    }

                    if (checkLoopIndex == searchIndex) {
                        break;
                    }
                    
                    if (!proteinSetIds.isEmpty()) {
                        boolean found = ((ProteinSetTable) m_proteinSetTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
                        if (found) {
                            break;
                        }
                    } else {
                        break;
                    }
                    if (checkLoopIndex == -1) {
                        checkLoopIndex =  searchIndex;
                    }
                }
                
            } else {
                previousSearch = searchText;
                searchIndex = -1;

                // prepare callback for the search
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                        // contruct the Map of proteinSetId
                        
                        
                        if (!proteinSetIds.isEmpty()) {
                            
                            ((ProteinSetTableModel) ((CompoundTableModel) m_proteinSetTable.getModel()).getBaseModel()).sortAccordingToModel(proteinSetIds, (CompoundTableModel) m_proteinSetTable.getModel());

                             int checkLoopIndex = -1;
                             while (true) {
                                // search already done, display next result
                                searchIndex++;
                                if (searchIndex >= proteinSetIds.size()) {
                                    searchIndex = 0;
                                }

                                if (checkLoopIndex == searchIndex) {
                                    break;
                                }

                                if (!proteinSetIds.isEmpty()) {
                                    boolean found = ((ProteinSetTable) m_proteinSetTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
                                    if (found) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                                if (checkLoopIndex == -1) {
                                    checkLoopIndex = searchIndex;
                                }
                            }
                            
                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        m_searchPanel.enableSearch(true);
                    }
                };

                Long rsmId = ((ProteinSetTableModel) ((CompoundTableModel) m_proteinSetTable.getModel()).getBaseModel()).getResultSummaryId();


                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchProteinSetsTask(callback, m_dataBox.getProjectId() ,rsmId, searchText, proteinSetIds));

                m_searchPanel.enableSearch(false);
            }
        
        }
    }
    
}