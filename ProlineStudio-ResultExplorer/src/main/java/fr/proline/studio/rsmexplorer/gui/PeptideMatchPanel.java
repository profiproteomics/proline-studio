package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.AddCompareDataButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseSearchPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.*;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.model.PeptideMatchTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.search.AbstractSearch;
import fr.proline.studio.search.SearchFloatingPanel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableCellRenderer;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Panel for Peptide Matches
 * @author JM235353
 */
public class PeptideMatchPanel extends HourglassPanel implements DataBoxPanelInterface, CompareDataProviderInterface {

    private AbstractDataBox m_dataBox;

    private boolean m_forRSM;
    private boolean m_startingPanel;
    private boolean m_proteinMatchUnknown;
    
    private PeptideMatchTable m_peptideMatchTable;
    private JScrollPane m_scrollPane;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
    private JButton m_decoyButton;

    private SearchFloatingPanel m_searchPanel;
    private JToggleButton m_searchToggleButton;
    
    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    private AddCompareDataButton m_addCompareDataButton;

    public PeptideMatchPanel(boolean forRSM, boolean startingPanel, boolean proteinMatchUnknown) {
        m_forRSM = forRSM;
        m_startingPanel = startingPanel;
        m_proteinMatchUnknown = proteinMatchUnknown;
        initComponents();

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
    public CompareDataInterface getCompareDataInterface() {
        return (CompareDataInterface) m_peptideMatchTable.getModel();
    }

    public void setData(long taskId, DPeptideMatch[] peptideMatches, long[] peptideMatchesId, boolean finished) {
        
        // update toolbar
        if (m_startingPanel) {
            if (m_forRSM) {
                ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
                if (rsm != null) {
                    m_decoyButton.setEnabled(rsm.getDecotResultSummary() != null);
                }
            } else {
                ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
                if (rset != null) {
                    m_decoyButton.setEnabled(rset.getDecoyResultSet() != null);
                }
            }
        }

        ((PeptideMatchTableModel) ((CompoundTableModel)m_peptideMatchTable.getModel()).getBaseModel()).setData(taskId, peptideMatches, peptideMatchesId);

        // select the first row
        if ((peptideMatches != null) && (peptideMatches.length > 0)) {
            m_peptideMatchTable.getSelectionModel().setSelectionInterval(0, 0);
            
            m_markerContainerPanel.setMaxLineNumber(peptideMatches.length);
            if (!m_startingPanel) {
                m_markerContainerPanel.removeAllMarkers();
            }
        }
        
        if (finished) {
            ((PeptideMatchTable)m_peptideMatchTable).setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {

        ((PeptideMatchTable) m_peptideMatchTable).dataUpdated(subTask, finished);


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
    
    public DPeptideMatch getSelectedPeptideMatch() {

        PeptideMatchTable table = ((PeptideMatchTable) m_peptideMatchTable);

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);

        CompoundTableModel compoundTableModel = ((CompoundTableModel)table.getModel());
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);


        // Retrieve ProteinSet selected
        PeptideMatchTableModel tableModel = (PeptideMatchTableModel) compoundTableModel.getBaseModel();
        return tableModel.getPeptideMatch(selectedRow);
    }

    private void initComponents() {
        
        setLayout(new BorderLayout());

        m_searchPanel = new SearchFloatingPanel(new Search());
        final JPanel peptideMatch = createPeptideMatchPanel();
        m_searchPanel.setToggleButton(m_searchToggleButton);
        
        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptideMatch.setBounds(0, 0, c.getWidth(), c.getHeight());
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
        
        layeredPane.add(peptideMatch, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_searchPanel, JLayeredPane.PALETTE_LAYER);

    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        if (m_startingPanel) {
            IconManager.IconType iconType = (m_forRSM) ? IconManager.IconType.DATASET_RSM_DECOY : IconManager.IconType.DATASET_RSET_DECOY;
            m_decoyButton = new JButton(IconManager.getIcon(iconType));
            m_decoyButton.setToolTipText("Display Decoy Data");
            m_decoyButton.setEnabled(false);



            m_decoyButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    WindowBox wbox;
                    if (m_forRSM) {
                        ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
                        ResultSummary decoyRsm = rsm.getDecotResultSummary();
                        if (decoyRsm == null) {
                            return;
                        }
                        String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);

                        AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(savedWindow);
                        wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, true);
                        wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);


                    } else {
                        ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
                        ResultSet decoyRset = rset.getDecoyResultSet();
                        if (decoyRset == null) {
                            return;
                        }
                        String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);

                        AbstractDataBox[] databoxes = WindowSavedManager. readBoxes(savedWindow);
                        wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, false);
                        wbox.setEntryData(m_dataBox.getProjectId(), decoyRset);
                    }

                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
                }
            });
        }

        
        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_searchPanel);
        
        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_peptideMatchTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };
        
        m_exportButton = new ExportButton((CompoundTableModel) m_peptideMatchTable.getModel(), "Peptide Match", m_peptideMatchTable);
        
              
        m_addCompareDataButton = new AddCompareDataButton(((CompoundTableModel) m_peptideMatchTable.getModel()), (CompareDataInterface) m_peptideMatchTable.getModel()) {

            @Override
            public void actionPerformed(CompareDataInterface compareDataInterface) {
                compareDataInterface.setName(m_dataBox.getFullName());
                DataMixerWindowBoxManager.addCompareTableModel(compareDataInterface);
            }
        };
        
        if (m_startingPanel) {
            m_graphicsButton = new JButton(IconManager.getIcon(IconManager.IconType.CHART));
            m_graphicsButton.setToolTipText("Graphics : Histogram / Scatter Plot");
            m_graphicsButton.setFocusPainted(false);
            m_graphicsButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!((CompoundTableModel) m_peptideMatchTable.getModel()).isLoaded()) {

                        ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), ((CompoundTableModel) m_peptideMatchTable.getModel()), "Data loading", "Histogram functionnality is not available while data is loading. Please Wait.");
                        dialog.setLocation(getLocationOnScreen().x + m_graphicsButton.getWidth() + 5, m_graphicsButton.getLocationOnScreen().y + getHeight() + 5);
                        dialog.setVisible(true);

                        if (!dialog.isWaitingFinished()) {
                            return;
                        }
                    }
                    // prepare window box
                    WindowBox wbox = WindowBoxFactory.getGraphicsWindowBox("Graphic", m_dataBox);

                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
                }
            });
        }
        
        
        if (m_startingPanel) {
            toolbar.add(m_decoyButton);
        }
        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        
        if (m_startingPanel) {
            toolbar.add(m_graphicsButton);
        }
        

        
        return toolbar;
    }
             
    
    private JPanel createPeptideMatchPanel() {
        JPanel peptideMatchPanel = new JPanel();
        peptideMatchPanel.setBounds(0, 0, 500, 400);

        peptideMatchPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        peptideMatchPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        peptideMatchPanel.add(toolbar, BorderLayout.WEST);


        return peptideMatchPanel;

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
        m_peptideMatchTable = new PeptideMatchTable();
        PeptideMatchTableModel peptideMatchTableModel = new PeptideMatchTableModel((LazyTable)m_peptideMatchTable, m_forRSM, !(m_startingPanel || m_proteinMatchUnknown));
        CompoundTableModel compoundTableModel = new CompoundTableModel(peptideMatchTableModel, true);
        m_peptideMatchTable.setModel(compoundTableModel);
        m_peptideMatchTable.setTableRenderer();
        m_peptideMatchTable.displayColumnAsPercentage(peptideMatchTableModel.convertColToColUsed(PeptideMatchTableModel.COLTYPE_PEPTIDE_SCORE));

        m_peptideMatchTable.getColumnExt(PeptideMatchTableModel.COLTYPE_PEPTIDE_ID).setVisible(false);
        
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, m_peptideMatchTable);
        
        m_scrollPane.setViewportView(m_peptideMatchTable);
	m_peptideMatchTable.setFillsViewportHeight(true);
	m_peptideMatchTable.setViewport(m_scrollPane.getViewport());
        

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        

        return internalPanel;
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

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peptideMatchTable;
   }

    private class Search extends AbstractSearch {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Long> peptideMatchIds = new ArrayList<>();

        @Override
        public void reinitSearch() {
            if (peptideMatchIds.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((PeptideMatchTableModel) ((CompoundTableModel) m_peptideMatchTable.getModel()).getBaseModel()).sortAccordingToModel(peptideMatchIds, (CompoundTableModel) m_peptideMatchTable.getModel());
        }

        @Override
        public void doSearch(String text) {
            final String searchText = text.trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                
                int checkLoopIndex = -1;
                while (true) {
                    // search already done, display next result
                    searchIndex++;
                    if (searchIndex >= peptideMatchIds.size()) {
                        searchIndex = 0;
                    }

                    if (checkLoopIndex == searchIndex) {
                        break;
                    }
                    
                    if (!peptideMatchIds.isEmpty()) {
                        boolean found = ((PeptideMatchTable) m_peptideMatchTable).selectPeptideMatch(peptideMatchIds.get(searchIndex), searchText);
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
                searchIndex = 0;

                // prepare callback for the search
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                        // contruct the Map of proteinSetId


                        if (!peptideMatchIds.isEmpty()) {

                            ((PeptideMatchTableModel) ((CompoundTableModel) m_peptideMatchTable.getModel()).getBaseModel()).sortAccordingToModel(peptideMatchIds, (CompoundTableModel) m_peptideMatchTable.getModel());

                             int checkLoopIndex = -1;
                             while (true) {
                                // search already done, display next result
                                searchIndex++;
                                if (searchIndex >= peptideMatchIds.size()) {
                                    searchIndex = 0;
                                }

                                if (checkLoopIndex == searchIndex) {
                                    break;
                                }

                                if (!peptideMatchIds.isEmpty()) {
                                    boolean found = ((PeptideMatchTable) m_peptideMatchTable).selectPeptideMatch(peptideMatchIds.get(searchIndex), searchText);
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

                long rsetId = ((PeptideMatchTableModel) ((CompoundTableModel) m_peptideMatchTable.getModel()).getBaseModel()).getResultSetId();

                
                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchPeptideMatchTask(callback, m_dataBox.getProjectId(), rsetId, searchText, peptideMatchIds));

                m_searchPanel.enableSearch(false);
            }
        }
    }
    
    
 

    private class PeptideMatchTable extends LazyTable {

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        //ProteinSet proteinSetSelected = null;
        public PeptideMatchTable() {
            super(m_scrollPane.getVerticalScrollBar());

            
        }

        public void setTableRenderer(){
            PeptideMatchTableModel tableModel = ((PeptideMatchTableModel) ((CompoundTableModel)getModel()).getBaseModel());
            getColumnModel().getColumn(tableModel.convertColToColUsed(convertColumnIndexToView(PeptideMatchTableModel.COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ))).setCellRenderer(new LazyTableCellRenderer(new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)),4 )) );
            getColumnModel().getColumn(tableModel.convertColToColUsed(convertColumnIndexToView(PeptideMatchTableModel.COLTYPE_PEPTIDE_CALCULATED_MASS))).setCellRenderer(new LazyTableCellRenderer(new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)),4 )) );
            setDefaultRenderer(DPeptideMatch.class, new PeptideRenderer());
            setDefaultRenderer(DMsQuery.class, new MsQueryRenderer());
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            setDefaultRenderer(Integer.class, new DefaultRightAlignRenderer(getDefaultRenderer(Integer.class))  );
        }

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            if (selectionWillBeRestored) {
                return;
            }



            m_dataBox.propagateDataChanged(DPeptideMatch.class);

        }

        public boolean selectPeptideMatch(Long proteinSetId, String searchText) {

            PeptideMatchTableModel tableModel = (PeptideMatchTableModel) ((CompoundTableModel)getModel()).getBaseModel();
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
            searchTextBeingDone = searchText;
            
            // must convert row index if there is a sorting
            row = convertRowIndexToView(row);
            
            // select the row
            getSelectionModel().setSelectionInterval(row, row);
            
            // scroll to the row
            scrollRowToVisible(row);

            searchTextBeingDone = null;
            
            return true;
            
        }
        String searchTextBeingDone = null;
        
        
        
        public void dataUpdated(SubTask subTask, boolean finished) {

            LastAction keepLastAction = m_lastAction;
            try {


                // retrieve selected row
                int rowSelected = getSelectionModel().getMinSelectionIndex();
                int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

                // Update Model (but protein set table must not react to the model update)

                selectionWillBeRestored(true);
                try {
                    ((PeptideMatchTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
                } finally {
                    selectionWillBeRestored(false);
                }



                // restore selected row
                if (rowSelectedInModel != -1) {
                    int rowSelectedInView = convertRowIndexToView(rowSelectedInModel);
                    setSelection(rowSelectedInView);

                    // if the subtask correspond to the loading of the data of the sorted column,
                    // we keep the row selected visible
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
                        ((PeptideMatchTable) m_peptideMatchTable).scrollRowToVisible(rowSelectedInView);
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
            //((SearchButton)searchButton).sortingChanged();
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
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();

            popupMenu.addAction(new RestrainAction());
            popupMenu.addAction(new ClearRestrainAction());

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
    }
}
