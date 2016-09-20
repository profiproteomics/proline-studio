package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.pattern.MsQueryInfoRSM;
import fr.proline.studio.pattern.MsQueryInfoRset;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.MSQueriesTableModel;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.CustomColumnControlButton;
import fr.proline.studio.table.GlobalTableModelInterface;
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
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;

/**
 * panel to display MSQueries 
 * @author MB243701
 */
public class MSQueriesPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface{

    private AbstractDataBox m_dataBox;
    
    private MSQueriesTable m_msqueriesTable;
    private JScrollPane m_scrollPane;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    
    private JLabel m_titleLabel;
    private final String TABLE_TITLE = "MS Queries";
    
    private boolean m_fromRSM;
    
    
    public MSQueriesPanel(boolean fromRSm) {
        m_fromRSM = fromRSm;
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());

        final JPanel msqueryPanel = createMSQueryPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                msqueryPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(msqueryPanel, JLayeredPane.DEFAULT_LAYER);
    }
    
    private JPanel createMSQueryPanel() {

        JPanel msqueryPanel = new JPanel();
        msqueryPanel.setBounds(0, 0, 500, 400);
        msqueryPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        m_titleLabel = new JLabel(TABLE_TITLE);
        msqueryPanel.add(m_titleLabel, BorderLayout.NORTH);
        msqueryPanel.add(toolbar, BorderLayout.WEST);
        msqueryPanel.add(internalPanel, BorderLayout.CENTER);

        return msqueryPanel;
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_msqueriesTable.getModel()), m_msqueriesTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_msqueriesTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_msqueriesTable.getModel()), "MsQueries", m_msqueriesTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        
        
        
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_msqueriesTable.getModel())) {
           
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
        toolbar.add(m_addCompareDataButton);
        
        return toolbar;
    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // create objects
        m_scrollPane = new JScrollPane();
        
        m_msqueriesTable = new MSQueriesTable();
        m_msqueriesTable.setModel(new CompoundTableModel(new MSQueriesTableModel((LazyTable)m_msqueriesTable, m_fromRSM), true));
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_msqueriesTable);
        m_msqueriesTable.setColumnControl(customColumnControl);
        // hide the id column
        m_msqueriesTable.getColumnExt(m_msqueriesTable.convertColumnIndexToView(MSQueriesTableModel.COLTYPE_MSQUERY_ID)).setVisible(false);
       
        m_msqueriesTable.setSortable(false);
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane,  (MSQueriesTable) m_msqueriesTable);
        
        m_scrollPane.setViewportView(m_msqueriesTable);
        m_msqueriesTable.setFillsViewportHeight(true);
        m_msqueriesTable.setViewport(m_scrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        
        return internalPanel;
    }               
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_msqueriesTable.dataUpdated(subTask, finished);
        if (finished) {
            m_msqueriesTable.setSortable(true);
        }
    }
    
    public void setData(Long taskId, List<DMsQuery> msQueries, Map<Long, Integer> nbPeptideMatchesByMsQueryIdMap, boolean finished) {
        ((MSQueriesTableModel) ((CompoundTableModel) m_msqueriesTable.getModel()).getBaseModel()).setData(taskId, msQueries, nbPeptideMatchesByMsQueryIdMap);
        int nbQ = msQueries.size();
        m_titleLabel.setText(TABLE_TITLE +" ("+msQueries.size()+")");
        m_msqueriesTable.getColumnExt(m_msqueriesTable.convertColumnIndexToView(MSQueriesTableModel.COLTYPE_MSQUERY_ID)).setVisible(false);
        //m_msqueriesTable.getColumnExt(m_msqueriesTable.convertColumnIndexToView(MSQueriesTableModel.COLTYPE_MSQUERY_ID)).setVisible(false);
        m_markerContainerPanel.setMaxLineNumber(nbQ);
        // select the first row
        if ((nbQ > 0)) {
            m_msqueriesTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        if (finished) {
            m_msqueriesTable.setSortable(true);
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
    public void addSingleValue(Object v) {
        getGlobalTableModelInterface().addSingleValue(v);
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_msqueriesTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
         return m_msqueriesTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_msqueriesTable;
    }
    
    public DMsQuery getSelectedMsQuery(){
        MSQueriesTable table = ((MSQueriesTable) m_msqueriesTable);

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);

        CompoundTableModel compoundTableModel = ((CompoundTableModel)table.getModel());
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

        // Retrieve ProteinSet selected
        MSQueriesTableModel tableModel = (MSQueriesTableModel) compoundTableModel.getBaseModel();
        return tableModel.getSelectedMsQuery(selectedRow);
    }
    
    
    
    
    private class MSQueriesTable extends LazyTable {
        
        private boolean selectionWillBeRestored = false;
        
        public MSQueriesTable() {
            super(m_scrollPane.getVerticalScrollBar());
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(MSQueriesPanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
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
                ((MSQueriesTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
        
    
        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        
        /** 
         * Called whenever the value of the selection changes.
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            
            super.valueChanged(e);
            
            if (selectionWillBeRestored) {
                return;
            }
            
            if (m_fromRSM){
                m_dataBox.propagateDataChanged(MsQueryInfoRSM.class);
            }else{
                m_dataBox.propagateDataChanged(MsQueryInfoRset.class);
            }

        }
        
    }
    
    
    
}
