package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseSearchPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.model.PeptideMatchTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.LazyTable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

/**
 * Panel for Peptide Matches
 * @author JM235353
 */
public class PeptideMatchPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    private boolean m_forRSM;
    private boolean m_startingPanel;
    
    private PeptideMatchTable m_peptideMatchTable;
    private JScrollPane m_scrollPane;
    private JButton m_searchButton;
    private JTextField m_searchTextField;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
    private JButton m_decoyButton;


    public PeptideMatchPanel(boolean forRSM, boolean startingPanel) {
        m_forRSM = forRSM;
        m_startingPanel = startingPanel;
        initComponents();

    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
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
        
        ((PeptideMatchTableModel) m_peptideMatchTable.getModel()).setData(taskId, peptideMatches, peptideMatchesId);

        // select the first row
        if ((peptideMatches != null) && (peptideMatches.length > 0)) {
            m_peptideMatchTable.getSelectionModel().setSelectionInterval(0, 0);
            
            m_markerContainerPanel.setMaxLineNumber(peptideMatches.length);
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



        // Retrieve ProteinSet selected
        PeptideMatchTableModel tableModel = (PeptideMatchTableModel) table.getModel();
        return tableModel.getPeptideMatch(selectedRow);
    }

    private void initComponents() {
        
        setLayout(new BorderLayout());
        
        if (m_startingPanel) {
            JToolBar toolbar = initToolbar();
            add(toolbar, BorderLayout.WEST);
        }
        
        JPanel internalPanel = createInternalPanel();
        add(internalPanel, BorderLayout.CENTER);
        
        
        
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        IconManager.IconType iconType = (m_forRSM) ? IconManager.IconType.RSM_DECOY : IconManager.IconType.RSET_DECOY;
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
                    wbox = WindowBoxFactory.getRsmPSMWindowBox("Decoy " + getTopComponentName(), true);
                    wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);
                    
                } else {
                     ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
                    ResultSet decoyRset = rset.getDecoyResultSet();
                    if (decoyRset == null) {
                        return;
                    }
                    wbox = WindowBoxFactory.getPeptidesForRsetOnlyWindowBox("Decoy " + getTopComponentName(), true);
                    wbox.setEntryData(m_dataBox.getProjectId(), decoyRset);
                }

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive();
            }
        });
        

        
        toolbar.add(m_decoyButton);
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
        m_peptideMatchTable = new PeptideMatchTable();
        m_peptideMatchTable.setModel(new PeptideMatchTableModel((LazyTable)m_peptideMatchTable, m_forRSM));
        
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, m_peptideMatchTable);
        
        m_scrollPane.setViewportView(m_peptideMatchTable);
	m_peptideMatchTable.setFillsViewportHeight(true);
	m_peptideMatchTable.setViewport(m_scrollPane.getViewport());
        
        if (m_startingPanel) {
            m_searchButton = new SearchButton();


            m_searchTextField = new JTextField(16) {

                @Override
                public Dimension getMinimumSize() {
                    return super.getPreferredSize();
                }
            };
        }
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        if (m_startingPanel) {
            // Search available only when the panel is the first of the window
            c.gridx = 0;
            c.gridy++;
            c.weighty = 0;
            c.weightx = 1;
            c.gridwidth = 1;
            internalPanel.add(Box.createHorizontalGlue(), c);

            c.gridx++;
            c.weightx = 0;
            internalPanel.add(m_searchTextField, c);

            c.gridx++;
            internalPanel.add(m_searchButton, c);
        }
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


    
    
    
    private class SearchButton extends JButton {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Long> peptideMatchIds = new ArrayList<>();

        public SearchButton() {
            
            setIcon(new javax.swing.ImageIcon(ImageUtilities.loadImage ("fr/proline/studio/images/search.png")));
            setMargin(new Insets(1,1,1,1));
            
            addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    doSearch();
                }
            });
        }

        public void sortingChanged() {
            if (peptideMatchIds.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((PeptideMatchTableModel) m_peptideMatchTable.getModel()).sortAccordingToModel(peptideMatchIds);
        }

        private void doSearch() {

            final String searchText = m_searchTextField.getText().trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                // search already done, display next result
                searchIndex++;
                if (searchIndex >= peptideMatchIds.size()) {
                    searchIndex = 0;
                }

                if (!peptideMatchIds.isEmpty()) {
                    ((PeptideMatchTable) m_peptideMatchTable).selectProteinSet(peptideMatchIds.get(searchIndex), searchText);
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

                            ((PeptideMatchTableModel) m_peptideMatchTable.getModel()).sortAccordingToModel(peptideMatchIds);

                            ((PeptideMatchTable) m_peptideMatchTable).selectProteinSet(peptideMatchIds.get(searchIndex), searchText);

                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        m_searchButton.setEnabled(true);
                    }
                };

                long rsetId = ((PeptideMatchTableModel) m_peptideMatchTable.getModel()).getResultSetId();

                
                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchPeptideMatchTask(callback, m_dataBox.getProjectId(), rsetId, searchText, peptideMatchIds));

                m_searchButton.setEnabled(false);
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
            setDefaultRenderer(Peptide.class, new PeptideRenderer());
            setDefaultRenderer(DMsQuery.class, new MsQueryRenderer());
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            setDefaultRenderer(Integer.class, new DefaultRightAlignRenderer(getDefaultRenderer(Integer.class))  );
            
            displayColumnAsPercentage(PeptideMatchTableModel.COLTYPE_PEPTIDE_SCORE);
            
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

        public void selectProteinSet(Long proteinSetId, String searchText) {
            PeptideMatchTableModel tableModel = (PeptideMatchTableModel) getModel();
            int row = tableModel.findRow(proteinSetId);
            if (row == -1) {
                return;
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
                    ((PeptideMatchTableModel) getModel()).dataUpdated();
                } finally {
                    selectionWillBeRestored(false);
                }



                // restore selected row
                if (rowSelectedInModel != -1) {
                    int rowSelectedInView = convertRowIndexToView(rowSelectedInModel);
                    setSelection(rowSelectedInView);

                    // if the subtask correspond to the loading of the data of the sorted column,
                    // we keep the row selected visible
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((PeptideMatchTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
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
    }
}
