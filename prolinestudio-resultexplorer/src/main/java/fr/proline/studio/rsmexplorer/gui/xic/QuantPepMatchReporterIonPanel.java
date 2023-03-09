package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.MasterQuantReporterIon;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.extendedtablemodel.*;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.table.CustomColumnControlButton;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.table.TablePopupMenu;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

public class QuantPepMatchReporterIonPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_reporterIonScrollPane;
    private QuantReporterIonTable m_quantReporterIonTable;

    private MarkerContainerPanel m_markerContainerPanel;

    private DQuantitationChannel[] m_quantChannels;
    private DDatasetType.QuantitationMethodInfo m_quantMethodInfo;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    private InfoToggleButton m_infoToggleButton;

    private SearchToggleButton m_searchToggleButton;

//    private boolean m_isAllPeptideIon = false;
//
//    public void setIsAllPeptideIon(boolean isAllPeptideIon) {
//        this.m_isAllPeptideIon = isAllPeptideIon;
//    }

    public QuantPepMatchReporterIonPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        final JPanel psmReporterIonPanel = createPepMatchReporterIonPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                psmReporterIonPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(psmReporterIonPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER + 1));

    }

    private JPanel createPepMatchReporterIonPanel() {

        JPanel pepMatchRepIonIonPanel = new JPanel();
        pepMatchRepIonIonPanel.setBounds(0, 0, 500, 400);
        pepMatchRepIonIonPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();

        pepMatchRepIonIonPanel.add(toolbar, BorderLayout.WEST);
        pepMatchRepIonIonPanel.add(internalPanel, BorderLayout.CENTER);

        return pepMatchRepIonIonPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_quantReporterIonTable, m_quantReporterIonTable, ((CompoundTableModel) m_quantReporterIonTable.getModel()));
        toolbar.add(m_searchToggleButton);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_quantReporterIonTable.getModel()), m_quantReporterIonTable);

        m_filterButton = new FilterButton(((CompoundTableModel) m_quantReporterIonTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }

        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_quantReporterIonTable.getModel()), "Peptide Matches Reporter Ions", m_quantReporterIonTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_quantReporterIonTable.getModel())) {

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

        m_infoToggleButton = new InfoToggleButton(m_quantReporterIonTable, m_quantReporterIonTable);

        toolbar.add(m_infoToggleButton);

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
        m_reporterIonScrollPane = new JScrollPane();

        m_quantReporterIonTable = new QuantReporterIonTable();
        m_quantReporterIonTable.setModel(new CompoundTableModel(new QuantPepMatchReporterIonTableModel(m_quantReporterIonTable), true));
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_quantReporterIonTable);
        m_quantReporterIonTable.setColumnControl(customColumnControl);

        m_quantReporterIonTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_reporterIonScrollPane, m_quantReporterIonTable);

        m_reporterIonScrollPane.setViewportView(m_quantReporterIonTable);
        m_quantReporterIonTable.setFillsViewportHeight(true);
        m_quantReporterIonTable.setViewport(m_reporterIonScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;
    }

    public void setData(Long taskId, DQuantitationChannel[] quantChannels, List<MasterQuantReporterIon> masterQuantRepIonsList, DDatasetType.QuantitationMethodInfo quantitationMethodInfo, boolean finished) {
        boolean qcChanged = true;
        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int q = 0; q < m_quantChannels.length; q++) {
                qcChanged = !(m_quantChannels[q].equals(quantChannels[q]));
            }
        }
        m_quantChannels = quantChannels;
        m_quantMethodInfo = quantitationMethodInfo;

        ((QuantPepMatchReporterIonTableModel) ((CompoundTableModel) m_quantReporterIonTable.getModel()).getBaseModel()).setData(taskId, quantChannels, masterQuantRepIonsList);
        //m_quantPeptideIonTable.setColumnControlVisible(((QuantPeptideIonTableModel) ((CompoundTableModel) m_quantPeptideIonTable.getModel()).getBaseModel()).getColumnCount() < XicProteinSetPanel.NB_MAX_COLUMN_CONTROL);

        // select the first row
        if ((masterQuantRepIonsList != null) && (masterQuantRepIonsList.size() > 0)) {
            m_quantReporterIonTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(masterQuantRepIonsList.size());
        }

        m_infoToggleButton.updateInfo();

        if (finished) {
            // allow to change column visibility
            //m_columnVisibilityButton.setEnabled(true);
            m_quantReporterIonTable.setSortable(true);
        }
        if (qcChanged) {
            setColumnsVisibility();
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_quantReporterIonTable.dataUpdated(subTask, finished);
        if (finished) {
            // allow to change column visibility
            //m_columnVisibilityButton.setEnabled(true);
            m_quantReporterIonTable.setSortable(true);
        }
    }

    public void setColumnsVisibility() {
//        if (m_isAllPeptideIon) {
//            int id = QuantPeptideIonTableModel.COLTYPE_PEPTIDE_ION_STATUS;
//            m_quantReporterIonTable.getColumnExt(m_quantReporterIonTable.convertColumnIndexToView(id)).setVisible(false);
//        }
        // hide the rawAbundance  and selectionLevel columns
        java.util.List<Integer> listIdsToHide = ((QuantPepMatchReporterIonTableModel) ((CompoundTableModel) m_quantReporterIonTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
        List<TableColumn> columns = m_quantReporterIonTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_quantReporterIonTable.getColumnExt(m_quantReporterIonTable.convertColumnIndexToView(id)).setVisible(false);
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
        return (GlobalTableModelInterface) m_quantReporterIonTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_quantReporterIonTable;
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

    public MasterQuantReporterIon getSelectedMasterQuantReporterIon() {
        return m_quantReporterIonTable.getSelectedMasterQuantReporterIon();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_quantReporterIonTable;
    }

    private class QuantReporterIonTable extends LazyTable implements ExportModelInterface, InfoInterface {
        private DisplayTablePopupMenu m_popupMenu;
        String searchTextBeingDone = null;
        private boolean selectionWillBeRestored = false;

        public QuantReporterIonTable() {
            super(m_reporterIonScrollPane.getVerticalScrollBar());
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

            m_dataBox.addDataChanged(DMasterQuantPeptideIon.class);
            m_dataBox.propagateDataChanged();
        }

        public boolean selectPeptideMatchRepIon(Long peptideMatchRepIonId, String searchText) {

            QuantPepMatchReporterIonTableModel tableModel = (QuantPepMatchReporterIonTableModel) ((CompoundTableModel) getModel()).getBaseModel();
            int row = tableModel.findRow(peptideMatchRepIonId);
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


        public void dataUpdated(SubTask subTask, boolean finished) {

            LastAction keepLastAction = m_lastAction;
            try {

                // retrieve selected row
                int rowSelected = getSelectionModel().getMinSelectionIndex();
                int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

                // Update Model (but protein set table must not react to the model update)
                selectionWillBeRestored(true);
                try {
                    ((QuantPeptideIonTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        public MasterQuantReporterIon getSelectedMasterQuantReporterIon() {

            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return null;

            }

            CompoundTableModel compoundTableModel = (CompoundTableModel) m_quantReporterIonTable.getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return null; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            try {
                selectedRow = convertRowIndexToModel(selectedRow);
            } catch (Exception e) {
                // for unknow reason, an exception can occures due to the fact that the sorter is not ready
                m_loggerProline.debug("Exception catched as a wart : " + e.getMessage());
                return null;
            }
            selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

            // Retrieve PeptideIon selected
            QuantPepMatchReporterIonTableModel tableModel = (QuantPepMatchReporterIonTableModel) compoundTableModel.getBaseModel();
            return tableModel.getPeptideMatchRepIon(selectedRow);
        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_quantReporterIonTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) m_quantReporterIonTable.getModel()).getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((CompoundTableModel) m_quantReporterIonTable.getModel()).getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(QuantPepMatchReporterIonPanel.this);
            return m_popupMenu;
        }

        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
            return count + ((count > 1) ? " Peptides Ions" : " Peptide Ion");
        }

    }

}

