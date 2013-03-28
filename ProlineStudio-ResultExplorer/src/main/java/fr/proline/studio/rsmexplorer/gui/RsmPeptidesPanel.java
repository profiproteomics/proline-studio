package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseSearchPeptideInstanceTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.PeptideInstanceTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.utils.LazyTable;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import org.openide.util.ImageUtilities;


/**
 *
 * @author JM235353
 */
public class RsmPeptidesPanel extends javax.swing.JPanel implements DataBoxPanelInterface {

    private AbstractDataBox dataBox;

    private PeptideInstanceTable peptideInstanceTable;
    private JScrollPane scrollPane;
    private JButton searchButton;
    private JTextField searchTextField;
    
    
    /**
     * Creates new form RsetPeptideMatchPanel
     */
    public RsmPeptidesPanel() {
        initComponents();

    }
    
    private void initComponents() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        


        // create objects
        scrollPane = new JScrollPane();
        
        peptideInstanceTable = new PeptideInstanceTable();
        peptideInstanceTable.setModel(new PeptideInstanceTableModel((LazyTable) peptideInstanceTable));
        
        MarkerContainerPanel markerContainerPanel = new MarkerContainerPanel(scrollPane, (PeptideInstanceTable) peptideInstanceTable);
        
        
        scrollPane.setViewportView(peptideInstanceTable);
	peptideInstanceTable.setFillsViewportHeight(true);
	peptideInstanceTable.setViewport(scrollPane.getViewport());

        searchButton = new SearchButton();
      
        
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

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.dataBox = dataBox;
    }

    public void setData(long taskId, PeptideInstance[] peptideInstances, boolean finished) {
        ((PeptideInstanceTableModel) peptideInstanceTable.getModel()).setData(taskId, peptideInstances);

        // select the first row
        if ((peptideInstances != null) && (peptideInstances.length > 0)) {
            peptideInstanceTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        
        if (finished) {
            ((PeptideInstanceTable)peptideInstanceTable).setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {

        ((PeptideInstanceTable) peptideInstanceTable).dataUpdated(subTask, finished);


    }

    public PeptideInstance getSelectedPeptideInstance() {

        PeptideInstanceTable table = ((PeptideInstanceTable) peptideInstanceTable);

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinSet selected
        PeptideInstanceTableModel tableModel = (PeptideInstanceTableModel) table.getModel();
        return tableModel.getPeptideInstance(selectedRow);
    }


                 

               

    private class SearchButton extends JButton {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Integer> peptideInstanceIds = new ArrayList<>();

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
            if (peptideInstanceIds.isEmpty()) {
                return;
            }
            searchIndex = -1;
            ((PeptideInstanceTableModel) peptideInstanceTable.getModel()).sortAccordingToModel(peptideInstanceIds);
        }

        private void doSearch() {
            
            final String searchText = searchTextField.getText().trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                // search already done, display next result
                searchIndex++;
                if (searchIndex >= peptideInstanceIds.size()) {
                    searchIndex = 0;
                }

                if (!peptideInstanceIds.isEmpty()) {
                    ((PeptideInstanceTable) peptideInstanceTable).selectPeptideInstance(peptideInstanceIds.get(searchIndex), searchText);
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


                        if (!peptideInstanceIds.isEmpty()) {

                            ((PeptideInstanceTableModel) peptideInstanceTable.getModel()).sortAccordingToModel(peptideInstanceIds);

                            ((PeptideInstanceTable) peptideInstanceTable).selectPeptideInstance(peptideInstanceIds.get(searchIndex), searchText);

                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        searchButton.setEnabled(true);
                    }
                };

                ResultSummary rsm = ((PeptideInstanceTableModel) peptideInstanceTable.getModel()).getResultSummary();

                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchPeptideInstanceTask(callback, dataBox.getProjectId(), rsm, searchText, peptideInstanceIds));

                searchButton.setEnabled(false);
            }
        }
    }

    private class PeptideInstanceTable extends LazyTable {

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        public PeptideInstanceTable() {
            super(scrollPane.getVerticalScrollBar());
            setDefaultRenderer(Peptide.class, new PeptideRenderer());
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            setDefaultRenderer(Integer.class, new DefaultRightAlignRenderer(getDefaultRenderer(Integer.class))  );

            displayColumnAsPercentage(PeptideInstanceTableModel.COLTYPE_PEPTIDE_SCORE);
            
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



            dataBox.propagateDataChanged(PeptideInstance.class); //JPM.TODO

        }

        public void selectPeptideInstance(Integer peptideInstanceId, String searchText) {
            PeptideInstanceTableModel tableModel = (PeptideInstanceTableModel) getModel();
            int row = tableModel.findRow(peptideInstanceId);
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
                    ((PeptideInstanceTableModel) getModel()).dataUpdated();
                } finally {
                    selectionWillBeRestored(false);
                }



                // restore selected row
                if (rowSelectedInModel != -1) {
                    int rowSelectedInView = convertRowIndexToView(rowSelectedInModel);
                    setSelection(rowSelectedInView);

                    // if the subtask correspond to the loading of the data of the sorted column,
                    // we keep the row selected visible
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((PeptideInstanceTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
                        ((PeptideInstanceTable) peptideInstanceTable).scrollRowToVisible(rowSelectedInView);
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
            //((SearchButton)searchButton).sortingChanged();
        }

        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;
    }
}
