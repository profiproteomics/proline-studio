package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinsOfPeptideMatchTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.URLCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

/**
 *
 * @author JM235353
 */
public class RsetProteinsForPeptideMatchPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox dataBox;
    private Integer peptideMatchCurId = null;

    /**
     * Creates new form RsetProteinsForPeptideMatchPanel
     */
    public RsetProteinsForPeptideMatchPanel() {
        initComponents();

        TableColumn accColumn = proteinTable.getColumnModel().getColumn(ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", ProteinsOfPeptideMatchTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        proteinTable.addMouseListener(renderer);
    }

    public ProteinMatch getSelectedProteinMatch() {

        ProteinTable table = (ProteinTable) proteinTable;

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinSet selected
        ProteinsOfPeptideMatchTableModel tableModel = (ProteinsOfPeptideMatchTableModel) table.getModel();

        return tableModel.getProteinMatch(selectedRow);
    }

    public void setData(PeptideMatch peptideMatch) {

        if (peptideMatch == null) {
            clearData();
            peptideMatchCurId = null;
            return;
        }
        
        if ((peptideMatchCurId!=null) && (peptideMatch.getId().intValue() == peptideMatchCurId.intValue())) {
            return;
        }
        
        peptideMatchCurId = peptideMatch.getId();



        ProteinMatch[] proteinMatchArray = peptideMatch.getTransientData().getProteinMatches();



        // Modify the Model
        ((ProteinsOfPeptideMatchTableModel) proteinTable.getModel()).setData(proteinMatchArray);

        // Select the first row
        proteinTable.getSelectionModel().setSelectionInterval(0, 0);

    }

    private void clearData() {
        ((ProteinsOfPeptideMatchTableModel) proteinTable.getModel()).setData(null);

    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.dataBox = dataBox;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        proteinTable = new ProteinTable();

        proteinTable.setModel(new ProteinsOfPeptideMatchTableModel());
        scrollPane.setViewportView(proteinTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable proteinTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    private class ProteinTable extends DecoratedTable {

        public ProteinTable() {
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

            dataBox.propagateDataChanged(ProteinMatch.class);

        }
    }
}
