package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseSearchPeptideInstanceTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.model.PeptideInstanceTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
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
 *
 * @author JM235353
 */
public class RsmPeptidesPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    private PeptideInstanceTable m_peptideInstanceTable;
    private JScrollPane m_scrollPane;
    private JButton m_searchButton;
    private JTextField m_searchTextField;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
    private JButton m_decoyButton;
    
    /**
     * Creates new form RsetPeptideMatchPanel
     */
    public RsmPeptidesPanel() {
        initComponents();

    }
    
    private void initComponents() {

        JToolBar toolbar = initToolbar();
        JPanel internalPanel = createInternalPanel();

        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.WEST);
        add(internalPanel, BorderLayout.CENTER);

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
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_decoyButton = new JButton(IconManager.getIcon(IconManager.IconType.RSM_DECOY));
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
                WindowBox wbox = WindowBoxFactory.getRsmPeptidesWindowBox("Decoy " + getTopComponentName(), true);
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
        m_scrollPane = new JScrollPane();
        
        m_peptideInstanceTable = new PeptideInstanceTable();
        m_peptideInstanceTable.setModel(new PeptideInstanceTableModel((LazyTable) m_peptideInstanceTable));
        
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, (PeptideInstanceTable) m_peptideInstanceTable);
        
        
        m_scrollPane.setViewportView(m_peptideInstanceTable);
	m_peptideInstanceTable.setFillsViewportHeight(true);
	m_peptideInstanceTable.setViewport(m_scrollPane.getViewport());

        m_searchButton = new SearchButton();
      
        
        m_searchTextField = new JTextField(16) {
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
        internalPanel.add(m_markerContainerPanel, c);
        
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

    public void setData(long taskId, PeptideInstance[] peptideInstances, boolean finished) {
        
        ResultSummary rsm = (ResultSummary) m_dataBox.getData(false, ResultSummary.class);
        if (rsm != null) {
            m_decoyButton.setEnabled(rsm.getDecotResultSummary() != null);
        }
        
        ((PeptideInstanceTableModel) m_peptideInstanceTable.getModel()).setData(taskId, peptideInstances);

        // select the first row
        if ((peptideInstances != null) && (peptideInstances.length > 0)) {
            m_peptideInstanceTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peptideInstances.length);
        }
        
        if (finished) {
            ((PeptideInstanceTable)m_peptideInstanceTable).setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {

        ((PeptideInstanceTable) m_peptideInstanceTable).dataUpdated(subTask, finished);


    }

    public PeptideInstance getSelectedPeptideInstance() {

        PeptideInstanceTable table = ((PeptideInstanceTable) m_peptideInstanceTable);

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
        ArrayList<Long> peptideInstanceIds = new ArrayList<>();

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
            ((PeptideInstanceTableModel) m_peptideInstanceTable.getModel()).sortAccordingToModel(peptideInstanceIds);
        }

        private void doSearch() {
            
            final String searchText = m_searchTextField.getText().trim().toUpperCase();

            if (searchText.compareTo(previousSearch) == 0) {
                // search already done, display next result
                searchIndex++;
                if (searchIndex >= peptideInstanceIds.size()) {
                    searchIndex = 0;
                }

                if (!peptideInstanceIds.isEmpty()) {
                    ((PeptideInstanceTable) m_peptideInstanceTable).selectPeptideInstance(peptideInstanceIds.get(searchIndex), searchText);
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

                            ((PeptideInstanceTableModel) m_peptideInstanceTable.getModel()).sortAccordingToModel(peptideInstanceIds);

                            ((PeptideInstanceTable) m_peptideInstanceTable).selectPeptideInstance(peptideInstanceIds.get(searchIndex), searchText);

                        }


                        //System.out.println("Ids size "+proteinSetIds.size());
                        m_searchButton.setEnabled(true);
                    }
                };

                ResultSummary rsm = ((PeptideInstanceTableModel) m_peptideInstanceTable.getModel()).getResultSummary();

                // Load data if needed asynchronously
                AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseSearchPeptideInstanceTask(callback, m_dataBox.getProjectId(), rsm, searchText, peptideInstanceIds));

                m_searchButton.setEnabled(false);
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
            super(m_scrollPane.getVerticalScrollBar());
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



            m_dataBox.propagateDataChanged(PeptideInstance.class); //JPM.TODO

        }

        public void selectPeptideInstance(Long peptideInstanceId, String searchText) {
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

            LastAction keepLastAction = m_lastAction;
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
                        ((PeptideInstanceTable) m_peptideInstanceTable).scrollRowToVisible(rowSelectedInView);
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
