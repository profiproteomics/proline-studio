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

import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
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
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.table.CustomColumnControlButton;
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author JM235353
 */
public class XicParentPeptideIonPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_peptideIonScrollPane;
    private QuantChildrenPeptideIonTable m_quantChildrenPeptideIonTable;

    private MarkerContainerPanel m_markerContainerPanel;

    private DQuantitationChannel[] m_quantChannels;


    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    private InfoToggleButton m_infoToggleButton;

    private SearchToggleButton m_searchToggleButton;


    public XicParentPeptideIonPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        final JPanel peptideIonPanel = createPeptideIonPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptideIonPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peptideIonPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER + 1));

    }

    private JPanel createPeptideIonPanel() {

        JPanel peptideIonPanel = new JPanel();
        peptideIonPanel.setBounds(0, 0, 500, 400);
        peptideIonPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();

        peptideIonPanel.add(toolbar, BorderLayout.WEST);
        peptideIonPanel.add(internalPanel, BorderLayout.CENTER);

        return peptideIonPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_quantChildrenPeptideIonTable, m_quantChildrenPeptideIonTable, ((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel()));
        toolbar.add(m_searchToggleButton);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_quantChildrenPeptideIonTable.getModel()), m_quantChildrenPeptideIonTable);

        m_filterButton = new FilterButton(((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }

        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel()), "Peptides Ions", m_quantChildrenPeptideIonTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel())) {

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

        m_infoToggleButton = new InfoToggleButton(m_quantChildrenPeptideIonTable, m_quantChildrenPeptideIonTable);

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
        m_peptideIonScrollPane = new JScrollPane();

        m_quantChildrenPeptideIonTable = new QuantChildrenPeptideIonTable();
        m_quantChildrenPeptideIonTable.setModel(new CompoundTableModel(new QuantAggregatePeptideIonTableModel(), true));
        CustomColumnControlButton customColumnControl = new CustomColumnControlButton(m_quantChildrenPeptideIonTable);
        m_quantChildrenPeptideIonTable.setColumnControl(customColumnControl);

        m_quantChildrenPeptideIonTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_peptideIonScrollPane, m_quantChildrenPeptideIonTable);

        m_peptideIonScrollPane.setViewportView(m_quantChildrenPeptideIonTable);
        m_quantChildrenPeptideIonTable.setFillsViewportHeight(true);
        m_quantChildrenPeptideIonTable.setViewport(m_peptideIonScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;
    }

    public void setData(DMasterQuantPeptideIon aggregatedMasterQuantPeptideIon, List<DMasterQuantPeptideIon> masterQuantPeptideIonList, QuantChannelInfo quantChannelInfo, HashMap<Long, HashSet<Long>> aggregatedToChildrenQuantChannelsId, HashMap<Long, DQuantitationChannel> quantitationChannelsMap) {

        ((QuantAggregatePeptideIonTableModel) ((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel()).getBaseModel()).setData(aggregatedMasterQuantPeptideIon, masterQuantPeptideIonList, quantChannelInfo, aggregatedToChildrenQuantChannelsId, quantitationChannelsMap);

        // select the first row
        if ((masterQuantPeptideIonList != null) && (masterQuantPeptideIonList.size() > 0)) {
            m_quantChildrenPeptideIonTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(masterQuantPeptideIonList.size());
        }

        m_infoToggleButton.updateInfo();

        m_quantChildrenPeptideIonTable.setSortable(true);

        setColumnsVisibility();
    }

    public void setColumnsVisibility() {

        List<Integer> listIdsToHide = ((QuantAggregatePeptideIonTableModel) ((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel()).getBaseModel()).getDefaultColumnsToHide();
        List<TableColumn> columns = m_quantChildrenPeptideIonTable.getColumns(true);
        for (Integer id : listIdsToHide) {
            boolean columnVisible = ((TableColumnExt) columns.get(id)).isVisible();
            if (columnVisible) {
                m_quantChildrenPeptideIonTable.getColumnExt(m_quantChildrenPeptideIonTable.convertColumnIndexToView(id)).setVisible(false);
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
        return (GlobalTableModelInterface) m_quantChildrenPeptideIonTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_quantChildrenPeptideIonTable;
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

    
    public DMasterQuantPeptideIon getSelectedMasterQuantPeptideIon() {
        return m_quantChildrenPeptideIonTable.getSelectedMasterQuantPeptideIon();
    }
    
    public QuantChannelInfo getSelectedQuantChannelInfo() {
        return m_quantChildrenPeptideIonTable.getSelectedQuantChannelInfo();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_quantChildrenPeptideIonTable;
    }

    private class QuantChildrenPeptideIonTable extends LazyTable implements ExportModelInterface, InfoInterface {

        public QuantChildrenPeptideIonTable() {
            super(m_peptideIonScrollPane.getVerticalScrollBar());

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

        public QuantChannelInfo getSelectedQuantChannelInfo() {
            
            int selectedRow = getSelectedRowInModel();
            if (selectedRow == -1) {
                return null;
            }
            
            // Retrieve PeptideIon selected
            CompoundTableModel compoundTableModel = (CompoundTableModel) m_quantChildrenPeptideIonTable.getModel();
            QuantAggregatePeptideIonTableModel tableModel = (QuantAggregatePeptideIonTableModel) compoundTableModel.getBaseModel();
            return tableModel.getQuantChannelInfo(selectedRow);
        }
        
        public DMasterQuantPeptideIon getSelectedMasterQuantPeptideIon() {

            int selectedRow = getSelectedRowInModel();
            if (selectedRow == -1) {
                return null;
            }
            
            // Retrieve PeptideIon selected
            CompoundTableModel compoundTableModel = (CompoundTableModel) m_quantChildrenPeptideIonTable.getModel();
            QuantAggregatePeptideIonTableModel tableModel = (QuantAggregatePeptideIonTableModel) compoundTableModel.getBaseModel();
            return tableModel.getPeptideIon(selectedRow);
        }
        
        private int getSelectedRowInModel() {
            
            // Retrieve Selected Row
            int selectedRow = getSelectedRow();

            // nothing selected
            if (selectedRow == -1) {
                return -1;

            }

            CompoundTableModel compoundTableModel = (CompoundTableModel) m_quantChildrenPeptideIonTable.getModel();
            if (compoundTableModel.getRowCount() == 0) {
                return -1; // this is a wart, for an unknown reason, it happens that the first row
                // is selected although it does not exist.
            }

            // convert according to the sorting
            try {
                selectedRow = convertRowIndexToModel(selectedRow);
            } catch (Exception e) {
                // for unknow reason, an exception can occures due to the fact that the sorter is not ready
                m_loggerProline.debug("Exception catched as a wart : " + e.getMessage());
                return -1;
            }
            
            return compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel()).getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((CompoundTableModel) m_quantChildrenPeptideIonTable.getModel()).getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(XicParentPeptideIonPanel.this);

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
            return count + ((count > 1) ? " Peptides Ions" : " Peptide Ion");
        }

    }

}
