package fr.proline.studio.rsmexplorer.gui;



import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.BooleanRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

/**
 * In : Window which display Protein Sets of a Result Summary - Panel used to display Proteins of a Protein Set (at the
 * center of the window)
 *
 * @author JM235353
 */
public class RsmProteinsOfProteinSetPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private DProteinSet m_proteinSetCur = null;

    /**
     * Creates new form RsmProteinsOfProteinSetPanel
     */
    public RsmProteinsOfProteinSetPanel() {
        initComponents();

        ((DecoratedTable) proteinTable).displayColumnAsPercentage(ProteinTableModel.COLTYPE_PROTEIN_SCORE);
        TableColumn accColumn = proteinTable.getColumnModel().getColumn(ProteinTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", ProteinTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        proteinTable.addMouseListener(renderer);


    }

    public DProteinMatch getSelectedProteinMatch() {

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
        ProteinTableModel tableModel = (ProteinTableModel) table.getModel();

        return tableModel.getProteinMatch(selectedRow);
    }

    public void setData(DProteinSet proteinSet, String searchedText) {

        if (proteinSet == m_proteinSetCur) {
            return;
        }
        m_proteinSetCur = proteinSet;

        if (proteinSet == null) {
            clearData();
            return;
        }

        // retrieve sameset and subset
        DProteinMatch[] sameSetArray = proteinSet.getSameSet();
        DProteinMatch[] subSetArray = proteinSet.getSubSet();

        // retrieve Typical Protein Match
        DProteinMatch typicalProtein = proteinSet.getTypicalProteinMatch();

        
        if (typicalProtein == null) {
            // data not ready
            clearData();
            return;
        }
        
        // Modify Panel Border Title
        //((ProteinGroupProteinSelectedPanel) ViewTopComponent.getPanel(ProteinGroupProteinSelectedPanel.class)).updateTitle(typicalProtein.getAccession());
        //JPM.TODO


        // Modify protein description
        proteinNameTextField.setText(typicalProtein.getDescription());


        // Modify the Model
        ((ProteinTableModel) proteinTable.getModel()).setData(proteinSet.getResultSummaryId(), sameSetArray, subSetArray);

        // Select the Row
        int row = ((ProteinTableModel) proteinTable.getModel()).findRowToSelect(searchedText);
        proteinTable.getSelectionModel().setSelectionInterval(row, row);

    }

    private void clearData() {
        proteinNameTextField.setText("");
        //((ProteinGroupProteinSelectedPanel) ViewTopComponent.getPanel(ProteinGroupProteinSelectedPanel.class)).updateTitle(null); //JPM.TODO
        ((ProteinTableModel) proteinTable.getModel()).setData(-1, null, null);

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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        proteinNameTextField = new javax.swing.JTextField();
        proteinNameTextField.setEditable(false);
        proteinNameTextField.setBackground(Color.white);
        scrollPane = new javax.swing.JScrollPane();
        proteinTable = new ProteinTable();

        proteinNameTextField.setText(org.openide.util.NbBundle.getMessage(RsmProteinsOfProteinSetPanel.class, "RsmProteinsOfProteinSetPanel.proteinNameTextField.text")); // NOI18N

        proteinTable.setModel(new ProteinTableModel());
        scrollPane.setViewportView(proteinTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(proteinNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proteinNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField proteinNameTextField;
    private javax.swing.JTable proteinTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables



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

            m_dataBox.propagateDataChanged(DProteinMatch.class);


        }
    }
}
