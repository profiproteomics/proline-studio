package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.comparedata.AddCompareDataButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.CompareDataProviderInterface;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.rsmexplorer.gui.model.PeptideTableModel;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;

/**
 * Panel for Peptides of a Protein 
 * @author JM235353
 */
public class RsmPeptidesOfProteinPanel extends HourglassPanel implements DataBoxPanelInterface, CompareDataProviderInterface {

    private AbstractDataBox m_dataBox;
    
    private PeptideTable m_peptidesTable;
    private JScrollPane m_scrollPane;

    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    
    private DProteinMatch m_currentProteinMatch = null;
    
    private AddCompareDataButton m_addCompareDataButton;
    
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
        m_peptidesTable.setTableRenderer();
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

        m_filterButton = new FilterButton(((PeptideTableModel) m_peptidesTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };
        m_exportButton = new ExportButton(((PeptideTableModel) m_peptidesTable.getModel()), "Peptides", m_peptidesTable);
        m_addCompareDataButton = new AddCompareDataButton(((PeptideTableModel) m_peptidesTable.getModel()), (CompareDataInterface) m_peptidesTable.getModel()) {

            @Override
            public void actionPerformed(CompareDataInterface compareDataInterface) {
                compareDataInterface.setName(m_dataBox.getFullName());
                DataMixerWindowBoxManager.addCompareTableModel(compareDataInterface);
            }
        };
        
        
        
        toolbar.add(m_filterButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);

        return toolbar;
    }

    
    public DPeptideInstance getSelectedPeptide() {
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
            DPeptideInstance[] peptideInstances = peptideSet.getTransientDPeptideInstances();

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
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }
    
    @Override
    public CompareDataInterface getCompareDataInterface() {
        return (CompareDataInterface) m_peptidesTable.getModel();
    }
    
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peptidesTable;
    }

    
    private class PeptideTable extends DecoratedTable  {
        
        public PeptideTable() {
            
        } 
        
        public void setTableRenderer() {
            getColumnModel().getColumn(convertColumnIndexToView(PeptideTableModel.COLTYPE_PEPTIDE_EXPERIMENTAL_MOZ)).setCellRenderer(new DoubleRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)),4 ) );
            getColumnModel().getColumn(convertColumnIndexToView(PeptideTableModel.COLTYPE_PEPTIDE_CALCULATED_MASS)).setCellRenderer(new DoubleRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)),4 ) );
            setDefaultRenderer(DPeptideMatch.class, new PeptideRenderer());
            setDefaultRenderer(Float.class, new FloatRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            setDefaultRenderer(Double.class, new DoubleRenderer( new DefaultRightAlignRenderer(getDefaultRenderer(String.class)) ) );
            
        }
        
        /** 
         * Called whenever the value of the selection changes.
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            
            super.valueChanged(e);
            

            
            m_dataBox.propagateDataChanged(DPeptideInstance.class);
            m_dataBox.propagateDataChanged(DPeptideMatch.class);
        }
        
 
        @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();

            popupMenu.addAction(new RestrainAction());
            popupMenu.addAction(new ClearRestrainAction());

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        
       
    }
    
    

    
    
    
}
