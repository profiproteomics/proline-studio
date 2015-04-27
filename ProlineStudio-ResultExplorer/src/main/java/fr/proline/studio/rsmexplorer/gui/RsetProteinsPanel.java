package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.comparedata.AddDataMixerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinsOfPeptideMatchTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.*;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.search.SearchFloatingPanel;
import fr.proline.studio.search.AbstractSearch;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.TopComponent;

/**
 * Panel for Protein Matches
 *
 * @author JM235353
 */
public class RsetProteinsPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    private long m_peptideMatchCurId = -1;
    private JScrollPane m_proteinScrollPane;
    private ProteinTable m_proteinTable;
    private MarkerContainerPanel m_markerContainerPanel;
    private JButton m_decoyButton;
    private final boolean m_startingPanel;
    
    private final SearchFloatingPanel m_searchPanel;
    private JToggleButton m_searchToggleButton;

    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private AddDataMixerButton m_addCompareDataButton;

    public RsetProteinsPanel(boolean startingPanel) {

        m_startingPanel = startingPanel;

        
        setLayout(new BorderLayout());

        
        m_searchPanel = new SearchFloatingPanel(new Search());
        
        final JPanel proteinPanel = createProteinPanel();
        m_searchPanel.setToggleButton(m_searchToggleButton);

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                proteinPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(proteinPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_searchPanel, JLayeredPane.PALETTE_LAYER);


    }

    private JPanel createProteinPanel() {
        JPanel proteinPanel = new JPanel();
        proteinPanel.setBounds(0, 0, 500, 400);

        proteinPanel.setLayout(new BorderLayout());

        JPanel internalPanel = initComponents();
        proteinPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        proteinPanel.add(toolbar, BorderLayout.WEST);


        return proteinPanel;

    }


    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Decoy Button
        if (m_startingPanel) {
            IconManager.IconType iconType = IconManager.IconType.DATASET_RSET_DECOY;
            m_decoyButton = new JButton(IconManager.getIcon(iconType));
            m_decoyButton.setToolTipText("Display Decoy Data");
            m_decoyButton.setEnabled(false);

            m_decoyButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {


                    ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
                    ResultSet decoyRset = rset.getDecoyResultSet();
                    if (decoyRset == null) {
                        return;
                    }

                    String windowSaved = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);

                    AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(windowSaved);
                    WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, false);
                    wbox.setEntryData(m_dataBox.getProjectId(), decoyRset);

                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
                }
            });
        }

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_searchPanel);
       
        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_proteinTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };
        m_exportButton = new ExportButton(((CompoundTableModel) m_proteinTable.getModel()), "Peptide Match", m_proteinTable);

        m_addCompareDataButton = new AddDataMixerButton(((CompoundTableModel) m_proteinTable.getModel())) {
           
            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                String name = ((JPanel)m_dataBox.getPanel()).getName();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), name, table);
                tableInfo.setIcon(new ImageIcon(m_dataBox.getIcon()));
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
            }
        };
        
        
        if (m_startingPanel) {
            toolbar.add(m_decoyButton);
        }
        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        
        return toolbar;
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

    public DProteinMatch getSelectedProteinMatch() {


        // Retrieve Selected Row
        int selectedRow = m_proteinTable.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        CompoundTableModel compoundTableModel = (CompoundTableModel) m_proteinTable.getModel();

        // convert according to the sorting
        selectedRow = m_proteinTable.convertRowIndexToModel(selectedRow);
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);


        // Retrieve ProteinSet selected
        ProteinsOfPeptideMatchTableModel tableModel = (ProteinsOfPeptideMatchTableModel) compoundTableModel.getBaseModel();

        return tableModel.getProteinMatch(selectedRow);
    }

    public void setDataProteinMatchArray(DProteinMatch[] proteinMatchArray, boolean finished) {
        // Modify the Model
        ((ProteinsOfPeptideMatchTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).setData(proteinMatchArray);

        // Select the first row
        if (proteinMatchArray.length>0) {
            m_proteinTable.getSelectionModel().setSelectionInterval(0, 0);
        }

        if (proteinMatchArray != null) {
            m_markerContainerPanel.setMaxLineNumber(proteinMatchArray.length);
            
            if (!m_startingPanel) {
                m_markerContainerPanel.removeAllMarkers();
            }
        }

        ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
        if ((m_decoyButton != null) && (rset != null)) {
            m_decoyButton.setEnabled(rset.getDecoyResultSet() != null);
        }
        
        if (finished) {
            m_proteinTable.setSortable(true);
        }
    }

    public void setDataPeptideMatch(DPeptideMatch peptideMatch) {

        if (peptideMatch == null) {
            clearData();
            m_peptideMatchCurId = -1;
            return;
        }

        if ((m_peptideMatchCurId != -1) && (peptideMatch.getId() == m_peptideMatchCurId)) {
            return;
        }

        m_peptideMatchCurId = peptideMatch.getId();



        DProteinMatch[] proteinMatchArray = peptideMatch.getProteinMatches();



        // Modify the Model
        ((ProteinsOfPeptideMatchTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).setData(proteinMatchArray);

        // Select the first row
        m_proteinTable.getSelectionModel().setSelectionInterval(0, 0);

        if (proteinMatchArray != null) {
            m_markerContainerPanel.setMaxLineNumber(proteinMatchArray.length);
            
            if (!m_startingPanel) {
                m_markerContainerPanel.removeAllMarkers();
            }
        }
        
        m_proteinTable.setSortable(true);

    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        ((ProteinTable) m_proteinTable).dataUpdated(subTask, finished);
    }

    private void clearData() {
        ((ProteinsOfPeptideMatchTableModel) (((CompoundTableModel) m_proteinTable.getModel()).getBaseModel())).setData(null);

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
        return (GlobalTableModelInterface) m_proteinTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_proteinTable;
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

    private JPanel initComponents() {


        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // create objects
        m_proteinScrollPane = new JScrollPane();

        m_proteinTable = new ProteinTable();
        m_proteinTable.setModel(new CompoundTableModel(new ProteinsOfPeptideMatchTableModel(m_proteinTable), true));
        
        
        
        TableColumn accColumn = m_proteinTable.getColumnModel().getColumn(ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        m_proteinTable.addMouseListener(renderer);
        m_proteinTable.addMouseMotionListener(renderer);

        // hide the id column (must be done after the URLCellRenderer is set)
        m_proteinTable.getColumnExt(ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_ID).setVisible(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_proteinScrollPane, (ProteinTable) m_proteinTable);

        m_proteinScrollPane.setViewportView(m_proteinTable);
        m_proteinTable.setFillsViewportHeight(true);
        m_proteinTable.setViewport(m_proteinScrollPane.getViewport());

        /*
         * if (m_startingPanel) { m_searchButton = new SearchButton();
         *
         *
         * m_searchTextField = new JTextField(16) {
         *
         * @Override public Dimension getMinimumSize() { return
         * super.getPreferredSize(); } }; }
         */

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);


        return internalPanel;

    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_proteinTable;
    }

    private class ProteinTable extends LazyTable {

        public ProteinTable() {
            super(m_proteinScrollPane.getVerticalScrollBar());



            displayColumnAsPercentage(ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_SCORE);
            setDefaultRenderer(Float.class, new FloatRenderer(new DefaultRightAlignRenderer(getDefaultRenderer(String.class))));
            setDefaultRenderer(Double.class, new DoubleRenderer(new DefaultRightAlignRenderer(getDefaultRenderer(String.class))));
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

            if (selectionWillBeRestored) {
                return;
            }

            m_dataBox.propagateDataChanged(DProteinMatch.class);

        }

        public void dataUpdated(SubTask subTask, boolean finished) {

            LastAction keepLastAction = m_lastAction;
            try {


                // retrieve selected row
                int rowSelected = getSelectionModel().getMinSelectionIndex();
                int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

                // Update Model (but protein table must not react to the model update)

                selectionWillBeRestored(true);
                try {
                    ((ProteinsOfPeptideMatchTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
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

        public boolean selectProteinMatch(Long proteinMatchId, String searchText) {

            ProteinsOfPeptideMatchTableModel tableModel = (ProteinsOfPeptideMatchTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(proteinMatchId);
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
        ArrayList<Long> proteinMatchIds = new ArrayList<>();

        @Override
        public void reinitSearch() {
            if (proteinMatchIds.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((ProteinsOfPeptideMatchTableModel) ((CompoundTableModel) m_proteinTable.getModel()).getBaseModel()).sortAccordingToModel(proteinMatchIds, (CompoundTableModel) m_proteinTable.getModel());
        }

        @Override
        public void doSearch(String text) {
            final String searchText = text.trim().toUpperCase();

            
             if (searchText.compareTo(previousSearch) == 0) {
                
                int checkLoopIndex = -1;
                while (true) {
                    // search already done, display next result
                    searchIndex++;
                    if (searchIndex >= proteinMatchIds.size()) {
                        searchIndex = 0;
                    }

                    if (checkLoopIndex == searchIndex) {
                        break;
                    }
                    
                    if (!proteinMatchIds.isEmpty()) {
                        boolean found = ((ProteinTable) m_proteinTable).selectProteinMatch(proteinMatchIds.get(searchIndex), searchText);
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

                        // contruct the Map of proteinMatchId
                        if (!proteinMatchIds.isEmpty()) {

                            ((ProteinsOfPeptideMatchTableModel) ((CompoundTableModel) m_proteinTable.getModel()).getBaseModel()).sortAccordingToModel(proteinMatchIds, (CompoundTableModel) m_proteinTable.getModel());

                            
                                                      int checkLoopIndex = -1;
                             while (true) {
                                // search already done, display next result
                                searchIndex++;
                                if (searchIndex >= proteinMatchIds.size()) {
                                    searchIndex = 0;
                                }

                                if (checkLoopIndex == searchIndex) {
                                    break;
                                }

                                if (!proteinMatchIds.isEmpty()) {
                                    boolean found = ((ProteinTable) m_proteinTable).selectProteinMatch(proteinMatchIds.get(searchIndex), searchText);
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


                        m_searchPanel.enableSearch(true);
                    }
                };

                ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);


                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchProteinMatchTask(callback, m_dataBox.getProjectId(), rset, searchText, proteinMatchIds));

                m_searchPanel.enableSearch(false);

            }
        }
    }
}
