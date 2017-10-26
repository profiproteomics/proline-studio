package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.rsmexplorer.gui.dialog.CalcDialog;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.DefaultFloatingPanel;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayIdentificationProteinSetsAction;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.table.CustomColumnControlButton;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.windows.WindowManager;
import fr.proline.studio.rsmexplorer.actions.xic.*;
import fr.proline.studio.pattern.xic.*;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.xic.DatabaseModifyPeptideTask;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.DataBoxViewerManager;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.utils.ResultCallback;
import java.util.Map;
import javax.swing.Icon;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class XicProteinSetPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    public final static int NB_MAX_COLUMN_CONTROL = 20;
    private AbstractDataBox m_dataBox;

    private JScrollPane m_proteinSetScrollPane;
    private QuantProteinSetTable m_quantProteinSetTable;

    private DefaultFloatingPanel m_refineProteinsPanel;
    
    private MarkerContainerPanel m_markerContainerPanel;
    private boolean m_isXICMode;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;

    private JButton m_calcButton;

    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;

    private static final String OVERVIEW_KEY = "OVERVIEW_KEY";    

    public XicProteinSetPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        final JPanel proteinSetPanel = createProteinSetPanel();

  
        
        ResultCallback resultCallback = new ResultCallback() {
            @Override
            public void run(boolean success) {
                m_refineProteinsPanel.actionFinished(success, success ? null : "Refine failed. Look to Tasks Log for more information.");

                if (success) {

                    final ArrayList<DMasterQuantProteinSet> masterQuantProteinSetModified = m_quantProteinSetTable.getModifiedQuantProteinSet();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                            if (!success) {
                                return; // should not happen
                            }

                            // propagate modifications to the previous views
                            DataBoxViewerManager.loadedDataModified(m_dataBox.getProjectId(), m_dataBox.getRsetId(), m_dataBox.getRsmId(), DMasterQuantProteinSet.class, masterQuantProteinSetModified, DataBoxViewerManager.REASON_PROTEINS_REFINED);
                        }
                    };

                    // ask asynchronous loading of data
                    DatabaseModifyPeptideTask task = new DatabaseModifyPeptideTask(callback);
                    
                    task.initRemovePeptideModifiedOnProtein(m_dataBox.getProjectId(), masterQuantProteinSetModified);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                }
                
            }
            
        };
        
        ActionListener refineAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataboxXicProteinSet databox = (DataboxXicProteinSet) m_dataBox;
                long projectId = m_dataBox.getProjectId();
                DDataset dataset = (DDataset) databox.getData(false, DDataset.class);
                
                boolean okCalled = ComputeQuantitationProfileAction.quantificationProfile(resultCallback, getX()+20, getY()+20, projectId, dataset, null);
                if (okCalled) {
                    m_refineProteinsPanel.actionStarted();
                }
            }
            
        };
        
        
        String[] actionText = {"Refine"};
        ActionListener[] actionListeners = { refineAction };
        Icon[] icons = { IconManager.getIcon(IconManager.IconType.REFINE) };
        
        m_refineProteinsPanel = new DefaultFloatingPanel("Proteins need to be refined : ", actionText, actionListeners, icons);
        
        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                proteinSetPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(proteinSetPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), new Integer(JLayeredPane.PALETTE_LAYER+1));  
        layeredPane.add(m_searchToggleButton.getSearchPanel(), new Integer(JLayeredPane.PALETTE_LAYER+2));   
        layeredPane.add(m_refineProteinsPanel, JLayeredPane.PALETTE_LAYER);  
        
        

    }

    private JPanel createProteinSetPanel() {

        JPanel proteinSetPanel = new JPanel();
        proteinSetPanel.setBounds(0, 0, 500, 400);
        proteinSetPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();

        proteinSetPanel.add(toolbar, BorderLayout.WEST);
        proteinSetPanel.add(internalPanel, BorderLayout.CENTER);

        return proteinSetPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_quantProteinSetTable, m_quantProteinSetTable, ((CompoundTableModel) m_quantProteinSetTable.getModel()));
        toolbar.add(m_searchToggleButton);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_quantProteinSetTable.getModel()), m_quantProteinSetTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_quantProteinSetTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);
                m_infoToggleButton.updateInfo();
            }

        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_quantProteinSetTable.getModel()), "Protein Sets", m_quantProteinSetTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);


        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_quantProteinSetTable.getModel())) {

            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getUserName(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i != null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
            }
        };
        
        toolbar.add(m_addCompareDataButton);

        
        m_infoToggleButton = new InfoToggleButton(m_quantProteinSetTable, m_quantProteinSetTable);
        toolbar.add(m_infoToggleButton);
        
        m_calcButton = new JButton(IconManager.getIcon(IconManager.IconType.CALCULATOR));
        m_calcButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CalcDialog dialog = CalcDialog.getCalcDialog(WindowManager.getDefault().getMainWindow(), m_quantProteinSetTable);
                dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);

            }

        });
        toolbar.add(m_calcButton);

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
        m_proteinSetScrollPane = new JScrollPane();

        m_quantProteinSetTable = new QuantProteinSetTable();
        m_quantProteinSetTable.setModel(new CompoundTableModel(new QuantProteinSetTableModel((LazyTable) m_quantProteinSetTable), true));
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_quantProteinSetTable);
        m_quantProteinSetTable.setColumnControl(customColumnControl);
        // hide the id column
        m_quantProteinSetTable.getColumnExt(m_quantProteinSetTable.convertColumnIndexToView(QuantProteinSetTableModel.COLTYPE_PROTEIN_SET_ID)).setVisible(false);

        m_quantProteinSetTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_proteinSetScrollPane, m_quantProteinSetTable);

        m_proteinSetScrollPane.setViewportView(m_quantProteinSetTable);
        m_quantProteinSetTable.setFillsViewportHeight(true);
        m_quantProteinSetTable.setViewport(m_proteinSetScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;
    }

    public void setData(Long taskId, DQuantitationChannel[] quantChannels, List<DMasterQuantProteinSet> proteinSets, boolean isXICMode, boolean finished) {

        m_isXICMode = isXICMode;
        ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).setData(taskId, quantChannels, proteinSets, isXICMode);
        URLCellRenderer renderer = (URLCellRenderer) ((CompoundTableModel)m_quantProteinSetTable.getModel()).getRenderer(0, QuantProteinSetTableModel.COLTYPE_PROTEIN_SET_NAME);
        m_quantProteinSetTable.addMouseListener(renderer);
        m_quantProteinSetTable.addMouseMotionListener(renderer);
        
        // select the first row
        if ((proteinSets != null) && (proteinSets.size() > 0)) {
            m_quantProteinSetTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(proteinSets.size());

            if (m_hideFirstTime) {
                // allow to change column visibility
                //m_columnVisibilityButton.setEnabled(true);
                m_quantProteinSetTable.setSortable(true);
                // hide the rawAbundance  and selectionLevel columns
                List<Integer> listIdsToHide = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
                for (Integer id : listIdsToHide) {
                    m_quantProteinSetTable.getColumnExt(m_quantProteinSetTable.convertColumnIndexToView(id)).setVisible(false);
                }
                // hide Id column
                m_quantProteinSetTable.getColumnExt(m_quantProteinSetTable.convertColumnIndexToView(QuantProteinSetTableModel.COLTYPE_PROTEIN_SET_ID)).setVisible(false);
                m_hideFirstTime = false;
            }
            
            // check if refine panel must be shown
            try {
                for (int i = 0; i < proteinSets.size(); i++) {
                    DMasterQuantProteinSet masterQuantProteinSet = proteinSets.get(i);
                    Map<String, Object> pmqSerializedMap = masterQuantProteinSet.getSerializedPropertiesAsMap();
                    if ((pmqSerializedMap != null) && (pmqSerializedMap.containsKey(DMasterQuantProteinSet.MASTER_QUANT_PROTEINSET_WITH_PEPTIDE_MODIFIED))) {
                        // we must show refine panel
                        m_refineProteinsPanel.setLocation(getX() + 20, getY() + 20);
                        m_refineProteinsPanel.setVisible(true);
                        break;
                    }


                }
            } catch (Exception e) {
                // should never happen
            }
        }

    }
    private boolean m_hideFirstTime = true;

    public void dataModified(ArrayList modificationsList, int reason) {

        boolean modification = m_quantProteinSetTable.dataModified(modificationsList, reason);
        
        if (modification) {
            if (reason == DataBoxViewerManager.REASON_PEPTIDE_SUPPRESSED) {
                m_refineProteinsPanel.setLocation(getX() + 20, getY() + 20);
                m_refineProteinsPanel.setVisible(true);
            }

        }
    }
    
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_quantProteinSetTable.dataUpdated(subTask, finished);
        if (m_hideFirstTime) {
            // allow to change column visibility
            //m_columnVisibilityButton.setEnabled(true);
            m_quantProteinSetTable.setSortable(true);
            // hide the rawAbundance  and selectionLevel columns
            List<Integer> listIdsToHide = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
            for (Integer id : listIdsToHide) {
                m_quantProteinSetTable.getColumnExt(m_quantProteinSetTable.convertColumnIndexToView(id)).setVisible(false);
            }
            // hide Id column
            m_quantProteinSetTable.getColumnExt(m_quantProteinSetTable.convertColumnIndexToView(QuantProteinSetTableModel.COLTYPE_PROTEIN_SET_ID)).setVisible(false);
            m_hideFirstTime = false;
        }
    }

    public DProteinSet getSelectedProteinSet() {
        return m_quantProteinSetTable.getSelectedProteinSet();
    }

    public DMasterQuantProteinSet getSelectedMasterQuantProteinSet() {
        return m_quantProteinSetTable.getSelectedMasterQuantProteinSet();
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
        return (GlobalTableModelInterface) m_quantProteinSetTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_quantProteinSetTable;
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
        return m_quantProteinSetTable;
    }

    private class QuantProteinSetTable extends LazyTable implements ExportTableSelectionInterface, ExportModelInterface, InfoInterface {

        private ObjectParameter m_overviewParameter = null;
        
        public QuantProteinSetTable() {
            super(m_proteinSetScrollPane.getVerticalScrollBar());
        }

        public ArrayList<DMasterQuantProteinSet> getModifiedQuantProteinSet() {
            return ((QuantProteinSetTableModel) (((CompoundTableModel) getModel()).getBaseModel())).getModifiedQuantProteinSet();
        }
        
        @Override
        public ArrayList<ParameterList> getParameters() {
            ArrayList<ParameterList>  parameterListArray = super.getParameters();

            ParameterList overviewParameterList = new ParameterList("Overview Parameters");
            
            String[] overviewDisplay = { m_isXICMode ? "Overview on Pep. Match Count" : "Overview on Basic SC", m_isXICMode ? "Overview on Raw Abundance" : "Overview on Specific SC", m_isXICMode ? "Overview on Abundance" : "Overview on Weighted SC" };
            Integer[] overviewValues = { 0, 1, 2 };
            
            List<TableColumn> columns = getColumns(true);
            QuantProteinSetTableModel quantProteinSetTableModel = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel());
            int overviewType = quantProteinSetTableModel.getOverviewType();
            boolean overviewColumnVisible = ((TableColumnExt) columns.get(QuantProteinSetTableModel.COLTYPE_OVERVIEW)).isVisible();
            
            int defaultIndex = 0;
            
            if (!overviewColumnVisible) {
                defaultIndex = 0;
            } else {
                switch (overviewType) {
                    case QuantProteinSetTableModel.COLTYPE_ABUNDANCE:
                        defaultIndex = 2;
                        break;
                    case QuantProteinSetTableModel.COLTYPE_RAW_ABUNDANCE:
                        defaultIndex = 1;
                        break;
                    case QuantProteinSetTableModel.COLTYPE_PSM:
                        defaultIndex = 0;
                        break;

                }
            }

            m_overviewParameter = new ObjectParameter(OVERVIEW_KEY, "Overview", null, overviewDisplay, overviewValues, defaultIndex, null);
            overviewParameterList.add(m_overviewParameter);
            
            parameterListArray.add(overviewParameterList);
            
            return parameterListArray;
        }
               
        @Override
        public void parametersChanged() {
            super.parametersChanged();
            
            // parametersChanged() can be call soon, and so parameters could be not initialized
            if (m_overviewParameter == null) {
                return;
            }

            Integer index = (Integer) m_overviewParameter.getAssociatedObjectValue();
            
            QuantProteinSetTableModel model = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel());

            if (index == 2) {
                model.setOverviewType(QuantProteinSetTableModel.COLTYPE_ABUNDANCE);
            } else if (index == 1) {
                model.setOverviewType(QuantProteinSetTableModel.COLTYPE_RAW_ABUNDANCE);
            } else if (index == 0) {
                model.setOverviewType(QuantProteinSetTableModel.COLTYPE_PSM);
            } 
   
        }
        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

        /*@Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();

            m_idProteinSetAction = new DisplayIdentificationProteinSetsAction();
            popupMenu.addAction(m_idProteinSetAction);
            popupMenu.addAction(null);
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
        }*/
        private DisplayIdentificationProteinSetsAction m_idProteinSetAction;

        /*@Override
        public void prepostPopupMenu() {
            m_idProteinSetAction.setBox(m_dataBox);
        }*/

        
        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(XicProteinSetPanel.this);

            m_idProteinSetAction = new DisplayIdentificationProteinSetsAction();
            m_popupMenu.addAction(m_idProteinSetAction);
            
            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_idProteinSetAction.setBox(m_dataBox);
            m_popupMenu.prepostPopupMenu();
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

            m_dataBox.propagateDataChanged(DProteinSet.class);
            m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);

        }

        @Override
        public HashSet exportSelection(int[] rows) {
            CompoundTableModel tableModel = (CompoundTableModel) getModel();
            int[] modelRows = new int[rows.length];
            for (int i = 0; i < rows.length; i++) {
                int compoundModelRow = convertRowIndexToModel(rows[i]);
                modelRows[i] = tableModel.convertRowToOriginalModel(compoundModelRow);
            }

            QuantProteinSetTableModel baseModel = (QuantProteinSetTableModel) tableModel.getBaseModel();
            return baseModel.exportSelection(modelRows);
        }

        public DProteinSet getSelectedProteinSet() {

            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }

            CompoundTableModel compoundTableModel = (CompoundTableModel) getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            selectedRow = convertRowIndexToModel(selectedRow);
            selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

            // Retrieve PeptideIon selected
            QuantProteinSetTableModel tableModel = (QuantProteinSetTableModel) compoundTableModel.getBaseModel();
            return tableModel.getProteinSet(selectedRow);

        }

        public DMasterQuantProteinSet getSelectedMasterQuantProteinSet() {

            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }

            CompoundTableModel compoundTableModel = (CompoundTableModel) getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            selectedRow = convertRowIndexToModel(selectedRow);
            selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

            // Retrieve PeptideIon selected
            QuantProteinSetTableModel tableModel = (QuantProteinSetTableModel) compoundTableModel.getBaseModel();
            return tableModel.getMasterQuantProteinSet(selectedRow);

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
                    ((QuantProteinSetTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
                    
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
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
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
        
        
        public boolean dataModified(ArrayList modificationsList, int reason) {

            boolean modified = false;

            LastAction keepLastAction = m_lastAction;
            try {

                // retrieve selected row
                int rowSelected = getSelectionModel().getMinSelectionIndex();
                int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

                // Update Model (but protein set table must not react to the model update)
                selectionWillBeRestored(true);
                try {
                    modified = ((QuantProteinSetTableModel) ((CompoundTableModel) m_quantProteinSetTable.getModel()).getBaseModel()).dataModified(modificationsList, reason);
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
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING))) {
                        scrollRowToVisible(rowSelectedInView);
                    }

                }

            } finally {

                m_lastAction = keepLastAction;

            }

            return modified;
            
        }
        

        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }
        private boolean selectionWillBeRestored = false;

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_quantProteinSetTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) m_quantProteinSetTable.getModel()).getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((CompoundTableModel) m_quantProteinSetTable.getModel()).getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }
        
        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
            return count+((count>1) ? " Proteins Sets" : " Protein Set");
        }

    }


}
