package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinsOfPeptideMatchTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.DecoratedTable;
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

/**
 *
 * @author JM235353
 */
public class RsetProteinsPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private long m_peptideMatchCurId = -1;
    
    private JScrollPane m_proteinScrollPane;
    private ProteinTable m_proteinTable;
    private MarkerContainerPanel m_markerContainerPanel;

    public RsetProteinsPanel() {
        JPanel internalPanel = initComponents();

        setLayout(new BorderLayout());
        add(internalPanel, BorderLayout.CENTER);

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

    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        ((ProteinTable) m_proteinTable).dataUpdated(subTask, finished);
    }

    private void clearData() {
        ((ProteinsOfPeptideMatchTableModel) m_proteinTable.getModel()).setData(null);

    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.m_dataBox = dataBox;
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
        

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        return internalPanel;
        
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

            m_dataBox.propagateDataChanged(ProteinMatch.class);

        }
        
        public void dataUpdated(SubTask subTask, boolean finished) {

            LastAction keepLastAction = lastAction;
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

                lastAction = keepLastAction;

            }

            if (finished) {
                setSortable(true);
            }
        }
        
        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;
    }
}
