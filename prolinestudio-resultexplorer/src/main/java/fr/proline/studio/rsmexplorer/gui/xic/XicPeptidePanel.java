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

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
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
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
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
import org.openide.windows.WindowManager;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.renderer.XicStatusRenderer;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class XicPeptidePanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private AbstractDataBox m_dataBox;

    private JScrollPane m_peptideScrollPane;
    private QuantPeptideTable m_quantPeptideTable;
    private QuantPeptideTableModel m_quantPeptideTableModel;

    private MarkerContainerPanel m_markerContainerPanel;

    // private DefaultFloatingPanel m_validateModificationsPanel;
    private ModifyStatusDialog m_modifyStatusDialog;
    private boolean m_displayForProteinSet;
    private DQuantitationChannel[] m_quantChannels;
    private boolean m_isXICMode;

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

    public XicPeptidePanel(boolean canGraph, boolean xicMode) {
        m_canGraph = canGraph;
        initComponents(xicMode);
    }

    private void initComponents(boolean xicMode) {
        setLayout(new BorderLayout());

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        m_modifyStatusDialog = new ModifyStatusDialog();
        final JPanel peptidePanel = createPeptidePanel(xicMode);
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
        layeredPane.add(m_infoToggleButton.getInfoPanel(), new Integer(JLayeredPane.PALETTE_LAYER + 1));
        layeredPane.add(m_searchToggleButton.getSearchPanel(), new Integer(JLayeredPane.PALETTE_LAYER + 2));
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
                m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);
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
                    wbox.setEntryData(m_dataBox.getProjectId(), m_dataBox.getData(false, List.class));

                    // open a window to display the window box
                    DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                    win.open();
                    win.requestActive();
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
        m_modifyStatusButton.setToolTipText("Modify Status");
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
                    m_modifyStatusDialog.setSelectedRows(selectedRows);
                    m_modifyStatusDialog.setVisible(true);
                    Point p = m_modifyStatusButton.getLocationOnScreen();
                    m_modifyStatusDialog.setLocation(p.x + 5, p.y + 5);
                }
            }
        };
        return modifyStatusButtonAction;
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
        m_quantPeptideTableModel = new QuantPeptideTableModel((LazyTable) m_quantPeptideTable, xicMode);
        m_quantPeptideTable.setModel(new CompoundTableModel(m_quantPeptideTableModel, true));
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

    public void setData(Long taskId, boolean displayForProteinSet, DQuantitationChannel[] quantChannels, List<DMasterQuantPeptide> peptides, boolean isXICMode, boolean finished) {
        boolean qcChanged = true;
        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int q = 0; q < m_quantChannels.length; q++) {
                qcChanged = !(m_quantChannels[q].equals(quantChannels[q]));
            }
        }
        m_quantChannels = quantChannels;
        m_isXICMode = isXICMode;
        m_displayForProteinSet = displayForProteinSet;
        m_quantPeptideTableModel.setData(taskId, m_dataBox.getProjectId(), quantChannels, peptides, m_isXICMode);

        if (!m_isXICMode) {
            m_quantPeptideTableModel.setOverviewType(QuantPeptideTableModel.COLTYPE_RAW_ABUNDANCE);
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
        if (!m_displayForProteinSet) {
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

    private class QuantPeptideTable extends LazyTable implements ExportModelInterface, InfoInterface {

        private ObjectParameter m_overviewParameter = null;

        public QuantPeptideTable() {
            super(m_peptideScrollPane.getVerticalScrollBar());
            setStutusColonneListner();
        }

        @Override
        public ArrayList<ParameterList> getParameters() {
            ArrayList<ParameterList> parameterListArray = super.getParameters();

            ParameterList overviewParameterList = new ParameterList("Overview Parameters");

            String[] overviewDisplay = {m_isXICMode ? "Overview on Pep. Match Count" : "Overview on Basic SC", "Overview on Abundance", m_isXICMode ? "Overview on Raw Abundance" : "Overview on Specific SC"};
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

            m_dataBox.propagateDataChanged(DMasterQuantPeptide.class);

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

        private void setStutusColonneListner() {
            addMouseListener(createChangeStatusMouseAdapter());
        }

        private MouseAdapter createChangeStatusMouseAdapter() {
            MouseAdapter changeStatusMouseAdapter = new MouseAdapter() {

                /**
                 * if mousePressed right on the colonne
                 * QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL,
                 * popup
                 *
                 * @param e
                 */
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        int col = m_quantPeptideTable.columnAtPoint(e.getPoint());
                        int row = m_quantPeptideTable.rowAtPoint(e.getPoint());
                        if (row != -1) {
                            int colModelIndex = m_quantPeptideTable.convertColumnIndexToModel(col);
                            int rowModelIndex = m_quantPeptideTable.convertRowIndexToModel(row);
                            if (m_quantPeptideTable.convertColumnIndexToModel(col) == QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL) {
                                if (e.isShiftDown() || e.isControlDown()) {//multi select
                                    m_modifyStatusDialog.selectedRow(-1);
                                    m_modifyStatusDialog.setVisible(false);
                                } else {
                                    m_quantPeptideTable.getSelectionModel().setSelectionInterval(row, row);
                                    if (m_modifyStatusDialog != null && m_quantPeptideTableModel.isRowEditable(rowModelIndex)) {
                                        m_modifyStatusDialog.selectedRow(rowModelIndex);
                                        m_modifyStatusDialog.setLocation(e.getLocationOnScreen());
                                        m_modifyStatusDialog.setVisible(true);
                                    }
                                }
                            }
                        }
                    }
                }
            };

            return changeStatusMouseAdapter;
        }
    }

    public void modifyStatusActionFinished(boolean success, String errorMessage) {
        m_modifyStatusDialog.actionFinished(success, errorMessage);
    }

    class ModifyStatusDialog extends DefaultDialog {

        private JPanel _helpPanel;
        private ModifyStatusPanel _internalPanel;
        String cmd_validated = "Validated";
        String cmd_invalidated = "Invalidated";
        String cmd_reset = "Reset manual status to auto";
        XRadioButtonPanel _validButtonPane;
        XRadioButtonPanel _invalidButtonPane;
        ButtonGroup _buttonGroup;
        JButton _resetButton;
        DMasterQuantPeptide _selectedPeptide; //for single select
        ArrayList<Integer> _selectedRows;

        public ModifyStatusDialog() {
            super(WindowManager.getDefault().getMainWindow(), Dialog.ModalityType.APPLICATION_MODAL);
            _internalPanel = new ModifyStatusPanel();
            super.setTitle("Modify Status Dialog");
            super.setHelpHeaderText("Modify validated/Invalidated status");
            super.setInternalComponent(_internalPanel);
            setButtonVisible(BUTTON_HELP, false);//use only cancel, ok button
        }

        @Override
        protected boolean okCalled() {
            actionStarted();
            String command = _buttonGroup.getSelection().getActionCommand();
            if (command.equals(cmd_validated)) {
                m_quantPeptideTableModel.validateModifications(XicPeptidePanel.this, _selectedRows, XicStatusRenderer.SelectLevel.SELECTED_MANUAL);
            } else if (command.equals(cmd_invalidated)) {
                m_quantPeptideTableModel.validateModifications(XicPeptidePanel.this, _selectedRows, XicStatusRenderer.SelectLevel.DESELECTED_MANUAL);
            }
            _buttonGroup.clearSelection();
            super.m_buttonClicked = BUTTON_OK;
            return false;//  dialog should visible till to actionFinished
        }

        private ActionListener createResetAction() {
            ActionListener resetAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    m_quantPeptideTableModel.validateModifications(XicPeptidePanel.this, _selectedRows, XicStatusRenderer.SelectLevel.RESET_AUTO);
                }
            };
            return resetAction;

        }

        @Override
        protected boolean cancelCalled() {
            _buttonGroup.clearSelection();
            setVisible(false);
            return true;
        }

        public void actionFinished(boolean success, String errorMessage) {
            _selectedPeptide = null;
            _selectedRows.clear();
            _buttonGroup.clearSelection();
            _internalPanel.setLoaded(1);
            setVisible(false);
            if (!success) {
                setStatus(success, errorMessage);//set dialog status
            }
        }

        public void actionStarted() {
            _internalPanel.setLoading(1, true);
        }

        /**
         * single selected, trigger by mouse clic on column
         * QuantPeptideTableModel.COLTYPE_MQPEPTIDE_SELECTION_LEVEL
         *
         * @param modelRow : index of select row (convert to model row index)
         */
        private void selectedRow(int modelRow) {
            if (modelRow == -1) {
                _selectedPeptide = null;
            } else {
                _selectedRows.clear();
                _selectedRows.add(modelRow);

                _selectedPeptide = (DMasterQuantPeptide) m_quantPeptideTableModel.getRowValue(DMasterQuantPeptide.class, modelRow);
                if (_selectedPeptide != null) {//single select
                    int selectLevel = _selectedPeptide.getSelectionLevel();
                    if (selectLevel < 2) {
                        _invalidButtonPane.getRadioButton().setSelected(true);
                        //_invalidButton.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                        //@todo add manul/auto icon
                    } else if (selectLevel >= 2) {
                        _validButtonPane.getRadioButton().setSelected(true);
                        //_validButton.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                        //@todo add manul/auto icon
                    }
                }
            }
        }

        /**
         * multi select, trigger by m_modifyStatusButton
         *
         * @param selectedRows: non empty array, table row orignal index, not
         * model index
         */
        private void setSelectedRows(int[] selectedViewRows) {
            if (selectedViewRows.length == 1) {
                selectedRow(m_quantPeptideTable.convertRowIndexToModel(selectedViewRows[0]));
            } else {
                int modelIndex;
                for (int row : selectedViewRows) {
                    modelIndex = m_quantPeptideTable.convertRowIndexToModel(row);
                    _selectedRows.add(modelIndex);
                }
                _buttonGroup.clearSelection();
            }
        }

        class ModifyStatusPanel extends HourglassPanel {

            ModifyStatusPanel() {
                //model
                _selectedRows = new ArrayList();

                //create 2 radio buttons
                _validButtonPane = new XRadioButtonPanel(cmd_validated, IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                _invalidButtonPane = new XRadioButtonPanel(cmd_invalidated, IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                _validButtonPane.getRadioButton().setActionCommand(cmd_validated);
                _invalidButtonPane.getRadioButton().setActionCommand(cmd_invalidated);

                _buttonGroup = new ButtonGroup();
                _buttonGroup.add(_validButtonPane.getRadioButton());
                _buttonGroup.add(_invalidButtonPane.getRadioButton());
                //create reset button
                _resetButton = new JButton(cmd_reset);
                _resetButton.addActionListener(createResetAction());

                //layout
                setBorder(BorderFactory.createLineBorder(Color.darkGray, 1, true));
                setOpaque(true);
                setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.NORTHWEST;
                c.insets = new java.awt.Insets(5, 5, 5, 0);
                c.gridx = 0;
                c.gridy = 0;
                this.add(_validButtonPane, c);
                c.gridy++;
                this.add(_invalidButtonPane, c);
                c.gridy++;
                this.add(Box.createRigidArea(new Dimension(10, 10)));
                c.gridy++;
                this.add(_resetButton, c);

                Dimension d = getPreferredSize();
                setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight());
            }
        }

    }

    class XRadioButtonPanel extends JPanel {

        private JRadioButton _radioButton;
        private JLabel _label;

        public XRadioButtonPanel() {
            setLayout(new GridBagLayout());
            add(getRadioButton());
            add(getLabel());
        }

        public XRadioButtonPanel(String text, Icon icon) {
            this();
            setText(text);
            setIcon(icon);
        }

        public JRadioButton getRadioButton() {
            if (_radioButton == null) {
                _radioButton = new JRadioButton();
            }
            return _radioButton;
        }

        protected JLabel getLabel() {
            if (_label == null) {
                _label = new JLabel();
                _label.setHorizontalTextPosition(JLabel.LEFT);
                _label.setVerticalTextPosition(JLabel.BOTTOM);
                _label.setLabelFor(getRadioButton());
            }
            return _label;
        }

        public void setText(String text) {
            getLabel().setText(text);
        }

        public String getText() {
            return getLabel().getText();
        }

        public void setIcon(Icon icon) {
            getLabel().setIcon(icon);
        }

        public Icon getIcon() {
            return getLabel().getIcon();
        }
    }
}
