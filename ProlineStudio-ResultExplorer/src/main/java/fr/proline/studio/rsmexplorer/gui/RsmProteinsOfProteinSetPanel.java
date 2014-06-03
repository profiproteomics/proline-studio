package fr.proline.studio.rsmexplorer.gui;



import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.gui.model.ProteinTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.BooleanRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.utils.DecoratedTable;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
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

    private JTextField m_proteinNameTextField;
    private ProteinTable m_proteinTable;
    private JScrollPane m_scrollPane;

    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    
    /**
     * Creates new form RsmProteinsOfProteinSetPanel
     */
    public RsmProteinsOfProteinSetPanel() {
        
        setLayout(new BorderLayout());
        
        JPanel proteinPanel = createProteinPanel();
        
        add(proteinPanel, BorderLayout.CENTER);
        
    }
    
    
    private JPanel createProteinPanel() {
        JPanel proteinPanel = new JPanel();
        proteinPanel.setBounds(0, 0, 500, 400);

        proteinPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        proteinPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        proteinPanel.add(toolbar, BorderLayout.WEST);


        return proteinPanel;

    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        
        m_proteinNameTextField = new JTextField();
        m_proteinNameTextField.setEditable(false);
        m_proteinNameTextField.setBackground(Color.white);
        
        
        m_scrollPane = new javax.swing.JScrollPane();
        m_proteinTable = new ProteinTable();
        m_proteinTable.setModel(new ProteinTableModel((ProgressInterface) m_proteinTable));
        m_scrollPane.setViewportView(m_proteinTable);

        
        
        m_proteinTable.displayColumnAsPercentage(ProteinTableModel.COLTYPE_PROTEIN_SCORE);
        TableColumn accColumn = m_proteinTable.getColumnModel().getColumn(ProteinTableModel.COLTYPE_PROTEIN_NAME);
        URLCellRenderer renderer = new URLCellRenderer("URL_Template_Protein_Accession", "http://www.uniprot.org/uniprot/", ProteinTableModel.COLTYPE_PROTEIN_NAME);
        accColumn.setCellRenderer(renderer);
        m_proteinTable.addMouseListener(renderer);
        
        // hide the id column  (must be done after the URLCellRenderer is set)
        m_proteinTable.getColumnExt(ProteinTableModel.COLTYPE_PROTEIN_ID).setVisible(false);
        
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(new JLabel("Typical Protein:"), c);
        
        c.gridx++;
        c.weightx = 1;
        internalPanel.add(m_proteinNameTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 1;
        internalPanel.add(m_scrollPane, c);
        
        return internalPanel;
        

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_filterButton = new FilterButton(((ProteinTableModel) m_proteinTable.getModel()));
        m_exportButton = new ExportButton(((ProteinTableModel) m_proteinTable.getModel()), "Proteins", m_proteinTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);

        return toolbar;
    }

    public DProteinMatch getSelectedProteinMatch() {

        ProteinTable table = (ProteinTable) m_proteinTable;

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        ProteinTableModel tableModel = (ProteinTableModel) table.getModel();
        if (tableModel.getRowCount() == 0) {
            return null; // this is a wart, for an unknown reason, it happens that the first row
            // is selected although it does not exist.
        }
        
        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinMatch selected


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
        m_proteinNameTextField.setText(typicalProtein.getDescription());


        // Modify the Model
        ((ProteinTableModel) m_proteinTable.getModel()).setData(proteinSet.getResultSummaryId(), sameSetArray, subSetArray);

        // Select the Row
        int row = ((ProteinTableModel) m_proteinTable.getModel()).findRowToSelect(searchedText);
        m_proteinTable.getSelectionModel().setSelectionInterval(row, row);

        m_proteinTable.setSortable(true);
        
    }

    private void clearData() {
        m_proteinNameTextField.setText("");
        //((ProteinGroupProteinSelectedPanel) ViewTopComponent.getPanel(ProteinGroupProteinSelectedPanel.class)).updateTitle(null); //JPM.TODO
        ((ProteinTableModel) m_proteinTable.getModel()).setData(-1, null, null);

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






    private class ProteinTable extends DecoratedTable implements ProgressInterface {

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

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
        }
    }
}
