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
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.*;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.model.PTMPeptidesTableModel;
import fr.proline.studio.table.CustomColumnControlButton;
import fr.proline.studio.table.LazyTable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.table.TableColumnExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMPeptidesTablePanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface, SplittedPanelContainer.ReactiveTabbedComponent {

    protected static final Logger LOG = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");
    protected AbstractDataBox m_dataBox;

    private JScrollPane m_ptmPeptidesScrollPane;
    protected PTMPeptidesTable m_ptmPeptidesTable;
    protected PTMPeptidesTableModel m_ptmPeptidesTableModel;
    protected MarkerContainerPanel m_markerContainerPanel;

    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;

    protected List<PTMPeptideInstance> m_ptmPeptideInstances = null;

    //--- Xic Specific 
    private final boolean m_isXICResult;

    // protected DPeptideInstance m_currentPepInst = null;
    private final boolean m_displayPeptidesMatches;

    //Specify if current JPanel is visible or not
    private boolean m_isDisplayed = true;

    /**
     * Display Peptides Matches of a PTMSite.
     *
     * @param viewAll if true display all Peptides Matches of all Peptide
     * Instance for this PTMSite. If false, display only best PeptideMatch of
     * all Peptide Instance for this PTMSite.
     *
     */
    public PTMPeptidesTablePanel(boolean viewAll, boolean isXicResult) {
        m_displayPeptidesMatches = viewAll;
        m_isXICResult = isXicResult;
        initComponents();
    }

    public PTMPeptideInstance getSelectedPTMPeptideInstance() {
        if (m_ptmPeptidesTableModel.getRowCount() <= 0) {
            return null;
        }
        PTMPeptidesTableModel.Row row = m_ptmPeptidesTableModel.getPTMPeptideInstanceAt(getSelectedRowInTableModel());
        return row != null ? row.ptmPeptideInstance : null;
    }

    public DPeptideMatch getSelectedPeptideMatch() {
        if (m_ptmPeptidesTableModel.getRowCount() <= 0) {
            return null;
        }
        PTMPeptidesTableModel.Row row = m_ptmPeptidesTableModel.getPTMPeptideInstanceAt(getSelectedRowInTableModel());
        return row != null ? row.peptideMatch : null;
    }

    public DMasterQuantPeptide getSelectedMasterQuantPeptide() {
        if (m_ptmPeptidesTableModel.getRowCount() <= 0) {
            return null;
        }
        return m_ptmPeptidesTableModel.getMasterQuantPeptideAt(getSelectedRowInTableModel());
    }

    public int getSelectedIndex() {
        if (m_ptmPeptidesTableModel.getRowCount() <= 0) {
            return -1;
        }
        return getSelectedRowInTableModel();
    }

    /**
     * the selected Index in the table is different from it's index in
     * tableModel, which is in orignal order without sorting nor filting
     *
     * @return
     */
    private int getSelectedRowInTableModel() {

        // Retrieve Selected Row
        int selectedRow = m_ptmPeptidesTable.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return -1;
        }

        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_ptmPeptidesTable.getModel());
        // convert according to the sorting
        selectedRow = m_ptmPeptidesTable.convertRowIndexToModel(selectedRow);
        //find the original index
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);
        return selectedRow;
    }

    public void setSelectedPeptide(PTMPeptideInstance pep) {
        int index = m_ptmPeptidesTableModel.getPeptideInstanceIndex(pep); //this is the model original index
        //now find the real row perhaps after sorting or filtering
        if (index != -1) {
            //find the original index
            CompoundTableModel compoundTableModel = ((CompoundTableModel) m_ptmPeptidesTable.getModel());
            int selectedRow = compoundTableModel.convertBaseModelRowToCompoundRow(index);
            // convert according to the sorting
            index = m_ptmPeptidesTable.convertRowIndexToView(selectedRow);

            this.setSelectedRow(index);
        }
    }

    public void setData(Long taskId, List<PTMPeptideInstance> ptmPeptides, List<PTMCluster> ptmClusters, Map<Long, DMasterQuantPeptide> quantPeptidesByPepInsId, boolean finished) {

        if (Objects.equals(ptmPeptides, m_ptmPeptideInstances)) {
            return;
        }

        m_ptmPeptideInstances = ptmPeptides;

        int previousPtmSitesColumnCount = m_ptmPeptidesTableModel.getPtmSitesColumnCount();
        List<Boolean> prevColumnsVisibility = m_ptmPeptidesTable.getColumns(true).stream().map(tc -> ((TableColumnExt)tc).isVisible()).collect(Collectors.toList());

        m_ptmPeptidesTableModel.setData(taskId, m_ptmPeptideInstances, ptmClusters, quantPeptidesByPepInsId);
        
        // select the first row
        if (m_ptmPeptideInstances != null) {
            m_ptmPeptidesTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(m_ptmPeptidesTable.getModel().getRowCount());
        }

        m_infoToggleButton.updateInfo();
        if (previousPtmSitesColumnCount != 0)
            restoreColumnsVisibility(prevColumnsVisibility, previousPtmSitesColumnCount);
        else
            setColumnsDefaultVisibility();
        if (finished) {
            m_ptmPeptidesTable.setSortable(true);
        }
    }

    private void restoreColumnsVisibility(List<Boolean> prevColumnsVisibility, int previousPtmSitesColumnCount) {
        // hide the rawAbundance  and selectionLevel columns
        int ptmSitesColumnCount = m_ptmPeptidesTableModel.getPtmSitesColumnCount();

        List<TableColumn> columns = m_ptmPeptidesTable.getColumns(true);

        for (int i = 0; i < columns.size(); i++) {
            if (i <= PTMPeptidesTableModel.LAST_STATIC_COLUMN) {
                m_ptmPeptidesTable.getColumnExt(m_ptmPeptidesTable.convertColumnIndexToView(i)).setVisible(prevColumnsVisibility.get(i));
            } else if (i > PTMPeptidesTableModel.LAST_STATIC_COLUMN+ptmSitesColumnCount) {
                m_ptmPeptidesTable.getColumnExt(m_ptmPeptidesTable.convertColumnIndexToView(i)).setVisible(prevColumnsVisibility.get(i + (previousPtmSitesColumnCount - ptmSitesColumnCount)));
            } else {
                m_ptmPeptidesTable.getColumnExt(m_ptmPeptidesTable.convertColumnIndexToView(i)).setVisible(true);
            }
        }
    }

    private void setColumnsDefaultVisibility() {
        // hide the rawAbundance  and selectionLevel columns
        List<Integer> listIdsToHide = m_ptmPeptidesTableModel.getDefaultColumnsToHide();
        List<TableColumn> columns = m_ptmPeptidesTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_ptmPeptidesTable.getColumnExt(m_ptmPeptidesTable.convertColumnIndexToView(id)).setVisible(false);
            }
        }
    }

    private void initComponents() {

        setLayout(new BorderLayout());

        final JPanel ptmPeptidesPanel = createPTMPeptidesPanel();

        final JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                ptmPeptidesPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(ptmPeptidesPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER + 1));
        setColumnsDefaultVisibility();
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_exportButton = new ExportButton(((CompoundTableModel) m_ptmPeptidesTable.getModel()), "PTM Peptides", m_ptmPeptidesTable);
        m_searchToggleButton = new SearchToggleButton(m_ptmPeptidesTable, m_ptmPeptidesTable, ((CompoundTableModel) m_ptmPeptidesTable.getModel()));
        m_settingsButton = new SettingsButton(((ProgressInterface) m_ptmPeptidesTable.getModel()), m_ptmPeptidesTable);
        m_infoToggleButton = new InfoToggleButton(m_ptmPeptidesTable, m_ptmPeptidesTable);
        m_filterButton = new FilterButton(((CompoundTableModel) m_ptmPeptidesTable.getModel())) {
            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }
        };

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_ptmPeptidesTable.getModel())) {

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

    private JPanel createPTMPeptidesPanel() {

        JPanel ptmPeptidesPanel = new JPanel();
        ptmPeptidesPanel.setBounds(0, 0, 500, 400);
        ptmPeptidesPanel.setLayout(new BorderLayout());

        //--- Create Internal Pane
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        m_ptmPeptidesScrollPane = new JScrollPane();

        m_ptmPeptidesTable = new PTMPeptidesTable();
        m_ptmPeptidesTableModel = new PTMPeptidesTableModel(m_ptmPeptidesTable, m_isXICResult, m_displayPeptidesMatches);
        m_ptmPeptidesTable.setModel(new CompoundTableModel(m_ptmPeptidesTableModel, true));

        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_ptmPeptidesTable);
        m_ptmPeptidesTable.setColumnControl(customColumnControl);

        // hide the id column
        m_ptmPeptidesTable.getColumnExt(m_ptmPeptidesTable.convertColumnIndexToView(PTMPeptidesTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_ptmPeptidesTable.getModel());
        m_ptmPeptidesTable.setRowSorter(sorter);

        m_markerContainerPanel = new MarkerContainerPanel(m_ptmPeptidesScrollPane, m_ptmPeptidesTable);

        m_ptmPeptidesScrollPane.setViewportView(m_ptmPeptidesTable);

        m_ptmPeptidesTable.setFillsViewportHeight(true);
        m_ptmPeptidesTable.setViewport(m_ptmPeptidesScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

        JToolBar toolbar = initToolbar();
        ptmPeptidesPanel.add(toolbar, BorderLayout.WEST);
        ptmPeptidesPanel.add(internalPanel, BorderLayout.CENTER);

        return ptmPeptidesPanel;
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_ptmPeptidesTable.dataUpdated(subTask, finished);
        if (finished) {
            // allow to change column visibility
            //m_columnVisibilityButton.setEnabled(true);
            m_ptmPeptidesTable.setSortable(true);
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
        return (GlobalTableModelInterface) m_ptmPeptidesTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_ptmPeptidesTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_ptmPeptidesTable;
    }

    public void setSelectedRow(int i) {

        this.m_ptmPeptidesTable.setRowSelectionInterval(i, i);
    }

    @Override
    public void setShowed(boolean showed) {
        if (showed == m_isDisplayed) {
            return;
        }
        m_isDisplayed = showed;
        if (showed) {
            if (m_dataBox instanceof DataBoxPTMPeptides) {
                ((DataBoxPTMPeptides) m_dataBox).updateData();
            }
        }
    }

    @Override
    public boolean isShowed() {
        return m_isDisplayed;
    }

    private class PTMPeptidesTable extends LazyTable implements CrossSelectionInterface, InfoInterface, ProgressInterface {

        private boolean selectionWillBeRestored = false;

        public PTMPeptidesTable() {
            super(m_ptmPeptidesScrollPane.getVerticalScrollBar());
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
            
            if (e.getValueIsAdjusting()) {
                // value is adjusting, so valueChanged will be called again
                return;
            }
            
            m_dataBox.addDataChanged(PTMPeptideInstance.class, null);  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
            m_dataBox.addDataChanged(DPeptideMatch.class);
            m_dataBox.addDataChanged(MsQueryInfoRsm.class);
            m_dataBox.propagateDataChanged();
        }

        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
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
                    ((PTMPeptidesTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(PTMPeptidesTablePanel.this);

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
            StringBuilder sb = new StringBuilder().append(count);
            if (count > 1) {
                sb.append(" Peptides ");
            } else {
                sb.append(" Peptide ");
            }

            if (m_displayPeptidesMatches) {
                sb.append("Match ");
            }

            sb.append("for PTM Sites");

            return sb.toString();
        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
        }

    }

}
