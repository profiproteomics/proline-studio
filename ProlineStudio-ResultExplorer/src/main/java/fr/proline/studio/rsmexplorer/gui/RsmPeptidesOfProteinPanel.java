package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.model.PeptideTableModel;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import org.jdesktop.swingx.JXTable;

/**
 * Panel for Peptides of a Protein 
 * @author JM235353
 */
public class RsmPeptidesOfProteinPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    
    private PeptideTable m_peptidesTable;
    private JScrollPane m_scrollPane;

    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    
    private DProteinMatch m_currentProteinMatch = null;
    
    private AddDataAnalyzerButton m_addCompareDataButton;
    
    /**
     * Creates new form RsmPeptidesOfProteinPanel
     */
    public RsmPeptidesOfProteinPanel() {
        setLayout(new BorderLayout());
        
        JPanel peptidesPanel = createPeptidesPanel();
        
        add(peptidesPanel, BorderLayout.CENTER);
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
        m_peptidesTable.setModel(new CompoundTableModel(new PeptideTableModel(), true));
        m_peptidesTable.getColumnExt(m_peptidesTable.convertColumnIndexToView(PeptideTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
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

        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_peptidesTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };
        m_exportButton = new ExportButton(((ProgressInterface) m_peptidesTable.getModel()), "Peptides", m_peptidesTable);
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_peptidesTable.getModel())) {
           
            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i!=null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
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
        CompoundTableModel compoundTableModel = ((CompoundTableModel)m_peptidesTable.getModel());
        indexInModelSelected = compoundTableModel.convertCompoundRowToBaseModelRow(indexInModelSelected);
        
        
        return ((PeptideTableModel) compoundTableModel.getBaseModel()).getPeptide(indexInModelSelected);
        
    }
    
    public void setData(DProteinMatch proteinMatch, ResultSummary rsm) {

        if (proteinMatch == m_currentProteinMatch) {
            return;
        }

        m_currentProteinMatch = proteinMatch;

        if ((proteinMatch == null) || (rsm == null)) {
            ((PeptideTableModel)((CompoundTableModel) m_peptidesTable.getModel()).getBaseModel()).setData(null);
        } else {
            PeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
            DPeptideInstance[] peptideInstances = peptideSet.getTransientDPeptideInstances();

            ((PeptideTableModel)((CompoundTableModel) m_peptidesTable.getModel()).getBaseModel()).setData(peptideInstances);

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
    public void addSingleValue(Object v) {
        getGlobalTableModelInterface().addSingleValue(v);
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_peptidesTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_peptidesTable;
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

            popupMenu.addAction(new RestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.propagateDataChanged(CompareDataInterface.class);
                }
            });
            popupMenu.addAction(new ClearRestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.propagateDataChanged(CompareDataInterface.class);
                }
            });

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        
       
    }
    
    

    
    
    
}
