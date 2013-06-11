package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.model.ProteinGroupTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.LazyTable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

/**
 * In : Window which display Protein Sets of a Result Summary
 * - Panel used to display Protein Sets (at the top)
 * 
 * @author JM235353
 */
public class RsmProteinSetPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
    private JScrollPane m_proteinGroupScrollPane;
    private ProteinGroupTable m_proteinGroupTable;
    private JButton m_searchButton;
    private JTextField m_searchTextField;
                
    
    private boolean m_forRSM;
    private JButton m_decoyButton;
    
    /**
     * Creates new form ProteinGroupsTablePanel
     */
    public RsmProteinSetPanel(boolean forRSM) {
        
        m_forRSM = forRSM;
        
        initComponents();

    }

    public void setData(Long taskId, ProteinSet[] proteinSets, boolean finished) {
        
                // update toolbar
        if (m_forRSM) {
            ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
            if (rsm != null) {
                m_decoyButton.setEnabled(rsm.getDecotResultSummary() != null);
            }
        }
        
        
        ((ProteinGroupTableModel) m_proteinGroupTable.getModel()).setData(taskId, proteinSets);

        // select the first row
        if ((proteinSets != null) && (proteinSets.length > 0)) {
            m_proteinGroupTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        
        if (finished) {
            m_proteinGroupTable.setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        ((ProteinGroupTable) m_proteinGroupTable).dataUpdated(subTask, finished);
    }

    public ProteinSet getSelectedProteinSet() {

        // Retrieve Selected Row
        int selectedRow = m_proteinGroupTable.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = m_proteinGroupTable.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinSet selected
        ProteinGroupTableModel tableModel = (ProteinGroupTableModel) m_proteinGroupTable.getModel();
        return tableModel.getProteinSet(selectedRow);
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.m_dataBox = dataBox;
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
        
        if (m_forRSM) {
            JToolBar toolbar = initToolbar();
            JPanel internalPanel = createInternalPanel();

            setLayout(new BorderLayout());
            add(toolbar, BorderLayout.WEST);
            add(internalPanel, BorderLayout.CENTER);
        } else {
            JPanel internalPanel = createInternalPanel();
            setLayout(new BorderLayout());
            add(internalPanel, BorderLayout.CENTER);
        }
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        IconManager.IconType iconType = (m_forRSM) ? IconManager.IconType.RSM_DECOY : IconManager.IconType.RSET_DECOY;
        m_decoyButton = new JButton(IconManager.getIcon(IconManager.IconType.RSM_DECOY ));
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
                WindowBox wbox = WindowBoxFactory.getProteinSetsWindowBox("Decoy " + getTopComponentName(), true);
                wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);

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
        m_proteinGroupScrollPane = new JScrollPane();
        
        m_proteinGroupTable = new ProteinGroupTable();
        m_proteinGroupTable.setModel(new ProteinGroupTableModel((LazyTable)m_proteinGroupTable));
        
        

        MarkerContainerPanel markerContainerPanel = new MarkerContainerPanel(m_proteinGroupScrollPane, (ProteinGroupTable) m_proteinGroupTable);
        
        m_proteinGroupScrollPane.setViewportView(m_proteinGroupTable);
        m_proteinGroupTable.setFillsViewportHeight(true);
        m_proteinGroupTable.setViewport(m_proteinGroupScrollPane.getViewport());
        
        m_proteinGroupTable.displayColumnAsPercentage(ProteinGroupTableModel.COLTYPE_PROTEIN_SCORE);


        
        m_searchButton = new SearchButton();
        try {
            m_searchButton.setIcon(new javax.swing.ImageIcon(ImageUtilities.loadImage ("fr/proline/studio/images/search.png")));
        } catch (NullPointerException e) {
            // Hack : netbeans editor introspection does
            // not work when this panel is added to another component
        }

        
        m_searchTextField = new JTextField(16) {
            @Override
            public Dimension getMinimumSize() {
                return super.getPreferredSize();
            }
            
        };
        m_searchTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_searchButton.doClick();
            }
        });
        
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(markerContainerPanel, c);
        
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
        
        return internalPanel;
    }                 


    
    private class SearchButton extends JButton  {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Integer> proteinSetIds = new ArrayList<>();

        
        public SearchButton() {
            
            setIcon(new ImageIcon(ImageUtilities.loadImage ("fr/proline/studio/images/search.png")));
            setMargin(new Insets(1,1,1,1));
            
            addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    doSearch();
                }
            });
        }
        
        public void sortingChanged() {
            if (proteinSetIds.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((ProteinGroupTableModel) m_proteinGroupTable.getModel()).sortAccordingToModel(proteinSetIds);
        }
        
        private void doSearch() {
            
            final String searchText = m_searchTextField.getText().trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                // search already done, display next result
                searchIndex++;
                if (searchIndex >= proteinSetIds.size()) {
                    searchIndex = 0;
                }
                
                if (!proteinSetIds.isEmpty()) {
                    ((ProteinGroupTable) m_proteinGroupTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
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
                        
                        
                        if (!proteinSetIds.isEmpty()) {
                            
                            ((ProteinGroupTableModel) m_proteinGroupTable.getModel()).sortAccordingToModel(proteinSetIds);

                            ((ProteinGroupTable) m_proteinGroupTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
                            
                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        m_searchButton.setEnabled(true);
                    }
                };

                ResultSummary rsm = ((ProteinGroupTableModel) m_proteinGroupTable.getModel()).getResultSummary();


                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchProteinSetsTask(callback, m_dataBox.getProjectId() ,rsm, searchText, proteinSetIds));

                m_searchButton.setEnabled(false);
            }
        }
        
    }
    
    private class ProteinGroupTable extends LazyTable  {
        /** 
         * Called whenever the value of the selection changes.
         * @param e the event that characterizes the change.
         */

        ProteinSet proteinSetSelected = null;
        
        
        public ProteinGroupTable() {
            super(m_proteinGroupScrollPane.getVerticalScrollBar() );
            
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            
            setDefaultRenderer(ProteinGroupTableModel.ProteinCount.class, new DefaultRightAlignRenderer(new DefaultTableRenderer()));
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
 
            m_dataBox.propagateDataChanged(ProteinSet.class);

        }
        
        public void selectProteinSet(Integer proteinSetId, String searchText) {
            ProteinGroupTableModel tableModel = (ProteinGroupTableModel) getModel();
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
            
            LastAction keepLastAction = lastAction;
            try {
            
            
            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((ProteinGroupTableModel) getModel()).dataUpdated();
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
                if (((keepLastAction == LastAction.ACTION_SELECTING ) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((ProteinGroupTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
                    ((ProteinGroupTable) m_proteinGroupTable).scrollRowToVisible(rowSelectedInView);
                }
                    
            }

            } finally {

                lastAction = keepLastAction;
 
            }
            
            if (finished) {
                setSortable(true);
            }
        }
        
        @Override
        public void sortingChanged(int col) {
            ((SearchButton)m_searchButton).sortingChanged();
        }
    
        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;
        
        
    }
    

    
    
}