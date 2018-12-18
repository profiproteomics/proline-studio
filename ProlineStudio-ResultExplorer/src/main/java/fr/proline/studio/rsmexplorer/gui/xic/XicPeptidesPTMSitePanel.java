package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.extendedtablemodel.JoinDataModel;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.PeptidesOfPTMSiteTableModel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Comparator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author VD225637
 */
public class XicPeptidesPTMSitePanel extends JPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {
    private AbstractDataBox m_dataBox;
    
    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;

    private JScrollPane m_peptidesPtmSiteScrollPane;
    private XicPeptidesPTMSiteTable m_peptidesPtmSiteTable; 
    private MarkerContainerPanel m_markerContainerPanel;
    
    public XicPeptidesPTMSitePanel(){
        initComponents();
    }
    
    private void initComponents() {

        setLayout(new BorderLayout());

        final JPanel peptidesPTMSitePanel = createPeptidesPTMSitePanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptidesPTMSitePanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peptidesPTMSitePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), new Integer(JLayeredPane.PALETTE_LAYER + 1));

    }
    
 private JPanel createPeptidesPTMSitePanel() {

        JPanel peptidesPTMSitePanel = new JPanel();
        peptidesPTMSitePanel.setBounds(0, 0, 500, 400);
        peptidesPTMSitePanel.setLayout(new BorderLayout());

        //--- Create Internal Pane
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        m_peptidesPtmSiteScrollPane = new JScrollPane();

        m_peptidesPtmSiteTable = new XicPeptidesPTMSiteTable(); 
        PeptidesOfPTMSiteTableModel model1 = new PeptidesOfPTMSiteTableModel();
        QuantPeptideInstTableModel model2 = new QuantPeptideInstTableModel(m_peptidesPtmSiteTable, true);
        JoinDataModel joinedModel = new JoinDataModel();
        joinedModel.setData(model1, model2, 0, 0, 0d , 0, 0, 0d, Boolean.FALSE);
        m_peptidesPtmSiteTable.setModel(new CompoundTableModel(joinedModel, true));

        // hide the id column
        m_peptidesPtmSiteTable.getColumnExt(m_peptidesPtmSiteTable.convertColumnIndexToView(PeptidesOfPTMSiteTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_peptidesPtmSiteTable.getModel());
        m_peptidesPtmSiteTable.setRowSorter(sorter);

        sorter.setComparator(PeptidesOfPTMSiteTableModel.COLTYPE_MODIFICATION_LOC, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                int pos1;
                if (s1.compareTo("N-term") == 0) {
                    pos1 = -1;
                } else if (s1.compareTo("C-term") == 0) {
                    pos1 = Integer.MAX_VALUE;
                } else {
                    pos1 = Integer.valueOf(s1);
                }
                int pos2;
                if (s2.compareTo("N-term") == 0) {
                    pos2 = 0;
                } else if (s2.compareTo("C-term") == 0) {
                    pos2 = Integer.MAX_VALUE;
                } else {
                    pos2 = Integer.valueOf(s2);
                }

                return pos2 - pos1;
            }

        });

        sorter.setComparator(PeptidesOfPTMSiteTableModel.COLTYPE_PROTEIN_LOC, new Comparator<Integer>() {

            @Override
            public int compare(Integer s1, Integer s2) {
                return s1 - s2;
            }

        });

        m_markerContainerPanel = new MarkerContainerPanel(m_peptidesPtmSiteScrollPane, m_peptidesPtmSiteTable);

        m_peptidesPtmSiteScrollPane.setViewportView(m_peptidesPtmSiteTable);
        m_peptidesPtmSiteTable.setFillsViewportHeight(true);
        m_peptidesPtmSiteTable.setViewport(m_peptidesPtmSiteScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);



        JToolBar toolbar = initToolbar();
        peptidesPTMSitePanel.add(toolbar, BorderLayout.WEST);
        peptidesPTMSitePanel.add(internalPanel, BorderLayout.CENTER);

        return peptidesPTMSitePanel;
    }
 
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_exportButton = new ExportButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel()), "Peptides", m_peptidesPtmSiteTable);
        m_searchToggleButton = new SearchToggleButton(m_peptidesPtmSiteTable, m_peptidesPtmSiteTable, ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()));        
        m_settingsButton = new SettingsButton(((ProgressInterface) m_peptidesPtmSiteTable.getModel()), m_peptidesPtmSiteTable);
        m_infoToggleButton = new InfoToggleButton(m_peptidesPtmSiteTable, m_peptidesPtmSiteTable);
        m_filterButton = new FilterButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel())) {
            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);
                m_infoToggleButton.updateInfo();
            }
        };

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel())) {

            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getUserName(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i != null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataAnalyzerWindowBoxManager.addTableInfo(tableInfo);
            }
        };

        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        toolbar.add(m_infoToggleButton);

        return toolbar;
    }
    
    public DPeptideInstance getSelectedPeptideInstance() {
        JoinDataModel joinModel = ((JoinDataModel) ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()).getBaseModel());     
        QuantPeptideInstTableModel model2 = (QuantPeptideInstTableModel) joinModel.getSecondTableModel();
        
        int rowSelected = m_peptidesPtmSiteTable.getSelectionModel().getMinSelectionIndex();
        int rowSelectedInJoinModel = (rowSelected == -1) ? -1 : m_peptidesPtmSiteTable.convertRowIndexToModel(rowSelected);
        if (rowSelectedInJoinModel == -1) {
            return null;
        }    
        Integer rowInModel2 = joinModel.getRowInSecondModel(rowSelectedInJoinModel);
        if (rowInModel2 == null) {
            return null;
        }

        DMasterQuantPeptide masterQuantPeptide = model2.getPeptide(rowInModel2);
        return masterQuantPeptide.getPeptideInstance();

    }
    
    public void setData(PTMSite peptidesPTMSite) {
        setData(-1L, new DQuantitationChannel[0], null, peptidesPTMSite,  true);
    }
    
    public void setData(Long taskId,  DQuantitationChannel[] quantChannels, List<DMasterQuantPeptide> peptides,  PTMSite peptidesPTMSite, boolean finished) {

        JoinDataModel previousModel = ((JoinDataModel) ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()).getBaseModel());        
        PeptidesOfPTMSiteTableModel model1 = (PeptidesOfPTMSiteTableModel) previousModel.getFirstTableModel();
        QuantPeptideInstTableModel model2 = (QuantPeptideInstTableModel) previousModel.getSecondTableModel();
        model1.setData(peptidesPTMSite, false, null);
        model2.setData(taskId, -1L, quantChannels, peptides, finished);
        JoinDataModel joinedModel = new JoinDataModel();
        joinedModel.setData(model1, model2, PeptidesOfPTMSiteTableModel.COLTYPE_PEPTIDE_ID, QuantPeptideInstTableModel.COLTYPE_PEPTIDE_INST_ID, 0d , -1, -1, 0d, Boolean.FALSE);
        
        
        m_peptidesPtmSiteTable.setModel(new CompoundTableModel(joinedModel, true));
        
        // select the first row
        if (peptidesPTMSite != null) {
            m_peptidesPtmSiteTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(m_peptidesPtmSiteTable.getModel().getRowCount());
        }
        m_infoToggleButton.updateInfo();
        m_peptidesPtmSiteTable.setSortable(true);
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_peptidesPtmSiteTable.dataUpdated(subTask, finished);
        if (finished) {
            // allow to change column visibility            
            m_peptidesPtmSiteTable.setSortable(true);
        }
    }
    
    
    @Override
    public void addSingleValue(Object v) {
        getGlobalTableModelInterface().addSingleValue(v);
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        //TODO Set QuantPepTableModel DB
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {        
        return m_dataBox;
    }

    @Override
    public void setLoading(int id) {
        //Nothing to do
    }

    @Override
    public void setLoading(int id, boolean calculating) {
        //Nothing to Do
    }

    @Override
    public void setLoaded(int id) {
         //Nothing to Do
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
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_peptidesPtmSiteTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
         return m_peptidesPtmSiteTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peptidesPtmSiteTable;
    }

    private class XicPeptidesPTMSiteTable extends LazyTable implements   CrossSelectionInterface, InfoInterface, ProgressInterface {

        public XicPeptidesPTMSiteTable() {
            super(m_peptidesPtmSiteScrollPane.getVerticalScrollBar());
        }
    
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
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
            
            m_dataBox.propagateDataChanged(PTMSite.class);            

        }

        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }        
        private boolean selectionWillBeRestored = false;

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(XicPeptidesPTMSitePanel.this);
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
            StringBuilder sb = new StringBuilder(count);
            if(count > 1) {
                sb.append(" Peptides ");
            } else {
                sb.append(" Peptide ");
            }

            
            sb.append("for PTM Site");
            
            return sb.toString();
        }

        @Override
        public boolean isLoaded() {
            return true;            
        }

        @Override
        public int getLoadingPercentage() {
            return 100;            
        }
           
        public void dataUpdated(SubTask subTask, boolean finished) {
            
            LastAction keepLastAction = m_lastAction;
            try {
            
            
            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((QuantPeptideTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
                if (((keepLastAction == LastAction.ACTION_SELECTING ) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
                    scrollRowToVisible(rowSelectedInView);
                }
                    
            }

            } finally {

                m_lastAction = keepLastAction;
 
            }
            
            if (finished) {
                setSortable(true);
            }
        }


    }
}
