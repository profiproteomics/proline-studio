/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.dto.DFeature;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.GetXICChromatogramTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.mzscope.AddMzScopeButton;
import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.MzScopeWindowBoxManager;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.WindowManager;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.pattern.xic.DataboxChildFeature;
import fr.proline.studio.table.AbstractTableAction;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JTable;

/**
 *
 * @author JM235353
 */
public class XicFeaturePanel  extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_featureScrollPane;
    private FeatureTable m_featureTable;

    private MarkerContainerPanel m_markerContainerPanel;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    private JButton m_graphicsTypeButton;
    private AddMzScopeButton m_mzscopeButton;

    private ExctractXICAction m_extractSelectedXICAction = null;
    private ExctractXICAction m_extractAlldXICAction = null;
    
    public static final int VIEW_ALL_GRAPH_PEAKS = 0;
    public static final int VIEW_ALL_ISOTOPES_FOR_FEATURE = 1;

    private int m_viewType = 0;

    private final boolean m_canGraph;

    private JLabel m_titleLabel;
    private final static String TABLE_TITLE = "Features";
    
    private int m_loadingXICId = 0;
    
    public XicFeaturePanel(boolean canGraph) {
        this.m_canGraph = canGraph ;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());

       final JPanel featurePanel = createFeaturePanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                featurePanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(featurePanel, JLayeredPane.DEFAULT_LAYER);


    }
        
    private JPanel createFeaturePanel() {

        JPanel featurePanel = new JPanel();
        featurePanel.setBounds(0, 0, 500, 400);
        featurePanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        m_titleLabel = new JLabel(TABLE_TITLE);
        featurePanel.add(m_titleLabel, BorderLayout.NORTH);
        featurePanel.add(toolbar, BorderLayout.WEST);
        featurePanel.add(internalPanel, BorderLayout.CENTER);

        return featurePanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
       
        m_settingsButton = new SettingsButton(((ProgressInterface) m_featureTable.getModel()), m_featureTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_featureTable.getModel())) {

            @Override
            protected void filteringDone() {
                 m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);
            }
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_featureTable.getModel()), "Features", m_featureTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        
        // graphics button
        m_graphicsButton = new JButton(IconManager.getIcon(IconManager.IconType.CHART));
        m_graphicsButton.setToolTipText("Graphics : Linear Plot");
        m_graphicsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!((CompoundTableModel) m_featureTable.getModel()).isLoaded()) {

                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), ((CompoundTableModel) m_featureTable.getModel()), "Data loading", "Histogram functionnality is not available while data is loading. Please Wait.");
                    dialog.setLocation(getLocationOnScreen().x + m_graphicsButton.getWidth() + 5, m_graphicsButton.getLocationOnScreen().y + getHeight() + 5);
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        return;
                    }
                }
                // prepare window box
                WindowBox wbox = WindowBoxFactory.getMultiGraphicsWindowBox("Feature Graphic", m_dataBox, false);
                wbox.setEntryData(m_dataBox.getProjectId(), m_dataBox.getData(false, List.class));

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive();
            }
        });
        
        // graphic type: all peaks isotope0 from all features or all peaks from the selected feature
        m_graphicsTypeButton = new JButton(IconManager.getIcon(IconManager.IconType.CHART_ARROW));
        setGraphicTypeToolTip();
        m_graphicsTypeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch(m_viewType) {
                    case VIEW_ALL_GRAPH_PEAKS: {
                        m_viewType = VIEW_ALL_ISOTOPES_FOR_FEATURE ;
                        break;
                    }
                    case VIEW_ALL_ISOTOPES_FOR_FEATURE: {
                        m_viewType = VIEW_ALL_GRAPH_PEAKS ;
                        break;
                    }
                }
                setGraphicTypeToolTip();
                m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);
            }
        });
        if(m_canGraph) {
            toolbar.add(m_graphicsButton);
            toolbar.add(m_graphicsTypeButton);
        }
        
        // mzscope
        m_mzscopeButton = new AddMzScopeButton(((CompoundTableModel) m_featureTable.getModel()), (FeatureTableModel) ((CompoundTableModel)m_featureTable.getModel()).getBaseModel()) {
            @Override
            public void actionPerformed(MzScopeInterface mzScopeInterface) {
                if (!((CompoundTableModel) m_featureTable.getModel()).isLoaded()) {

                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), ((CompoundTableModel) m_featureTable.getModel()), "Data loading", "Histogram functionnality is not available while data is loading. Please Wait.");
                    dialog.setLocation(getLocationOnScreen().x + m_mzscopeButton.getWidth() + 5, m_mzscopeButton.getLocationOnScreen().y + getHeight() + 5);
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        return;
                    }
                }
                MzScopeInterface mzscopeInterface = (MzScopeInterface)m_dataBox.getData(false, MzScopeInterface.class);
                MzScopeWindowBoxManager.addMzdbScope(mzscopeInterface);
                
            }
        };
        toolbar.add(m_mzscopeButton);
        
        
        return toolbar;
    }
    
    private void setGraphicTypeToolTip() {
        String tooltipText = "";
        switch (m_viewType) {
            case VIEW_ALL_GRAPH_PEAKS: {
                tooltipText = "Display all isotopes for the selected feature";
                break;
            }
            case VIEW_ALL_ISOTOPES_FOR_FEATURE: {
                tooltipText = "Display peaks for this map";
                break;
            }
        }
        m_graphicsTypeButton.setToolTipText(tooltipText);
    }
    
    public int getGraphViewType() {
        return m_viewType;
    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // create objects
        m_featureScrollPane = new JScrollPane();
        
        m_featureTable = new FeatureTable();
        m_featureTable.setModel(new CompoundTableModel(new FeatureTableModel((LazyTable)m_featureTable), true));
        
        
        // hide the columns
        List<Integer> listIdsToHide = ((FeatureTableModel) ((CompoundTableModel) m_featureTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
        for (Integer id : listIdsToHide) {
            m_featureTable.getColumnExt(m_featureTable.convertColumnIndexToView(id.intValue())).setVisible(false);
        }
        // hide the id column
        m_featureTable.getColumnExt(m_featureTable.convertColumnIndexToView(FeatureTableModel.COLTYPE_FEATURE_ID)).setVisible(false);
        
        m_featureTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_featureScrollPane, m_featureTable);
        
        m_featureScrollPane.setViewportView(m_featureTable);
        m_featureTable.setFillsViewportHeight(true);
        m_featureTable.setViewport(m_featureScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        return internalPanel;
    }                 
    
    public void setData(Long taskId,  List<DFeature> features, QuantChannelInfo quantChannelInfo, List<Boolean> featureHasPeak, boolean finished) {
        ((FeatureTableModel) ((CompoundTableModel) m_featureTable.getModel()).getBaseModel()).setData(taskId,  features, quantChannelInfo, featureHasPeak);
        //m_featureTable.setColumnControlVisible(((FeatureTableModel) ((CompoundTableModel) m_featureTable.getModel()).getBaseModel()).getColumnCount() < XicProteinSetPanel.NB_MAX_COLUMN_CONTROL);     
        m_titleLabel.setText(TABLE_TITLE +" ("+features.size()+")");
        // select the first row
        if ((features.size() > 0)) {
            m_featureTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(features.size());
        }
        if (finished) {
            m_featureTable.setSortable(true);
        }
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_featureTable.dataUpdated(subTask, finished);
        if (finished) {
            m_featureTable.setSortable(true);
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
        return (GlobalTableModelInterface) m_featureTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_featureTable;
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

    public DFeature getSelectedFeature() {
        return m_featureTable.getSelectedFeature();
    }
    
    public List<DFeature> getSelectedFeatures() {
        return m_featureTable.getSelectedFeatures();
    }
    
    public Color getPlotColor() {
        return m_featureTable.getPlotColor();
    }
    
    public String getPlotTitle() {
        return m_featureTable.getPlotTitle();
    }
    

    public MzScopeInterface getMzScopeInterface() {
        return (FeatureTableModel) ((CompoundTableModel)m_featureTable.getModel()).getBaseModel();
    }
    
    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_featureTable;
    }
    
    private class FeatureTable extends LazyTable implements ExportModelInterface  {

        
        public FeatureTable() {
            super(m_featureScrollPane.getVerticalScrollBar() );
            
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
            
            if (selectionWillBeRestored) {
                return;
            }
 
            m_dataBox.propagateDataChanged(Feature.class);
            m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);

        }
        
        public boolean selectFeature(Long featureId, String searchText) {

            FeatureTableModel tableModel = (FeatureTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(featureId);
            if (row == -1) {
                return false;
            }
            row = ((CompoundTableModel)getModel()).convertBaseModelRowToCompoundRow(row);
            if (row == -1) {
                return false;
            }
            
            // JPM.hack we need to keep the search text
            // to be able to give it if needed to the panel
            // which display proteins of a protein set
            searchTextBeingDone = searchText;
            
            // must convert row index if there is a sorting
            row = convertRowIndexToView(row);
            
            // select the row
            getSelectionModel().setSelectionInterval(row, row);
            
            // scroll to the row
            scrollRowToVisible(row);

            searchTextBeingDone = null;
            
            return true;
        }
        String searchTextBeingDone = null;

        public void dataUpdated(SubTask subTask, boolean finished) {
            
            LastAction keepLastAction = m_lastAction;
            try {
            
            
            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((FeatureTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
        
        @Override
        public void sortingChanged(int col) {
           
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

        public DFeature getSelectedFeature() {
            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }
            
            return getFeature(selectedRow);
        }
        private DFeature getFeature(int row) {

            CompoundTableModel compoundTableModel = (CompoundTableModel) getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            row = convertRowIndexToModel(row);
            row = compoundTableModel.convertCompoundRowToBaseModelRow(row);

            // Retrieve PeptideIon selected
            FeatureTableModel tableModel = (FeatureTableModel) compoundTableModel.getBaseModel();
            return tableModel.getFeature(row);

        }
        
        public ArrayList<DFeature> getSelectedFeatures() {

            int[] selectedRows = getSelectedRows();

            // nothing selected
            if ((selectedRows == null) || (selectedRows.length == 0)) {
                return null;

            }

            ArrayList<DFeature> features = new ArrayList<DFeature>(selectedRows.length);
            for (int row : selectedRows) {
                features.add(getFeature(row));
            }

            return features;
        }
        
        public Color getPlotColor() {
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
            FeatureTableModel tableModel = (FeatureTableModel) compoundTableModel.getBaseModel();
            return tableModel.getPlotColor(selectedRow);

        }
        
        public String getPlotTitle() {
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
            FeatureTableModel tableModel = (FeatureTableModel) compoundTableModel.getBaseModel();
            return tableModel.getPlotTitle(selectedRow);

        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_featureTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }
        
        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) m_featureTable.getModel()).getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((CompoundTableModel) m_featureTable.getModel()).getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }
        

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(XicFeaturePanel.this);

            m_extractSelectedXICAction = new ExctractXICAction(false);
            m_extractAlldXICAction = new ExctractXICAction(true);
            
            m_popupMenu.addAction(null);
            m_popupMenu.addAction(m_extractSelectedXICAction);
            m_popupMenu.addAction(m_extractAlldXICAction);
            
            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            
            m_extractSelectedXICAction.setBox(m_dataBox);
            m_extractAlldXICAction.setBox(m_dataBox);
            
            m_popupMenu.prepostPopupMenu();
        }
        

        
    }
    
    public class ExctractXICAction extends AbstractTableAction {

        private AbstractDataBox m_box;

        private boolean m_all;

        public ExctractXICAction(boolean all) {
            super(all ? "Extract All XIC" : "Extract Selected XIC");
            m_all = all;
        }

        public void setBox(AbstractDataBox box) {
            m_box = box;
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {

            if (m_box == null) {
                return; // should not happen
            }

            final DataboxChildFeature databoxChildFeature = (DataboxChildFeature) m_box;
            QuantChannelInfo quantChannelInfo = (QuantChannelInfo) databoxChildFeature.getData(false, QuantChannelInfo.class);
            DQuantitationChannel[] qChannels = quantChannelInfo.getQuantChannels();
            
            HashMap<Long, DQuantitationChannel> qChannelMap = new HashMap<>();
            
            for (DQuantitationChannel qChannel : qChannels) {
                qChannelMap.put(qChannel.getId(), qChannel);
            }
            
            List<DFeature> featureList;
            if (!m_all) {
                featureList = getSelectedFeatures();
            } else {
                // all features
                featureList = (List<DFeature>) databoxChildFeature.getData(true, DFeature.class);
            }

            
            
            HashSet<String> identifierFound = new HashSet<>();

            int size = featureList.size();
            ArrayList<String> rawFileIdentifierList = new ArrayList<>(size);
            ArrayList<Double> mozList = new ArrayList<>(size);
            final ArrayList<DFeature> featuresSearched = new  ArrayList<>(size);
            
            
            for (int i = 0; i < size; i++) {
                DFeature feature = featureList.get(i);
                if (feature.getPeakArray() != null) {
                    // data already loaded
                    continue;
                }
                double moz = feature.getMoz();
                if (moz <= 1e-15) {
                    // moz is equal to 0, do not take it in account
                    continue;
                }

                
                String rawFileIdentifier = qChannelMap.get(feature.getQuantChannelId()).getRawFileIdentifier();

                // avoid to treat multiple time the same rawFile
                if (identifierFound.contains(rawFileIdentifier)) {
                    continue;
                }
                identifierFound.add(rawFileIdentifier);

                rawFileIdentifierList.add(rawFileIdentifier);
                mozList.add(moz);
                featuresSearched.add(feature);
            }

            
            if (featuresSearched.isEmpty()) {
                // nothing to do, data already loaded
                return;
            }
            
            double ppm = 5; // tolerance set to 5ppm, could be changed


            final int loadingId = m_loadingXICId++;
            setLoading(loadingId);
            
            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {
                    if (success) {

                        databoxChildFeature.setRetrievedXic(featuresSearched);

                    } else {
                        // we could manage error with errorMessage

                    }
                    setLoaded(loadingId);
                }
            };

            // use canonicalPath when it is possible to be sure to have an unique path
            GetXICChromatogramTask task = new GetXICChromatogramTask(callback, featuresSearched, rawFileIdentifierList, mozList, ppm);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

        }
        

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(m_all || ((selectedRows != null) && (selectedRows.length > 0)));
        }

    }
    
}
