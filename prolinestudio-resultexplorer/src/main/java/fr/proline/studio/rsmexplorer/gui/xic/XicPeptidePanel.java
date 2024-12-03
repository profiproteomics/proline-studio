/* 
 * Copyright (C) 2019
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

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.data.SelectLevelEnum;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
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
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.gui.SelectLevelRadioButtonGroup;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.table.CustomColumnControlButton;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
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
import fr.proline.studio.WindowManager;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.renderer.RendererMouseCallback;
import fr.proline.studio.rsmexplorer.gui.renderer.XicStatusRenderer;
import java.awt.Dialog;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class XicPeptidePanel extends HourglassPanel implements RendererMouseCallback, DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private AbstractDataBox m_dataBox;

    private JScrollPane m_peptideScrollPane;
    private QuantPeptideTable m_quantPeptideTable;
    private QuantPeptideTableModel m_quantPeptideTableModel;

    private MarkerContainerPanel m_markerContainerPanel;

    // private DefaultFloatingPanel m_validateModificationsPanel;
    private ModifyStatusDialog m_modifyStatusDialog;
    //private boolean m_displayForProteinSet;
    private DMasterQuantProteinSet m_proteinSetToDisplayFor;
    private DQuantitationChannel[] m_quantChannels;
    private DDatasetType.QuantitationMethodInfo m_quantMethodInfo;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    private SearchToggleButton m_searchToggleButton;
    private JButton m_modifyStatusButton;
    private InfoToggleButton m_infoToggleButton;

    private final boolean m_canGraph;

    private static final String OVERVIEW_KEY = "OVERVIEW_KEY";

    public XicPeptidePanel(boolean canGraph, DDatasetType.QuantitationMethodInfo quantMethodInfo) {
        m_canGraph = canGraph;
        m_quantMethodInfo = quantMethodInfo;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000);

        final JPanel peptidePanel = createPeptidePanel();
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
        layeredPane.add(m_infoToggleButton.getInfoPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER + 1));
        layeredPane.add(m_searchToggleButton.getSearchPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER + 2));
    }

    private JPanel createPeptidePanel() {

        JPanel peptidePanel = new JPanel();
        peptidePanel.setBounds(0, 0, 500, 400);
        peptidePanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

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
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }

        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_quantPeptideTable.getModel()), "Peptides", m_quantPeptideTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

        if (m_canGraph) {
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
                    wbox.setEntryData(m_dataBox.getProjectId(), null);  //JPM.DATABOX : it can work with null, there must be a wart somewhere so it works..
                    // open a window to display the window box
                    DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                    WindowManager.getDefault().getMainWindow().displayWindow(win);
                }
            });
            toolbar.add(m_graphicsButton);
        }

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_quantPeptideTable.getModel())) {

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
        toolbar.add(m_addCompareDataButton);
        m_modifyStatusButton = new JButton(IconManager.getIcon(IconManager.IconType.REFINE));
        m_modifyStatusButton.setToolTipText("Modify Peptide Status...");
        ActionListener modifyStatusButtonAction = createModifyStatusButtonAction();
        m_modifyStatusButton.addActionListener(modifyStatusButtonAction);
        toolbar.add(m_modifyStatusButton);
        m_infoToggleButton = new InfoToggleButton(m_quantPeptideTable, m_quantPeptideTable);
        toolbar.add(m_infoToggleButton);

        return toolbar;
    }

    private ActionListener createModifyStatusButtonAction() {
        ActionListener modifyStatusButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = m_quantPeptideTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    if (m_modifyStatusDialog == null) {
                        m_modifyStatusDialog = new ModifyStatusDialog();
                    }
                    m_modifyStatusDialog.setSelectedRows(selectedRows);
                    m_modifyStatusDialog.setLocationRelativeTo(m_modifyStatusButton);
                    m_modifyStatusDialog.setVisible(true);
                    
                }
            }
        };
        return modifyStatusButtonAction;
    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        m_peptideScrollPane = new JScrollPane();

        m_quantPeptideTable = new QuantPeptideTable();
        m_quantPeptideTableModel = new QuantPeptideTableModel((LazyTable) m_quantPeptideTable, this, m_quantMethodInfo);
        m_quantPeptideTable.setModel(new CompoundTableModel(m_quantPeptideTableModel, true));
        
        XicStatusRenderer renderer = (XicStatusRenderer) m_quantPeptideTableModel.getRenderer(0, QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL);
        m_quantPeptideTable.addMouseListener(renderer);
        m_quantPeptideTable.addMouseMotionListener(renderer);
        
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_quantPeptideTable);
        m_quantPeptideTable.setColumnControl(customColumnControl);
        // hide the id column
        m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        if (m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING)) {
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

    public void setData(Long taskId, DMasterQuantProteinSet proteinSetToDisplayFor, DQuantitationChannel[] quantChannels, List<DMasterQuantPeptide> peptides, DDatasetType.QuantitationMethodInfo quantitationMethodInfo, boolean finished) {
        boolean qcChanged = true;
        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int q = 0; q < m_quantChannels.length; q++) {
                qcChanged = !(m_quantChannels[q].equals(quantChannels[q]));
            }
        }
        m_quantChannels = quantChannels;
        m_quantMethodInfo = quantitationMethodInfo;
        m_proteinSetToDisplayFor = proteinSetToDisplayFor;
        m_quantPeptideTableModel.setData(taskId, m_dataBox.getProjectId(), quantChannels, peptides, m_proteinSetToDisplayFor, m_quantMethodInfo);

        if (m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING)) {
            m_quantPeptideTableModel.setOverviewType(QuantPeptideTableModel.COLTYPE_RAW_ABUNDANCE);
        }
        // select the first row
        if ((peptides != null) && (peptides.size() > 0)) {
            m_quantPeptideTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peptides.size());
            m_markerContainerPanel.removeAllMarkers();
        }

        m_infoToggleButton.updateInfo();

        if (finished) {
            // allow to change column visibility
            //m_columnVisibilityButton.setEnabled(true);
            m_quantPeptideTable.setSortable(true);
        }
        if (qcChanged) {
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

    private void setColumnsVisibility() {
        // hide the rawAbundance  and selectionLevel columns
        List<Integer> listIdsToHide = m_quantPeptideTableModel.getDefaultColumnsToHide();
        List<TableColumn> columns = m_quantPeptideTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(id)).setVisible(false);
            }
        }
        if (m_proteinSetToDisplayFor == null) {
            // hide the cluster column
            boolean columnVisible = ((TableColumnExt) columns.get(QuantPeptideTableModel.COLTYPE_PEPTIDE_CLUSTER)).isVisible();
            if (columnVisible) {
                m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_PEPTIDE_CLUSTER)).setVisible(false);
            }
        }
        // hide the id column
        boolean columnVisible = ((TableColumnExt) columns.get(QuantPeptideTableModel.COLTYPE_PEPTIDE_ID)).isVisible();
        if (columnVisible) {
            m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        }
        if (m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING)) {
            // hide Validate/Unvalidate peptide column for SC 
            columnVisible = ((TableColumnExt) columns.get(QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL)).isVisible();
            if (columnVisible) {
                m_quantPeptideTable.getColumnExt(m_quantPeptideTable.convertColumnIndexToView(QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL)).setVisible(false);
            }
        }
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_quantPeptideTableModel.setDatabox(dataBox);
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

    @Override
    public void mouseAction(MouseEvent e) {
        int col = m_quantPeptideTable.columnAtPoint(e.getPoint());
        int row = m_quantPeptideTable.rowAtPoint(e.getPoint());
        if (row != -1) {
            int rowModelIndex = m_quantPeptideTable.convertRowIndexToModel(row);
            if (m_quantPeptideTable.convertColumnIndexToModel(col) == QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL) {
                m_quantPeptideTable.getSelectionModel().setSelectionInterval(row, row);
                if (m_quantPeptideTableModel.isRowEditable(rowModelIndex)) {
                    if (m_modifyStatusDialog == null) {
                        m_modifyStatusDialog = new ModifyStatusDialog();
                    }
                    int[] selectedViewRows = new int[1];
                    selectedViewRows[0] = row;
                    m_modifyStatusDialog.setSelectedRows(selectedViewRows);
                    m_modifyStatusDialog.setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y);
                    m_modifyStatusDialog.setVisible(true);
                }
            }
        }
    }
    
    private class QuantPeptideTable extends LazyTable implements ExportModelInterface, InfoInterface {

        private ObjectParameter m_overviewParameter = null;

        public QuantPeptideTable() {
            super(m_peptideScrollPane.getVerticalScrollBar());
        }

        @Override
        public ArrayList<ParameterList> getParameters() {
            ArrayList<ParameterList> parameterListArray = super.getParameters();

            ParameterList overviewParameterList = new ParameterList("Overview Parameters");

            boolean isSC = m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING);
            String[] overviewDisplay = {!isSC ? "Overview on Pep. Match Count" : "Overview on Basic SC", "Overview on Abundance", !isSC ? "Overview on Raw Abundance" : "Overview on Specific SC"};
            Integer[] overviewValues = {0, 1, 2};

            List<TableColumn> columns = getColumns(true);
            int overviewType = m_quantPeptideTableModel.getOverviewType();
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

            if (index == 1) {
                m_quantPeptideTableModel.setOverviewType(QuantPeptideTableModel.COLTYPE_ABUNDANCE);
            } else if (index == 2) {
                m_quantPeptideTableModel.setOverviewType(QuantPeptideTableModel.COLTYPE_RAW_ABUNDANCE);
            } else if (index == 0) {
                m_quantPeptideTableModel.setOverviewType(QuantPeptideTableModel.COLTYPE_PSM);
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
            if (rowIndex < 0) {
                return null;
            }
            int colIndex = columnAtPoint(p);
            if (colIndex < 0) {
                return null;
            }
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            int realRowIndex = convertRowIndexToModel(rowIndex);
            if (realColumnIndex == QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL) {
                return super.getToolTipText(e);
            }
            CompoundTableModel tableModel = (CompoundTableModel) getModel();
            return tableModel.getTootlTipValue(realRowIndex, realColumnIndex);
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

            if (e.getValueIsAdjusting()) {
                // value is adjusting, so valueChanged will be called again
                return;
            }

            m_dataBox.addDataChanged(DMasterQuantPeptide.class);
            m_dataBox.propagateDataChanged();

        }

        public boolean selectPeptide(Long peptideId, String searchText) {

            QuantPeptideTableModel tableModel = (QuantPeptideTableModel) ((CompoundTableModel) getModel()).getBaseModel();
            int row = tableModel.findRow(peptideId);
            if (row == -1) {
                return false;
            }
            row = ((CompoundTableModel) getModel()).convertBaseModelRowToCompoundRow(row);
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
            try {  //JPM.WART : check if the problems is due to a multi-threading problem
                selectedRow = convertRowIndexToModel(selectedRow);
            } catch (Exception e) {
                // for unknow reason, an exception can occures due to the fact that the sorter is not ready
                m_loggerProline.debug("Exception catched as a wart : " + e.getMessage());
                return null;
            }
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
            return ((CompoundTableModel) m_quantPeptideTable.getModel()).getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((CompoundTableModel) m_quantPeptideTable.getModel()).getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
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
            return count + ((count > 1) ? " Peptides" : " Peptide");
        }

    }

    public void modifyStatusActionFinished(boolean success, String errorMessage) {
        m_modifyStatusDialog.actionFinished(success, errorMessage);
    }

    class ModifyStatusDialog extends DefaultDialog {

        private final ModifyStatusPanel m_internalPanel;

        private static final String CMD_VALIDATED = "Validated";
        private static final String CMD_INVALIDATED = "Invalidated";
        private static final String CMD_RESET = "Reset Status to Automatic";

        //XRadioButtonPanel _localValidButtonPane;
        //XRadioButtonPanel _localInvalidButtonPane; //JPM.LOCAL : put back if we can modify local values
        private LocalStatusPanel m_localStatusPanel;

        private SelectLevelRadioButtonGroup m_globalValidButtonPane;
        private SelectLevelRadioButtonGroup m_globalInvalidButtonPane;
        private ButtonGroup m_globalButtonGroup;

        private JButton m_resetButton;
        private DMasterQuantPeptide m_selectedPeptide; //for single select
        private ArrayList<Integer> m_selectedRows;

        public ModifyStatusDialog() {
            super(WindowManager.getDefault().getMainWindow(), Dialog.ModalityType.APPLICATION_MODAL);

            setTitle("Modify Peptide Status");

            m_internalPanel = new ModifyStatusPanel();

            String help_text = "Peptide status is the combination between the global status"
                    + " (defined for the whole dataset) and the local status (defined for"
                    + " a Protein Set using razor peptides selection for example).<br>"
                    + " Only the global status can be changed, this will impact the "
                    + "local status and Post-Processing service SHOULD be run again.";
            setHelpHeader(help_text, 370, 100);
            setInternalComponent(m_internalPanel);
            setResizable(true);

            setButtonVisible(BUTTON_HELP, false);//use only cancel, ok button
        }

        @Override
        protected boolean okCalled() {
            actionStarted();
            String command = m_globalButtonGroup.getSelection().getActionCommand();
            if (command.equals(CMD_VALIDATED)) {
                ArrayList<DMasterQuantPeptide> listToModify = m_quantPeptideTableModel.listToModifyForValidateModifications(m_selectedRows, SelectLevelEnum.SELECTED_MANUAL);
                m_quantPeptideTableModel.validateModifications(XicPeptidePanel.this, listToModify);
            } else if (command.equals(CMD_INVALIDATED)) {
                ArrayList<DMasterQuantPeptide> listToModify = m_quantPeptideTableModel.listToModifyForValidateModifications(m_selectedRows, SelectLevelEnum.DESELECTED_MANUAL);
                m_quantPeptideTableModel.validateModifications(XicPeptidePanel.this, listToModify);
            }
            super.m_buttonClicked = BUTTON_OK;
            return false;//  dialog should visible till to actionFinished
        }

        private ActionListener createResetAction() {
            ActionListener resetAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ArrayList<DMasterQuantPeptide> listToModify = m_quantPeptideTableModel.listToModifyForValidateModifications(m_selectedRows, SelectLevelEnum.RESET_AUTO);

                    m_quantPeptideTableModel.validateModifications(XicPeptidePanel.this, listToModify);
                }
            };
            return resetAction;

        }

        @Override
        protected boolean cancelCalled() {
            return true;
        }

        public void actionFinished(boolean success, String errorMessage) {
            m_selectedPeptide = null;
            m_selectedRows.clear();
            m_internalPanel.setLoaded(1);
            setVisible(false);
            if (!success) {
                setStatus(success, errorMessage);//set dialog status
            }
        }

        public void actionStarted() {
            m_internalPanel.setLoading(1, true);
        }

        /*
         * single selected, trigger by mouse clic on column
         * QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL
         *
         * @param modelRow : index of select row (convert to model row index)
         */
        /*private void selectedRow(int modelRow) {
            if (modelRow == -1) {
                m_selectedRows.clear();
                m_selectedPeptide = null;
            } else {
                m_selectedRows.clear();
                m_selectedRows.add(modelRow);

                m_selectedPeptide = (DMasterQuantPeptide) m_quantPeptideTableModel.getRowValue(DMasterQuantPeptide.class, modelRow);
                if (m_selectedPeptide != null) {//single select
                    int globalSelectLevel = m_selectedPeptide.getSelectionLevel();
                    updateRadioButton(XicStatusRenderer.SelectLevelEnum.valueOf(globalSelectLevel), m_globalValidButtonPane, m_globalInvalidButtonPane);
                    XicStatusRenderer.SelectLevel selectLevel = m_quantPeptideTableModel.getSelectionLevelFor(m_selectedPeptide);

                    updateLocal(selectLevel.m_status, m_localStatusPanel);
                    //JPM.LOCAL
                    //updateRadioButton(selectLevel.m_status, _localValidButtonPane, _localInvalidButtonPane);
                }
            }

            ArrayList<DMasterQuantPeptide> listToModify = m_quantPeptideTableModel.listToModifyForValidateModifications(m_selectedRows, XicStatusRenderer.SelectLevelEnum.RESET_AUTO);
            m_resetButton.setEnabled(listToModify != null);

        }*/


        private void updateRadioButton(ButtonGroup buttonGroup, boolean selectLevelHomogenous, SelectLevelEnum selectLevel, SelectLevelRadioButtonGroup validPane, SelectLevelRadioButtonGroup invalidPane) {

            if (selectLevelHomogenous) {
                switch (selectLevel) {
                    case DESELECTED_MANUAL:
                    case DESELECTED_AUTO:
                        validPane.removeOptionIcon();
                        invalidPane.getRadioButton().setSelected(true);
                        invalidPane.addIcon(selectLevel.getIntValue());
                        break;
                    case SELECTED_AUTO:
                    case SELECTED_MANUAL:
                        validPane.getRadioButton().setSelected(true);
                        validPane.addIcon(selectLevel.getIntValue());
                        invalidPane.removeOptionIcon();
                        break;
                    default:
                        validPane.getRadioButton().setSelected(false);
                        invalidPane.getRadioButton().setSelected(false);
                        validPane.removeOptionIcon();
                        invalidPane.removeOptionIcon();
                }
            } else {
                validPane.removeOptionIcon();
                invalidPane.removeOptionIcon();
                buttonGroup.clearSelection();
            }
        }

        private void updateLocal(boolean localSelectLevelHomogenous, SelectLevelEnum selectLevel, LocalStatusPanel statusPanel) {

            boolean automatic = (selectLevel == SelectLevelEnum.DESELECTED_AUTO) || (selectLevel == SelectLevelEnum.SELECTED_AUTO);
            boolean validated = (selectLevel == SelectLevelEnum.SELECTED_AUTO) || (selectLevel == SelectLevelEnum.SELECTED_MANUAL);

            statusPanel.setValues(localSelectLevelHomogenous, validated, automatic);

        }

        /**
         * multi select, trigger by m_modifyStatusButton
         *
         * @param selectedViewRows: non empty array, table row orignal index, not
         * model index
         */
        /*private void setSelectedRowsOLD(int[] selectedViewRows) {
            if (selectedViewRows.length == 1) {
                selectedRow(m_quantPeptideTable.convertRowIndexToModel(selectedViewRows[0]));
            } else {
                int modelIndex;
                for (int row : selectedViewRows) {
                    modelIndex = m_quantPeptideTable.convertRowIndexToModel(row);
                    m_selectedRows.add(modelIndex);
                }
                m_globalValidButtonPane.removeOptionIcon();
                m_globalInvalidButtonPane.removeOptionIcon();

                m_localStatusPanel.resetValues();
            }

            ArrayList<DMasterQuantPeptide> listToModify = m_quantPeptideTableModel.listToModifyForValidateModifications(m_selectedRows, XicStatusRenderer.SelectLevelEnum.RESET_AUTO);
            m_resetButton.setEnabled(listToModify != null);
        }*/
        private void setSelectedRows(int[] selectedViewRows) {

            m_selectedRows.clear();
            for (int row : selectedViewRows) {
                int modelIndex = m_quantPeptideTable.convertRowIndexToModel(row);
                m_selectedRows.add(modelIndex);
            }

            DMasterQuantPeptide firstSelectedPeptide = (DMasterQuantPeptide) m_quantPeptideTableModel.getRowValue(DMasterQuantPeptide.class, m_selectedRows.get(0));
            if (m_selectedRows.size() == 1) {
                m_selectedPeptide = firstSelectedPeptide;
            }

            boolean globalSelectLevelHomogenous = true;
            int globalSelectLevelRef = firstSelectedPeptide.getSelectionLevel();
            for (int modelIndex : m_selectedRows) {
                DMasterQuantPeptide selectedPeptide = (DMasterQuantPeptide) m_quantPeptideTableModel.getRowValue(DMasterQuantPeptide.class, modelIndex);
                int globalSelectLevelCur = selectedPeptide.getSelectionLevel();
                if (globalSelectLevelRef != globalSelectLevelCur) {
                    globalSelectLevelHomogenous = false;
                    break;
                }
            }
            updateRadioButton(m_globalButtonGroup, globalSelectLevelHomogenous, SelectLevelEnum.valueOf(globalSelectLevelRef), m_globalValidButtonPane, m_globalInvalidButtonPane);

            boolean localSelectLevelHomogenous = true;
            int localSelectLevelRef = m_quantPeptideTableModel.getSelectionLevelFor(firstSelectedPeptide).m_status.getIntValue();
            for (int modelIndex : m_selectedRows) {
                DMasterQuantPeptide selectedPeptide = (DMasterQuantPeptide) m_quantPeptideTableModel.getRowValue(DMasterQuantPeptide.class, modelIndex);
                int localSelectLevelCur = m_quantPeptideTableModel.getSelectionLevelFor(selectedPeptide).m_status.getIntValue();
                if (localSelectLevelRef != localSelectLevelCur) {
                    localSelectLevelHomogenous = false;
                    break;
                }
            }
            updateLocal(localSelectLevelHomogenous, m_quantPeptideTableModel.getSelectionLevelFor(firstSelectedPeptide).m_status, m_localStatusPanel);
            //JPM.LOCAL
            //updateRadioButton(localSelectLevelHomogenous, selectLevel.m_status, _localValidButtonPane, _localInvalidButtonPane);



            ArrayList<DMasterQuantPeptide> listToModify = m_quantPeptideTableModel.listToModifyForValidateModifications(m_selectedRows, SelectLevelEnum.RESET_AUTO);
            m_resetButton.setEnabled(listToModify != null);
        }

        class ModifyStatusPanel extends HourglassPanel {

            ModifyStatusPanel() {
                super();

                //create local items
                m_selectedRows = new ArrayList();

                // Local Status
                JPanel localPane = createLocalStatusPanel();

                // global status
                JPanel globalPane = createGlobalStatusPanel();


                // whole layout
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                setBorder(new EmptyBorder(10, 10, 10, 10));
                add(localPane);
                add(Box.createRigidArea(new Dimension(0, 10)));
                add(globalPane);
                Dimension d = getPreferredSize();
                setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight());
            }
        }

        private JPanel createLocalStatusPanel() {

                JLabel localText = new JLabel("Status for Selected Protein Set :");
                m_localStatusPanel = new LocalStatusPanel();

                JPanel localPane = new JPanel(new GridBagLayout());
                localPane.setBorder(BorderFactory.createTitledBorder(" Peptide Local Status"));
                GridBagConstraints c = new GridBagConstraints();

                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new java.awt.Insets(2, 2, 2, 2);
                c.gridx = 0;
                c.gridy = 0;
                c.fill = GridBagConstraints.BOTH;

                localPane.add(Box.createHorizontalStrut(20), c);

                c.gridx++;
                localPane.add(localText, c);

                c.gridx++;
                localPane.add(Box.createHorizontalStrut(10), c);

                c.gridx++;
                localPane.add(m_localStatusPanel, c);

                c.gridx++;
                c.weightx = 1;
                localPane.add(Box.createGlue(), c);
                c.weightx = 0;


                return localPane;
                /*localPane.add(_localValidButtonPane, c1);
                c1.gridy++;
                localPane.add(_localInvalidButtonPane, c1);
                */ //JPM.LOCAL

                /*_localValidButtonPane = new XRadioButtonPanel(cmd_validated, IconManager.getIcon(IconManager.IconType.VALIDATED));;
                _localInvalidButtonPane = new XRadioButtonPanel(cmd_invalidated, IconManager.getIcon(IconManager.IconType.INVALIDATED));
                _localValidButtonPane.getRadioButton().setEnabled(false);
                _localInvalidButtonPane.getRadioButton().setEnabled(false);
                ButtonGroup localButtonGroup = new ButtonGroup();
                localButtonGroup.add(_localValidButtonPane.getRadioButton());
                localButtonGroup.add(_localInvalidButtonPane.getRadioButton());*/  //JPM.LOCAL
        }

        private JPanel createGlobalStatusPanel() {

            JPanel globalPane = new JPanel(new GridBagLayout());
            globalPane.setBorder(BorderFactory.createTitledBorder(" Peptide Global Status"));
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new java.awt.Insets(2, 2, 2, 2);
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;

            JLabel globalTextLabel = new JLabel("Status for Whole Dataset :");

            globalPane.add(Box.createHorizontalStrut(20), c);

            c.gridx++;
            globalPane.add(globalTextLabel, c);

            c.gridx++;
            globalPane.add(Box.createHorizontalStrut(10), c);

            int anchor = c.gridx + 1;
            c.gridx = anchor;
            m_globalValidButtonPane = new SelectLevelRadioButtonGroup(globalPane, c, CMD_VALIDATED, IconManager.getIcon(IconManager.IconType.VALIDATED));

            c.gridy++;
            c.gridx = anchor;
            m_globalInvalidButtonPane = new SelectLevelRadioButtonGroup(globalPane, c, CMD_INVALIDATED, IconManager.getIcon(IconManager.IconType.INVALIDATED));
            m_globalValidButtonPane.getRadioButton().setActionCommand(CMD_VALIDATED);
            m_globalInvalidButtonPane.getRadioButton().setActionCommand(CMD_INVALIDATED);

            m_globalButtonGroup = new ButtonGroup();
            m_globalButtonGroup.add(m_globalValidButtonPane.getRadioButton());
            m_globalButtonGroup.add(m_globalInvalidButtonPane.getRadioButton());

            // -- Reset Status Button
            //create reset button
            m_resetButton = new JButton(CMD_RESET, IconManager.getIcon(IconManager.IconType.DEFAULT));
            m_resetButton.addActionListener(createResetAction());
            JPanel resetButtonPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c2 = new GridBagConstraints();
            c2.anchor = GridBagConstraints.NORTHWEST;
            c2.insets = new java.awt.Insets(2, 2, 2, 2);
            c2.gridx = 0;
            c2.gridy = 0;
            c2.fill = GridBagConstraints.BOTH;
            c2.weightx = 1;
            resetButtonPanel.add(Box.createHorizontalGlue(), c2);
            c2.weightx = 0;
            c2.gridx++;
            resetButtonPanel.add(m_resetButton);

            c.gridx = 1;
            c.gridwidth = 6;
            c.weightx = 1;
            c.gridy++;
            globalPane.add(resetButtonPanel, c);

            return globalPane;
        }

    }


    private class LocalStatusPanel extends JPanel {

        private final JLabel m_textLabel;
        private final JLabel m_validatedlabel;
        private final JLabel m_automaticLabel;

        public LocalStatusPanel() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(1, 1, 1, 1);

            m_textLabel = new JLabel();
            m_validatedlabel = new JLabel() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(16,16);
                }
            };
            m_automaticLabel = new JLabel() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(16,16);
                }
            };

            c.gridx = 0;
            add(m_textLabel, c);

            c.gridx++;
            add(Box.createHorizontalStrut(10), c);

            c.gridx++;
            add(m_validatedlabel, c);

            c.gridx++;
            add(m_automaticLabel, c);
        }

        public void setValues(boolean localSelectLevelHomogenous, boolean validated, boolean automatic) {

            if (!localSelectLevelHomogenous) {
                resetValues();
                return;
            }

            if (validated) {
                m_textLabel.setText("Validated");
                m_validatedlabel.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED));
                m_validatedlabel.setToolTipText("Validated");
            } else {
                m_textLabel.setText("Invalidated");
                m_validatedlabel.setIcon(IconManager.getIcon(IconManager.IconType.INVALIDATED));
                m_validatedlabel.setToolTipText("Invalidated");
            }

            if (automatic) {
                m_automaticLabel.setIcon(IconManager.getIcon(IconManager.IconType.GEAR));
                m_automaticLabel.setToolTipText("Automatic");
            } else {
                m_automaticLabel.setIcon(IconManager.getIcon(IconManager.IconType.HAND_OPEN));
                m_automaticLabel.setToolTipText("Manual");
            }
        }

        public void resetValues() {
            m_textLabel.setText("Heterogeneous Values");
            m_validatedlabel.setIcon(null);
            m_automaticLabel.setToolTipText(null);
            m_automaticLabel.setIcon(null);
            m_automaticLabel.setToolTipText(null);
        }

    }

}
