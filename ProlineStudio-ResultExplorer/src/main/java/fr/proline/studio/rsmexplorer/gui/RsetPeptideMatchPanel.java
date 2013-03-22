package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseSearchPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.PeptideMatchTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.MsQueryRenderer;
import fr.proline.studio.utils.LazyTable;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import org.openide.util.ImageUtilities;

/**
 *
 * @author JM235353
 */
public class RsetPeptideMatchPanel extends javax.swing.JPanel implements DataBoxPanelInterface {

    private AbstractDataBox dataBox;

    /**
     * Creates new form RsetPeptideMatchPanel
     */
    public RsetPeptideMatchPanel() {
        initComponents();

    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.dataBox = dataBox;
    }

    public void setData(long taskId, PeptideMatch[] peptideMatches) {
        ((PeptideMatchTableModel) peptideMatchTable.getModel()).setData(taskId, peptideMatches);

        // select the first row
        if ((peptideMatches != null) && (peptideMatches.length > 0)) {
            peptideMatchTable.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    public void dataUpdated(SubTask subTask) {

        ((PeptideMatchTable) peptideMatchTable).dataUpdated(subTask);


    }

    public PeptideMatch getSelectedPeptideMatch() {

        PeptideMatchTable table = ((PeptideMatchTable) peptideMatchTable);

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        peptideMatchTable = new PeptideMatchTable();
        searchButton = new SearchButton();
        try {
            searchButton.setIcon(new javax.swing.ImageIcon(ImageUtilities.loadImage ("fr/proline/studio/images/search.png")));
        } catch (NullPointerException e) {
            // Hack : netbeans editor introspection does
            // not work when this panel is added to another component
        }
        searchTextField = new javax.swing.JTextField();

        peptideMatchTable.setModel(new PeptideMatchTableModel((LazyTable)peptideMatchTable));
        scrollPane.setViewportView(peptideMatchTable);

        searchButton.setText(org.openide.util.NbBundle.getMessage(RsetPeptideMatchPanel.class, "RsetPeptideMatchPanel.searchButton.text")); // NOI18N

        searchTextField.setText(org.openide.util.NbBundle.getMessage(RsetPeptideMatchPanel.class, "RsetPeptideMatchPanel.searchTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable peptideMatchTable;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchTextField;
    // End of variables declaration//GEN-END:variables

    private class SearchButton extends JButton {

        String previousSearch = "";
        int searchIndex = 0;
        ArrayList<Integer> peptideMatchIds = new ArrayList<Integer>();

        public SearchButton() {
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
            ((PeptideMatchTableModel) peptideMatchTable.getModel()).sortAccordingToModel(peptideMatchIds);
        }

        private void doSearch() {

            final String searchText = searchTextField.getText().trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                // search already done, display next result
                searchIndex++;
                if (searchIndex >= peptideMatchIds.size()) {
                    searchIndex = 0;
                }

                if (!peptideMatchIds.isEmpty()) {
                    ((PeptideMatchTable) peptideMatchTable).selectProteinSet(peptideMatchIds.get(searchIndex), searchText);
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

                            ((PeptideMatchTableModel) peptideMatchTable.getModel()).sortAccordingToModel(peptideMatchIds);

                            ((PeptideMatchTable) peptideMatchTable).selectProteinSet(peptideMatchIds.get(searchIndex), searchText);

                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        searchButton.setEnabled(true);
                    }
                };

                ResultSet rset = ((PeptideMatchTableModel) peptideMatchTable.getModel()).getResultSet();

                
                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchPeptideMatchTask(callback, dataBox.getProjectId(), rset, searchText, peptideMatchIds));

                searchButton.setEnabled(false);
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
            super(scrollPane.getVerticalScrollBar());
            setDefaultRenderer(Peptide.class, new PeptideRenderer());
            setDefaultRenderer(MsQuery.class, new MsQueryRenderer());
            setDefaultRenderer(Float.class, new FloatRenderer( getDefaultRenderer(String.class) ) );
            
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



            dataBox.propagateDataChanged(PeptideMatch.class);

        }

        public void selectProteinSet(Integer proteinSetId, String searchText) {
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
        
        
        
        public void dataUpdated(SubTask subTask) {

            LastAction keepLastAction = lastAction;
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
                        ((PeptideMatchTable) peptideMatchTable).scrollRowToVisible(rowSelectedInView);
                    }

                }

            } finally {

                lastAction = keepLastAction;

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
