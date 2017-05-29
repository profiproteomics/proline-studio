package fr.proline.studio.rsmexplorer.gui.xic;


import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.AddDataAnalyzerButton;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.DefaultFloatingPanel;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataMixerWindowBoxManager;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.CustomColumnControlButton;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class XicPeptidePanel  extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_peptideScrollPane;
    private QuantPeptideTable m_quantPeptideTable;

    private MarkerContainerPanel m_markerContainerPanel;

    private DefaultFloatingPanel m_validateModificationsPanel;
    
    private boolean m_displayForProteinSet;
    private DQuantitationChannel[] m_quantChannels;
    private boolean m_isXICMode;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;


    private final boolean m_canGraph ;

    private static final String OVERVIEW_KEY = "OVERVIEW_KEY";  
    
    public XicPeptidePanel(boolean canGraph, boolean xicMode) {
        m_canGraph = canGraph ;
        initComponents(xicMode);
    }
    
    public void displayValidatePanel(boolean visible) {
        
        if (m_validateModificationsPanel.isVisible() ^ visible) {
            if (visible) {
                m_validateModificationsPanel.setLocation(getX() + 80, getY() + 20);
                m_validateModificationsPanel.setVisible(true);
            } else {
                m_validateModificationsPanel.setVisible(false);
            }
        }
        
        
    }
    
    private void initComponents(boolean xicMode) {
        setLayout(new BorderLayout());

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        
        final JPanel peptidePanel = createPeptidePanel(xicMode);

        
        ActionListener validateModificationsAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_validateModificationsPanel.actionStarted();
                m_quantPeptideTable.validateModifications();
            }

        };

        ActionListener cancelModificationsAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_validateModificationsPanel.actionStarted();
                m_quantPeptideTable.cancelModifications();
            }

        };
        
        String[] actionText = {"Validate", "Cancel"};
        ActionListener[] actionListeners = { validateModificationsAction, cancelModificationsAction };
        Icon[] icons = { IconManager.getIcon(IconManager.IconType.OK), IconManager.getIcon(IconManager.IconType.CANCEL) };
        
        m_validateModificationsPanel = new DefaultFloatingPanel("Validate Modifications : ", actionText, actionListeners, icons);

        
        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptidePanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peptidePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), new Integer(JLayeredPane.PALETTE_LAYER+1));  
        layeredPane.add(m_searchToggleButton.getSearchPanel(), new Integer(JLayeredPane.PALETTE_LAYER+2));  
        layeredPane.add(m_validateModificationsPanel, JLayeredPane.PALETTE_LAYER);  

    }
        
    private JPanel createPeptidePanel(boolean xicMode) {

        JPanel peptidePanel = new JPanel();
        peptidePanel.setBounds(0, 0, 500, 400);
        peptidePanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel(xicMode);

        JToolBar toolbar = initToolbar();
        peptidePanel.add(toolbar, BorderLayout.WEST);
        peptidePanel.add(internalPanel, BorderLayout.CENTER);

        return peptidePanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_quantPeptideTable, m_quantPeptideTable, ((CompoundTableModel) m_quantPeptideTable.getModel()));
        toolbar.add(m_searchToggleButton);
        
        m_settingsButton = new SettingsButton(((ProgressInterface) m_quantPeptideTable.getModel()), m_quantPeptideTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_quantPeptideTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(CompareDataInterface.class);
                m_infoToggleButton.updateInfo();
            }
            
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_quantPeptideTable.getModel()), "Peptides", m_quantPeptideTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

        
        // graphics button
        m_graphicsButton = new JButton(IconManager.getIcon(IconManager.IconType.CHART));
        m_graphicsButton.setToolTipText("Graphics : Linear Plot");
        m_graphicsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!((CompoundTableModel) m_quantPeptideTable.getModel()).isLoaded()) {

                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), ((CompoundTableModel) m_quantPeptideTable.getModel()), "Data loading", "Histogram functionnality is not available while data is loading. Please Wait.");
                    dialog.setLocation(getLocationOnScreen().x + m_graphicsButton.getWidth() + 5, m_graphicsButton.getLocationOnScreen().y + getHeight() + 5);
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        return;
                    }
                }
                // prepare window box
                WindowBox wbox = WindowBoxFactory.getMultiGraphicsWindowBox("Peptide Graphic", m_dataBox, false);
                wbox.setEntryData(m_dataBox.getProjectId(), m_dataBox.getData(false, List.class));

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive();
            }
        });
        if (m_canGraph){
            toolbar.add(m_graphicsButton);
        }
        
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_quantPeptideTable.getModel())) {
            
            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getUserName(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i!=null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataMixerWindowBoxManager.addTableInfo(tableInfo);
            }
        };
        toolbar.add(m_addCompareDataButton);
        
        m_infoToggleButton = new InfoToggleButton(m_quantPeptideTable, m_quantPeptideTable);
        toolbar.add(m_infoToggleButton);
        
        
        return toolbar;
    }
    
    private JPanel createInternalPanel(boolean xicMode) {

        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // create objects
        m_peptideScrollPane = new JScrollPane();
        
        m_quantPeptideTable = new QuantPeptideTable();
        QuantPeptideTableModel baseModel = new QuantPeptideTableModel((LazyTable) m_quantPeptideTable, xicMode);
        m_quantPeptideTable.setModel(new CompoundTableModel(baseModel, true));
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_quantPeptideTable);
        m_quantPeptideTable.setColumnControl(customColumnControl);
        // hide the id column
        m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        if (!xicMode) {
            m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL)).setVisible(false);
        }
        
        m_quantPeptideTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_peptideScrollPane, m_quantPeptideTable);
        
        m_peptideScrollPane.setViewportView(m_quantPeptideTable);
        m_quantPeptideTable.setFillsViewportHeight(true);
        m_quantPeptideTable.setViewport(m_peptideScrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        

        
        return internalPanel;
    }                 
    
    public void setData(Long taskId, boolean displayForProteinSet, DQuantitationChannel[] quantChannels,  List<DMasterQuantPeptide> peptides, boolean isXICMode, boolean finished) {
        boolean qcChanged = true;
        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int q = 0; q < m_quantChannels.length; q++) {
                qcChanged = !(m_quantChannels[q].equals(quantChannels[q]));
            }
        }
        m_quantChannels = quantChannels;
        m_isXICMode = isXICMode;
        m_displayForProteinSet = displayForProteinSet;
        ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel()).setData(taskId, m_dataBox.getProjectId(), quantChannels, peptides, m_isXICMode);

        if (!m_isXICMode){
            ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel()).setOverviewType(QuantPeptideTableModel.COLTYPE_RAW_ABUNDANCE);
        }
        // select the first row
        if ((peptides != null) && (peptides.size() > 0)) {
            m_quantPeptideTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peptides.size());

        }

        m_infoToggleButton.updateInfo();
        
        if (finished) {
            // allow to change column visibility
            //m_columnVisibilityButton.setEnabled(true);
            m_quantPeptideTable.setSortable(true);
        }
        if (qcChanged){
            setColumnsVisibility();
        }
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_quantPeptideTable.dataUpdated(subTask, finished);
        if (finished) {
            // allow to change column visibility
            //m_columnVisibilityButton.setEnabled(true);
            m_quantPeptideTable.setSortable(true);
        }
    }
    
    public void setColumnsVisibility() {
        // hide the rawAbundance  and selectionLevel columns
        List<Integer> listIdsToHide = ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
        List<TableColumn> columns = m_quantPeptideTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(id)).setVisible(false);
            }
        }
        if (!m_displayForProteinSet) {
            // hide the cluster column
            boolean columnVisible = ((TableColumnExt) columns.get(QuantPeptideTableModel.COLTYPE_PEPTIDE_CLUSTER)).isVisible();
            if(columnVisible) {
                m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_PEPTIDE_CLUSTER)).setVisible(false);
            }
        }
        // hide the id column
        boolean columnVisible = ((TableColumnExt) columns.get(QuantPeptideTableModel.COLTYPE_PEPTIDE_ID)).isVisible();
        if(columnVisible) {
            m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        }
        if (!m_isXICMode) {
            // hide Validate/Unvalidate peptide column for SC 
            columnVisible = ((TableColumnExt) columns.get(QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL)).isVisible();
            if (columnVisible) {
                m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL)).setVisible(false);
            }
        }
    }

    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel()).setDatabox(dataBox);
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
        return (GlobalTableModelInterface) m_quantPeptideTable.getModel();
    }
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_quantPeptideTable;
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

    public DMasterQuantPeptide getSelectedMasterQuantPeptide() {
        return m_quantPeptideTable.getSelectedMasterQuantPeptide();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_quantPeptideTable;
    }
    
    private class QuantPeptideTable extends LazyTable implements ExportModelInterface, InfoInterface  {

        private ObjectParameter m_overviewParameter = null;
        
        public QuantPeptideTable() {
            super(m_peptideScrollPane.getVerticalScrollBar() );

        }
        
        public void validateModifications() {
            QuantPeptideTableModel quantPeptideTableModel = ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel());
            quantPeptideTableModel.validateModifications(m_validateModificationsPanel);
        }
        
        public void cancelModifications() {
            QuantPeptideTableModel quantPeptideTableModel = ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel());
            quantPeptideTableModel.cancelModifications();
            m_validateModificationsPanel.actionFinished(true, null);
        }
        
        @Override
        public ArrayList<ParameterList> getParameters() {
            ArrayList<ParameterList>  parameterListArray = super.getParameters();

            ParameterList overviewParameterList = new ParameterList("Overview Parameters");
            

     
            String[] overviewDisplay = { m_isXICMode ? "Overview on Pep. Match Count" : "Overview on Basic SC", "Overview on Abundance", m_isXICMode ?"Overview on Raw Abundance" : "Overview on Specific SC" };
            Integer[] overviewValues = { 0, 1, 2 };
            
            List<TableColumn> columns = getColumns(true);
            QuantPeptideTableModel quantPeptideTableModel = ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel());
            int overviewType = quantPeptideTableModel.getOverviewType();
            boolean overviewColumnVisible = ((TableColumnExt) columns.get(QuantPeptideTableModel.COLTYPE_OVERVIEW)).isVisible();
            
            int defaultIndex = 0;

            if (!overviewColumnVisible) {
                defaultIndex = 0;
            } else {
                switch (overviewType) {
                    case QuantPeptideTableModel.COLTYPE_ABUNDANCE:
                        defaultIndex = 1;
                        break;
                    case QuantPeptideTableModel.COLTYPE_RAW_ABUNDANCE:
                        defaultIndex = 2;
                        break;
                    case QuantPeptideTableModel.COLTYPE_PSM:
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
            
            QuantPeptideTableModel model = ((QuantPeptideTableModel) ((CompoundTableModel) m_quantPeptideTable.getModel()).getBaseModel());

            if (index == 1) {
                model.setOverviewType(QuantPeptideTableModel.COLTYPE_ABUNDANCE);
            } else if (index == 2) {
                model.setOverviewType(QuantPeptideTableModel.COLTYPE_RAW_ABUNDANCE);
            } else if (index == 0) {
                model.setOverviewType(QuantPeptideTableModel.COLTYPE_PSM);
            } 
   
        }
        
        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
        //Implement table cell tool tips.
        @Override
        public String getToolTipText(MouseEvent e) {
            Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            if (rowIndex<0) {
                return null;
            }
            int colIndex = columnAtPoint(p);
            if (colIndex<0) {
                return null;
            }
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            int realRowIndex = convertRowIndexToModel(rowIndex);
            CompoundTableModel tableModel = (CompoundTableModel) getModel();
            return tableModel.getTootlTipValue(realRowIndex, realColumnIndex);
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
 
            m_dataBox.propagateDataChanged(DMasterQuantPeptide.class);

        }
        
        public boolean selectPeptide(Long peptideId, String searchText) {
  
            QuantPeptideTableModel tableModel = (QuantPeptideTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(peptideId);
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
        


        public DMasterQuantPeptide getSelectedMasterQuantPeptide() {

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
            QuantPeptideTableModel tableModel = (QuantPeptideTableModel) compoundTableModel.getBaseModel();
            return tableModel.getPeptide(selectedRow);

        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_quantPeptideTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) m_quantPeptideTable.getModel()).getExportRowCell(convertRowIndexToModel(row),  convertColumnIndexToModel(col));
        }
        
        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((CompoundTableModel) m_quantPeptideTable.getModel()).getExportFonts(convertRowIndexToModel(row),  convertColumnIndexToModel(col));
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(XicPeptidePanel.this);

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
