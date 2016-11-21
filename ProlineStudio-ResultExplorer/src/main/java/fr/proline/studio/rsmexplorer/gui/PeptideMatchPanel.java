package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.*;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.PeptideMatchTableModel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Panel for Peptide Matches
 * @author JM235353
 */
public class PeptideMatchPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private final boolean m_forRSM;
    private final boolean m_mergedData;
    private final boolean m_startingPanel;
    private final boolean m_proteinMatchUnknown;
    private final boolean m_isDecoyAndValidated; // to display columns isDecoy and isValidated (in case of rsm)
    
    private PeptideMatchTable m_peptideMatchTable;
    private JScrollPane m_scrollPane;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
    private JButton m_decoyButton;

    private SearchToggleButton m_searchToggleButton;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    private AddDataAnalyzerButton m_addCompareDataButton;

    public PeptideMatchPanel(boolean forRSM, boolean mergedData, boolean startingPanel, boolean proteinMatchUnknown, boolean isDecoyAndValidated) {
        m_forRSM = forRSM;
        m_mergedData = mergedData;
        m_startingPanel = startingPanel;
        m_proteinMatchUnknown = proteinMatchUnknown;
        m_isDecoyAndValidated = isDecoyAndValidated;
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
    public void addSingleValue(Object v) {
        getGlobalTableModelInterface().addSingleValue(v);
    }

    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_peptideMatchTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_peptideMatchTable;
    }

    public void setData(long taskId, DPeptideMatch[] peptideMatches, long[] peptideMatchesId, boolean finished) {
        
        // update toolbar
        if (m_startingPanel) {
            if (m_forRSM) {
                ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
                if (rsm != null) {
                    m_decoyButton.setEnabled(rsm.getDecoyResultSummary() != null);
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

        final JPanel peptideMatch = createPeptideMatchPanel();

        
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
        layeredPane.add(m_searchToggleButton.getSearchPanel(), JLayeredPane.PALETTE_LAYER);

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
                        ResultSummary decoyRsm = rsm.getDecoyResultSummary();
                        if (decoyRsm == null) {
                            return;
                        }
                        String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);

                        AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(savedWindow);
                        wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, false, WindowSavedManager.SAVE_WINDOW_FOR_RSM);
                        wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);


                    } else {
                        ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
                        ResultSet decoyRset = rset.getDecoyResultSet();
                        if (decoyRset == null) {
                            return;
                        }
                        String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);

                        AbstractDataBox[] databoxes = WindowSavedManager. readBoxes(savedWindow);
                        wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, false, WindowSavedManager.SAVE_WINDOW_FOR_RSET);
                        wbox.setEntryData(m_dataBox.getProjectId(), decoyRset);
                    }

                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
                }
            });
        }

        m_settingsButton = new SettingsButton(((ProgressInterface) m_peptideMatchTable.getModel()), m_peptideMatchTable);
        
        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_peptideMatchTable, m_peptideMatchTable, ((CompoundTableModel) m_peptideMatchTable.getModel()));
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_peptideMatchTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };
        
        m_exportButton = new ExportButton((CompoundTableModel) m_peptideMatchTable.getModel(), "Peptide Match", m_peptideMatchTable);
        
              
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_peptideMatchTable.getModel())) {
           
            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i!=null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
               
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
                    WindowBox wbox = WindowBoxFactory.getGraphicsWindowBox("Graphic", m_dataBox, true);

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
        toolbar.add(m_settingsButton);
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
        PeptideMatchTableModel peptideMatchTableModel = new PeptideMatchTableModel((LazyTable)m_peptideMatchTable, m_forRSM, !(m_startingPanel || m_proteinMatchUnknown), m_isDecoyAndValidated, m_mergedData);
        CompoundTableModel compoundTableModel = new CompoundTableModel(peptideMatchTableModel, true);
        m_peptideMatchTable.setModel(compoundTableModel);
        //m_peptideMatchTable.displayColumnAsPercentage(peptideMatchTableModel.convertColToColUsed(PeptideMatchTableModel.COLTYPE_PEPTIDE_SCORE));

        m_peptideMatchTable.getColumnExt(m_peptideMatchTable.convertColumnIndexToView(PeptideMatchTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        
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
            m_popupMenu = new DisplayTablePopupMenu(PeptideMatchPanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
    }
}
