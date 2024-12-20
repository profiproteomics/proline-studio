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


import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author MB243701
 */
public class QuantChannelPeptideMatchPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_psmScrollPane;
    private XicPeptideMatchTable m_psmTable;

    private MarkerContainerPanel m_markerContainerPanel;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;


    public QuantChannelPeptideMatchPanel() {
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

        JPanel psmPanel = new JPanel();
        psmPanel.setBounds(0, 0, 500, 400);
        psmPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();

        psmPanel.add(toolbar, BorderLayout.WEST);
        psmPanel.add(internalPanel, BorderLayout.CENTER);

        return psmPanel;
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_psmTable.getModel()), m_psmTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_psmTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
            }
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_psmTable.getModel()), "PSM", m_psmTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);

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
        m_psmScrollPane = new JScrollPane();

        m_psmTable = new XicPeptideMatchTable();
        m_psmTable.setModel(new CompoundTableModel(new QuantChannelPeptideMatchTableModel((LazyTable) m_psmTable), true));

        // hide the id column
        m_psmTable.getColumnExt(m_psmTable.convertColumnIndexToView(QuantChannelPeptideMatchTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);

        m_psmTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_psmScrollPane, m_psmTable);

        m_psmScrollPane.setViewportView(m_psmTable);
        m_psmTable.setFillsViewportHeight(true);
        m_psmTable.setViewport(m_psmScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;
    }

    public void setData(Long taskId, DQuantitationChannel[] quantChannelArray, QuantChannelInfo quantChannelInfo, List<DPeptideMatch> peptideMatchList, Map<Long, List<Long>> peptideMatchIdListPerQC, boolean finished) {
        ((QuantChannelPeptideMatchTableModel) (((CompoundTableModel) m_psmTable.getModel()).getBaseModel())).setData(taskId, quantChannelArray, quantChannelInfo, peptideMatchList, peptideMatchIdListPerQC);
        int nb = 0;
        if (peptideMatchList != null) {
            nb = peptideMatchList.size();
        }

        // select the first row
        if ((nb > 0)) {
            m_psmTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(nb);
        }
        if (finished) {
            m_psmTable.setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_psmTable.dataUpdated(subTask, finished);
        if (finished) {
            m_psmTable.setSortable(true);
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
        return (GlobalTableModelInterface) m_psmTable.getModel();
    }
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_psmTable;
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

    public DPeptideMatch getSelectedPSM() {
        return m_psmTable.getSelectedPSM();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_psmTable;
    }

    private class XicPeptideMatchTable extends LazyTable implements ExportModelInterface {

        
        public XicPeptideMatchTable() {
            super(m_psmScrollPane.getVerticalScrollBar());

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

            m_dataBox.addDataChanged(DPeptideMatch.class);
            m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
            m_dataBox.propagateDataChanged();

        }

        public boolean selectPSM(Long peptideMatchId, String searchText) {

            QuantChannelPeptideMatchTableModel tableModel = (QuantChannelPeptideMatchTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(peptideMatchId);
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
                    ((QuantChannelPeptideMatchTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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


        public DPeptideMatch getSelectedPSM() {

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
            QuantChannelPeptideMatchTableModel tableModel = (QuantChannelPeptideMatchTableModel) compoundTableModel.getBaseModel();
            return tableModel.getPSM(selectedRow);

        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_psmTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((CompoundTableModel) getModel()).getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }
        
        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((CompoundTableModel) getModel()).getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
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


        
        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(QuantChannelPeptideMatchPanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }

    }
}
