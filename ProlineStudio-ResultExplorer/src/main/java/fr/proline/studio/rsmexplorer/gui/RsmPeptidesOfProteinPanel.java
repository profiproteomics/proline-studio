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
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.PeptideTableModel;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;

/**
 * Panel for Peptides of a Protein 
 * @author JM235353
 */
public class RsmPeptidesOfProteinPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    
    private PeptideTable m_peptidesTable;
    private JScrollPane m_scrollPane;
    
    private MarkerContainerPanel m_markerContainerPanel;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    
    private DProteinMatch m_currentProteinMatch = null;
    private DPeptideMatch m_currentPeptideMatch = null;
    
    private AddDataAnalyzerButton m_addCompareDataButton;
    
    private InfoToggleButton m_infoToggleButton;
    
    /**
     * Creates new form RsmPeptidesOfProteinPanel
     */
    public RsmPeptidesOfProteinPanel() {
        initComponents();
    }

    private void initComponents() {


        setLayout(new BorderLayout());

        final JPanel peptidesPanel = createPeptidesPanel();


        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptidesPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                layeredPane.revalidate();
                layeredPane.repaint();

            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        add(layeredPane, BorderLayout.CENTER);

        layeredPane.add(peptidesPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);  

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


        m_scrollPane = new JScrollPane();
        
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, m_peptidesTable);
        
        m_scrollPane.setViewportView(m_peptidesTable);
        m_peptidesTable.setFillsViewportHeight(true);
        m_peptidesTable.setViewport(m_scrollPane.getViewport());
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_peptidesTable.getModel()), m_peptidesTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_peptidesTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
                m_infoToggleButton.updateInfo();
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
        
        m_infoToggleButton = new InfoToggleButton((ProgressInterface) m_peptidesTable.getModel(), m_peptidesTable);
        
        
        
        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        toolbar.add(m_infoToggleButton);

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
    
    public void setData(DProteinMatch proteinMatch, DPeptideMatch peptideMatch, ResultSummary rsm) {

        if ((proteinMatch == m_currentProteinMatch) && (peptideMatch == m_currentPeptideMatch)) {
            return;
        }

        boolean proteinMatchChanged = (proteinMatch != m_currentProteinMatch);
        if (proteinMatchChanged) {
            
            m_currentProteinMatch = proteinMatch;
            
            if ((proteinMatch == null) || (rsm == null)) {
                ((PeptideTableModel) ((CompoundTableModel) m_peptidesTable.getModel()).getBaseModel()).setData(null);
            } else {
                PeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
                DPeptideInstance[] peptideInstances = peptideSet.getTransientDPeptideInstances();

                ((PeptideTableModel) ((CompoundTableModel) m_peptidesTable.getModel()).getBaseModel()).setData(peptideInstances);

            }
            
        }
        
        m_infoToggleButton.updateInfo();
        
        if (proteinMatch != null) {

            boolean peptideMatchChanged = (peptideMatch != m_currentPeptideMatch);
            if ((!peptideMatchChanged || (peptideMatchChanged && (m_currentPeptideMatch==null))) && proteinMatchChanged) {
                // select first peptide
                PeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
                DPeptideInstance[] peptideInstances = peptideSet.getTransientDPeptideInstances();
                if ((peptideInstances != null) && (peptideInstances.length > 0)) {

                    m_peptidesTable.getSelectionModel().setSelectionInterval(0, 0);
                    m_markerContainerPanel.setMaxLineNumber(peptideInstances.length);
                    m_markerContainerPanel.removeAllMarkers();

                }
            } else if (peptideMatchChanged && !proteinMatchChanged) {
            // search peptide
                // Select the Row
                int row = ((PeptideTableModel) (((CompoundTableModel) m_peptidesTable.getModel()).getBaseModel())).findRow(peptideMatch.getId());
                if (row != -1) {
                    row = ((CompoundTableModel) m_peptidesTable.getModel()).convertBaseModelRowToCompoundRow(row);
                }

                m_peptidesTable.getSelectionModel().setSelectionInterval(row, row);
                m_peptidesTable.scrollRowToVisible(row);
                
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

    
    private class PeptideTable extends DecoratedMarkerTable implements InfoInterface {
        
        public PeptideTable() {
            
        } 
        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
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
            m_popupMenu = new DisplayTablePopupMenu(RsmPeptidesOfProteinPanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
            return count+((count>1) ? " Peptides" : " Peptide");
        }

        
       
    }
    
    

    
    
    
}
