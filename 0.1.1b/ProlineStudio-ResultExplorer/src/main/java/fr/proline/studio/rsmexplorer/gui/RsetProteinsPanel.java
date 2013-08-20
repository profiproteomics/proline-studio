package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSet;
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
import fr.proline.studio.utils.LazyTable;
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
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

/**
 * Panel for Protein Matches
 * @author JM235353
 */
public class RsetProteinsPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private long m_peptideMatchCurId = -1;
    
    private JScrollPane m_proteinScrollPane;
    private ProteinTable m_proteinTable;
    private MarkerContainerPanel m_markerContainerPanel;

    private JButton m_searchButton;
    private JTextField m_searchTextField;
    
    private JButton m_decoyButton;
    
    private boolean m_startingPanel;
    
    public RsetProteinsPanel(boolean startingPanel) {
        
        m_startingPanel = startingPanel;
        
        setLayout(new BorderLayout());
        
        JPanel internalPanel = initComponents();
        add(internalPanel, BorderLayout.CENTER);
        
        if (m_startingPanel) {
            JToolBar toolbar = initToolbar();
            add(toolbar, BorderLayout.WEST);
        }
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        IconManager.IconType iconType = IconManager.IconType.RSET_DECOY;
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
                    WindowBox wbox = WindowBoxFactory.getProteinMatchesForRsetWindowBox("Decoy " + getTopComponentName(), true);
                    wbox.setEntryData(m_dataBox.getProjectId(), decoyRset);


                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive();
            }
        });
        

        
        toolbar.add(m_decoyButton);
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
    
    public ProteinMatch getSelectedProteinMatch() {


        // Retrieve Selected Row
        int selectedRow = m_proteinTable.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = m_proteinTable.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinSet selected
        ProteinsOfPeptideMatchTableModel tableModel = (ProteinsOfPeptideMatchTableModel) m_proteinTable.getModel();

        return tableModel.getProteinMatch(selectedRow);
    }
    
    public void setDataProteinMatchArray(ProteinMatch[] proteinMatchArray) {
        // Modify the Model
        ((ProteinsOfPeptideMatchTableModel) m_proteinTable.getModel()).setData(proteinMatchArray);

        // Select the first row
        m_proteinTable.getSelectionModel().setSelectionInterval(0, 0);
        
        if (proteinMatchArray != null) {
            m_markerContainerPanel.setMaxLineNumber(proteinMatchArray.length);
        }
        
        ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
        if ((m_decoyButton != null) && (rset != null)) {
            m_decoyButton.setEnabled(rset.getDecoyResultSet() != null);
        }
    }

    public void setDataPeptideMatch(PeptideMatch peptideMatch) {

        if (peptideMatch == null) {
            clearData();
            m_peptideMatchCurId = -1;
            return;
        }
        
        if ((m_peptideMatchCurId!=-1) && (peptideMatch.getId() == m_peptideMatchCurId)) {
            return;
        }
        
        m_peptideMatchCurId = peptideMatch.getId();



        ProteinMatch[] proteinMatchArray = peptideMatch.getTransientData().getProteinMatches();



        // Modify the Model
        ((ProteinsOfPeptideMatchTableModel) m_proteinTable.getModel()).setData(proteinMatchArray);

        // Select the first row
        m_proteinTable.getSelectionModel().setSelectionInterval(0, 0);
        
        if (proteinMatchArray != null) {
            m_markerContainerPanel.setMaxLineNumber(proteinMatchArray.length);
        }
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        ((ProteinTable) m_proteinTable).dataUpdated(subTask, finished);
    }

    private void clearData() {
        ((ProteinsOfPeptideMatchTableModel) m_proteinTable.getModel()).setData(null);

    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
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
        m_proteinTable.setModel(new ProteinsOfPeptideMatchTableModel(m_proteinTable));

         TableColumn accColumn = m_proteinTable.getColumnModel().getColumn(ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        m_proteinTable.addMouseListener(renderer);
        
        
        m_markerContainerPanel = new MarkerContainerPanel(m_proteinScrollPane, (ProteinTable) m_proteinTable);
        
        m_proteinScrollPane.setViewportView(m_proteinTable);
        m_proteinTable.setFillsViewportHeight(true);
        m_proteinTable.setViewport(m_proteinScrollPane.getViewport());
        
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

    private class SearchButton extends JButton {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Long> proteinMatchIds = new ArrayList<>();

        public SearchButton() {

            setIcon(new javax.swing.ImageIcon(ImageUtilities.loadImage("fr/proline/studio/images/search.png")));
            setMargin(new Insets(1, 1, 1, 1));

            addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    doSearch();
                }
            });
        }

        public void sortingChanged() {
            if (proteinMatchIds.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((ProteinsOfPeptideMatchTableModel) m_proteinTable.getModel()).sortAccordingToModel(proteinMatchIds); 
        }

        private void doSearch() {

            final String searchText = m_searchTextField.getText().trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                // search already done, display next result
                searchIndex++;
                if (searchIndex >= proteinMatchIds.size()) {
                    searchIndex = 0;
                }

                if (!proteinMatchIds.isEmpty()) {
                    ((ProteinTable) m_proteinTable).selectProteinMatch(proteinMatchIds.get(searchIndex), searchText);
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

                            ((ProteinsOfPeptideMatchTableModel) m_proteinTable.getModel()).sortAccordingToModel(proteinMatchIds);

                            ((ProteinTable) m_proteinTable).selectProteinMatch(proteinMatchIds.get(searchIndex), searchText);

                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        m_searchButton.setEnabled(true);
                    }
                };

                ResultSet rset = (ResultSet) m_dataBox.getData(false, ResultSet.class);
 

                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchProteinMatchTask(callback, m_dataBox.getProjectId(), rset, searchText, proteinMatchIds));

                m_searchButton.setEnabled(false);
            }
        }
    }

    
    private class ProteinTable extends LazyTable {

        public ProteinTable() {
            super(m_proteinScrollPane.getVerticalScrollBar() );
            
           

            displayColumnAsPercentage(ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_SCORE);
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            setDefaultRenderer(Double.class, new DoubleRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) ); 
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
            
            m_dataBox.propagateDataChanged(ProteinMatch.class);

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
                    ((ProteinsOfPeptideMatchTableModel) getModel()).dataUpdated();
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
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((ProteinsOfPeptideMatchTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
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
        
        public void selectProteinMatch(Long proteinMatchId, String searchText) {
            ProteinsOfPeptideMatchTableModel tableModel = (ProteinsOfPeptideMatchTableModel) getModel();
            int row = tableModel.findRow(proteinMatchId);
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
        
        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;
    }
}
