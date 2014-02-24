package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.model.PeptideTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.utils.DecoratedTable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;

/**
 * Panel for Peptides of a Protein 
 * @author JM235353
 */
public class RsmPeptidesOfProteinPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
    private PeptideTable m_peptidesTable;
    private JScrollPane m_scrollPane;

    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    
    private DProteinMatch m_currentProteinMatch = null;
    
    /**
     * Creates new form RsmPeptidesOfProteinPanel
     */
    public RsmPeptidesOfProteinPanel() {
        setLayout(new BorderLayout());
        
        JPanel proteinPanel = createPeptidesPanel();
        
        add(proteinPanel, BorderLayout.CENTER);
    }

    private JPanel createPeptidesPanel() {
        JPanel peptidesPanel = new JPanel();
        peptidesPanel.setBounds(0, 0, 500, 400);

        peptidesPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        peptidesPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        peptidesPanel.add(toolbar, BorderLayout.WEST);


        return peptidesPanel;

    }
    

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_peptidesTable = new PeptideTable();
        m_peptidesTable.setModel(new PeptideTableModel());
        m_peptidesTable.getColumnExt(PeptideTableModel.COLTYPE_PEPTIDE_ID).setVisible(false);
        m_peptidesTable.displayColumnAsPercentage(PeptideTableModel.COLTYPE_PEPTIDE_SCORE);


        m_scrollPane = new JScrollPane();
        m_scrollPane.setViewportView(m_peptidesTable);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(m_scrollPane, c);

        return internalPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_filterButton = new FilterButton(((PeptideTableModel) m_peptidesTable.getModel()));
        m_exportButton = new ExportButton(((PeptideTableModel) m_peptidesTable.getModel()), "Peptides", m_peptidesTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);

        return toolbar;
    }

    
    public PeptideInstance getSelectedPeptide() {
        int selectedIndex = m_peptidesTable.getSelectionModel().getMinSelectionIndex();
            
        if (selectedIndex == -1) {
            return null;
        } 
        
        int indexInModelSelected = m_peptidesTable.convertRowIndexToModel(selectedIndex);
        return ((PeptideTableModel) m_peptidesTable.getModel()).getPeptide(indexInModelSelected);
        
    }
    
    public void setData(DProteinMatch proteinMatch, ResultSummary rsm) {

        if (proteinMatch == m_currentProteinMatch) {
            return;
        }

        m_currentProteinMatch = proteinMatch;
        
        if ((proteinMatch == null) || (rsm == null)) {
            ((PeptideTableModel) m_peptidesTable.getModel()).setData(null);
        } else {
            PeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
            PeptideInstance[] peptideInstances = peptideSet.getTransientPeptideInstances();

            ((PeptideTableModel) m_peptidesTable.getModel()).setData(peptideInstances);

            // select the first peptide
            if ((peptideInstances != null) && (peptideInstances.length > 0)) {
                m_peptidesTable.getSelectionModel().setSelectionInterval(0, 0);
            }
        }
        
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

    
    private class PeptideTable extends DecoratedTable  {
        
        public PeptideTable() {
            setDefaultRenderer(Peptide.class, new PeptideRenderer());
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            setDefaultRenderer(Double.class, new DoubleRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            
            // WART to have 4 digits for deltaMoz
            /*setDefaultRenderer(Float.class, new FloatRenderer(new DefaultRightAlignRenderer(getDefaultRenderer(String.class))) {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                    if (column == PeptideTableModel.COLTYPE_PEPTIDE_DELTA_MOZ) {
                            if (m_deltaMozRenderer == null) {
                                m_deltaMozRenderer = new FloatRenderer(new DefaultRightAlignRenderer(getDefaultRenderer(String.class)), 6);
                            }
                            return m_deltaMozRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        } else {


                            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        }
                    }


            });*/
            
            
        } 
        
        private FloatRenderer m_deltaMozRenderer = null;
        
        /** 
         * Called whenever the value of the selection changes.
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            
            super.valueChanged(e);
            

            
            m_dataBox.propagateDataChanged(PeptideInstance.class);

        }
        
 
        
       
    }
    
    

    
    
    
}
