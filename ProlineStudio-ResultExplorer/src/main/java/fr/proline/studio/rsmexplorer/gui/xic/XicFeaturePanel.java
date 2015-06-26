package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.filter.FilterButtonV2;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.mzscope.AddMzScopeButton;
import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.MzScopeWindowBoxManager;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
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

/**
 *
 * @author JM235353
 */
public class XicFeaturePanel  extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_featureScrollPane;
    private FeatureTable m_featureTable;

    private MarkerContainerPanel m_markerContainerPanel;

    private FilterButtonV2 m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    private JButton m_graphicsTypeButton;
    private AddMzScopeButton m_mzscopeButton;

    public static final int VIEW_ALL_GRAPH_PEAKS = 0;
    public static final int VIEW_ALL_ISOTOPES_FOR_FEATURE = 1;

    private int m_viewType = 0;

    private final boolean m_canGraph;

    private JLabel m_titleLabel;
    private final static String TABLE_TITLE = "Features";
    
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
       
        m_filterButton = new FilterButtonV2(((CompoundTableModel) m_featureTable.getModel())) {

            @Override
            protected void filteringDone() {
                 m_dataBox.propagateDataChanged(CompareDataInterface.class);
            }
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_featureTable.getModel()), "Features", m_featureTable);

        toolbar.add(m_filterButton);
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
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
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
    
    public void setData(Long taskId,  List<Feature> features, QuantChannelInfo quantChannelInfo, List<Boolean> featureHasPeak, boolean finished) {
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

    public Feature getSelectedFeature() {
        return m_featureTable.getSelectedFeature();
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
            m_dataBox.propagateDataChanged(CompareDataInterface.class);

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


        public Feature getSelectedFeature() {

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
            return tableModel.getFeature(selectedRow);

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
        
        //Implement table cell tool tips.
//        @Override
//        public String getToolTipText(MouseEvent e) {
//            Point p = e.getPoint();
//            int rowIndex = rowAtPoint(p);
//            if (rowIndex<0) {
//                return null;
//            }
//            int colIndex = columnAtPoint(p);
//            if (colIndex<0) {
//                return null;
//            }
//            int realColumnIndex = convertColumnIndexToModel(colIndex);
//            int realRowIndex = convertRowIndexToModel(rowIndex);
//
//            return ((CompoundTableModel)getModel()).getTootlTipValue(realRowIndex, realColumnIndex);
//        }


        // called by super.DecoratedTable
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
