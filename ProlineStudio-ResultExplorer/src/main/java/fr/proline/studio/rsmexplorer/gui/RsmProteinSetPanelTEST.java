package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.*;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinGroupTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.LazyTable;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.openide.util.ImageUtilities;

/**
 * In : Window which display Protein Sets of a Result Summary
 * - Panel used to display Protein Sets (at the top)
 * 
 * @author JM235353
 */
public class RsmProteinSetPanelTEST extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox dataBox;
    
    private JScrollPane proteinGroupScrollPane;
    private ProteinGroupTable proteinGroupTable;
    private JButton searchButton;
    private JTextField searchTextField;
                
    
    
    /**
     * Creates new form ProteinGroupsTablePanel
     */
    public RsmProteinSetPanelTEST() {
        initComponents();
        

        proteinGroupTable.displayColumnAsPercentage(ProteinGroupTableModel.COLTYPE_PROTEIN_SCORE);

        searchTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchButton.doClick();
            }
            
        });
        

    }

    public void setData(Long taskId, ProteinSet[] proteinSets, boolean finished) {
        ((ProteinGroupTableModel) proteinGroupTable.getModel()).setData(taskId, proteinSets);

        // select the first row
        if ((proteinSets != null) && (proteinSets.length > 0)) {
            proteinGroupTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        
        if (finished) {
            proteinGroupTable.setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        ((ProteinGroupTable) proteinGroupTable).dataUpdated(subTask, finished);
    }

    public ProteinSet getSelectedProteinSet() {

        // Retrieve Selected Row
        int selectedRow = proteinGroupTable.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = proteinGroupTable.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinSet selected
        ProteinGroupTableModel tableModel = (ProteinGroupTableModel) proteinGroupTable.getModel();
        return tableModel.getProteinSet(selectedRow);
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.dataBox = dataBox;
    }
        
                       
    private void initComponents() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // create objects
        proteinGroupScrollPane = new JScrollPane();
        
        proteinGroupTable = new ProteinGroupTable();
        proteinGroupTable.setModel(new ProteinGroupTableModel((LazyTable)proteinGroupTable));
        
        

        MarkerContainerPanel markerContainerPanel = new MarkerContainerPanel(proteinGroupScrollPane, (ProteinGroupTable) proteinGroupTable);
        
        proteinGroupScrollPane.setViewportView(proteinGroupTable);
        proteinGroupTable.setFillsViewportHeight(true);
        proteinGroupTable.setViewport(proteinGroupScrollPane.getViewport());
        
        
        searchButton = new SearchButton();
        try {
            searchButton.setIcon(new javax.swing.ImageIcon(ImageUtilities.loadImage ("fr/proline/studio/images/search.png")));
        } catch (NullPointerException e) {
            // Hack : netbeans editor introspection does
            // not work when this panel is added to another component
        }
        searchTextField = new JTextField(16) {
            @Override
            public Dimension getMinimumSize() {
                return super.getPreferredSize();
            }
            
        };

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        add(markerContainerPanel, c);
        
        c.gridx = 0;
        c.gridy++;
        c.weighty = 0;
        c.weightx = 1;
        c.gridwidth = 1;
        add(Box.createHorizontalGlue(), c);
        
        c.gridx++;
        c.weightx = 0;
        add(searchTextField, c);
        
        c.gridx++;
        add(searchButton, c);
    }                 


    
    private class SearchButton extends JButton  {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Integer> proteinSetIds = new ArrayList<Integer>();

        
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
            ((ProteinGroupTableModel) proteinGroupTable.getModel()).sortAccordingToModel(proteinSetIds);
        }
        
        private void doSearch() {
            
            final String searchText = searchTextField.getText().trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                // search already done, display next result
                searchIndex++;
                if (searchIndex >= proteinSetIds.size()) {
                    searchIndex = 0;
                }
                
                if (!proteinSetIds.isEmpty()) {
                    ((ProteinGroupTable) proteinGroupTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
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
                            
                            ((ProteinGroupTableModel) proteinGroupTable.getModel()).sortAccordingToModel(proteinSetIds);

                            ((ProteinGroupTable) proteinGroupTable).selectProteinSet(proteinSetIds.get(searchIndex), searchText);
                            
                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        searchButton.setEnabled(true);
                    }
                };

                ResultSummary rsm = ((ProteinGroupTableModel) proteinGroupTable.getModel()).getResultSummary();


                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchProteinSetsTask(callback, dataBox.getProjectId() ,rsm, searchText, proteinSetIds));

                searchButton.setEnabled(false);
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
            super(proteinGroupScrollPane.getVerticalScrollBar() );
            
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
 
            dataBox.propagateDataChanged(ProteinSet.class);

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
                    ((ProteinGroupTable) proteinGroupTable).scrollRowToVisible(rowSelectedInView);
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
            ((SearchButton)searchButton).sortingChanged();
        }
    
        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;
        
        
    }
    

    
    
}