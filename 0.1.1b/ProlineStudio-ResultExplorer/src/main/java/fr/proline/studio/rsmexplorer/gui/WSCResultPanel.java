package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.dpm.task.ComputeSCTask;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import fr.proline.studio.rsmexplorer.gui.model.WSCProteinTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.BooleanRenderer;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * Panel for Protein Matches
 * @author JM235353
 */
public class WSCResultPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private ComputeSCTask.WSCResultData m_weightedSCResult = null;
   
    private javax.swing.JTextField rsmRefNameField;
    private javax.swing.JTable proteinTable;
    private javax.swing.JScrollPane scrollPane;
    
    
    
    /**
     * Creates new form RsmProteinsOfProteinSetPanel
     */
    public WSCResultPanel() {
        initComponents();

//        ((DecoratedTable) proteinTable).displayColumnAsPercentage(WSCProteinTableModel.COLTYPE_);
        TableColumn accColumn = proteinTable.getColumnModel().getColumn(WSCProteinTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", WSCProteinTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        proteinTable.addMouseListener(renderer);

    }

//    public ProteinMatch getSelectedProteinMatch() {
//
//       ProteinTable table = (ProteinTable) proteinTable;
//
//        // Retrieve Selected Row
//        int selectedRow = table.getSelectedRow();
//
//
//        // nothing selected
//        if (selectedRow == -1) {
//            return null;
//
//        }
//
//        // convert according to the sorting
//        selectedRow = table.convertRowIndexToModel(selectedRow);
//
//
//
//        // Retrieve ProteinSet selected
//        WSCProteinTableModel tableModel = (WSCProteinTableModel) table.getModel();
//
//        return tableModel.getProteinMatch(selectedRow);
//    }

    public void setData(ComputeSCTask.WSCResultData scResult, String searchedText) {

        if (scResult == m_weightedSCResult) {
            return;
        }
        m_weightedSCResult = scResult;

        if (m_weightedSCResult == null) {
            clearData();
            return;
        }


        // Modify protein description
        rsmRefNameField.setText(scResult.getDataSetReference().getName());


        // Modify the Model
        ((WSCProteinTableModel) proteinTable.getModel()).setData(scResult);

        // Select the Row
        int row = ((WSCProteinTableModel) proteinTable.getModel()).findRowToSelect(searchedText);
        proteinTable.getSelectionModel().setSelectionInterval(row, row);

    }

    private void clearData() {
        rsmRefNameField.setText("");
        ((WSCProteinTableModel) proteinTable.getModel()).setData(null);

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

    /**
     * This method is called from within the constructor to initialize the form. 
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        rsmRefNameField = new javax.swing.JTextField();
        rsmRefNameField.setEditable(false);
        rsmRefNameField.setBackground(Color.white);
        
        scrollPane = new javax.swing.JScrollPane();
        proteinTable = new ProteinTable();

        proteinTable.setModel(new WSCProteinTableModel());
        scrollPane.setViewportView(proteinTable);
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        add(rsmRefNameField, c);
        
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        add(scrollPane, c);

    }
               



    private class ProteinTable extends DecoratedTable {

        public ProteinTable() {
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(Float.class)) ) );
            setDefaultRenderer(Boolean.class, new BooleanRenderer());
            
        
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
    }
}
