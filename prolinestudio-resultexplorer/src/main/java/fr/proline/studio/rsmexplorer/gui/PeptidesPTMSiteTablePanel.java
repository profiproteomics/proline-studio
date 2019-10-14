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
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataAnalyzerWindowBoxManager;
import fr.proline.studio.pattern.MsQueryInfoRset;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.model.PeptidesOfPTMSiteTableModel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
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
import java.util.Comparator;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PeptidesPTMSiteTablePanel extends JPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface, PeptidesPTMSitePanelInterface {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.PeptidesPTMSiteTablePanel");
    protected AbstractDataBox m_dataBox;

    private JScrollPane m_peptidesPtmSiteScrollPane;
    protected PeptidesPTMSiteTable m_peptidesPtmSiteTable;
    protected MarkerContainerPanel m_markerContainerPanel;

    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;

    protected PTMSite m_currentPTMSite = null;
    protected DPeptideInstance m_currentPepInst = null;

    private final boolean m_displayPeptidesMatches;

    /**
     * Display Peptides Matches of a PTMSite.
     *
     * @param viewAll if true display all Peptides Matches of all Peptide
     * Instance for this PTMSite. If false, display only best PeptideMatch of
     * all Peptide Instance for this PTMSite.
     *
     */
    public PeptidesPTMSiteTablePanel(boolean viewAll) {
        m_displayPeptidesMatches = viewAll;
        initComponents();
    }

    @Override
    public PTMSite getSelectedPTMSite() {

        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // Retrieve ProteinPTMSite selected
        PeptidesOfPTMSiteTableModel tableModel = (PeptidesOfPTMSiteTableModel) compoundTableModel.getBaseModel();
        return tableModel.getCurrentPTMSite();
    }

    public DPeptideMatch getSelectedPeptideMatchSite() {

        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // Retrieve ProteinPTMSite selected
        PeptidesOfPTMSiteTableModel tableModel = (PeptidesOfPTMSiteTableModel) compoundTableModel.getBaseModel();
        if (tableModel.getRowCount() <= 0) {
            return null;
        }
        // Retrieve Selected Row
        int selectedRow = getSelectedTableModelRow();

        return tableModel.getSelectedPeptideMatchSite(selectedRow);
    }

    @Override
    public DPeptideInstance getSelectedPeptideInstance() {
        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // Retrieve ProteinPTMSite selected
        PeptidesOfPTMSiteTableModel tableModel = (PeptidesOfPTMSiteTableModel) compoundTableModel.getBaseModel();

        if (tableModel.getRowCount() <= 0) {
            return null;
        }

        // Retrieve Selected Row
        int selectedRow = getSelectedTableModelRow();

        return tableModel.getSelectedPeptideInstance(selectedRow);
    }

    /**
     * the selected Index in the table is different from it's index in
     * tableModel, which is in orignal order without sorting nor filting
     *
     * @return
     */
    public int getSelectedTableModelRow() {

        // Retrieve Selected Row
        int selectedRow = m_peptidesPtmSiteTable.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return -1;
        }
        CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
        // convert according to the sorting
        selectedRow = m_peptidesPtmSiteTable.convertRowIndexToModel(selectedRow);
        //find the original index
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);
        return selectedRow;
    }

    @Override
    public void setSelectedPeptide(DPeptideInstance pep) {
        PeptidesOfPTMSiteTableModel model = ((PeptidesOfPTMSiteTableModel) ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()).getBaseModel());
        int index = model.getSelectedIndex(pep); //this is the model original index
        //now find the real row perhaps after sorting or filting
        if (index != -1) {
            //find the original index
            CompoundTableModel compoundTableModel = ((CompoundTableModel) m_peptidesPtmSiteTable.getModel());
            int selectedRow = compoundTableModel.convertBaseModelRowToCompoundRow(index);
            // convert according to the sorting
            index = m_peptidesPtmSiteTable.convertRowIndexToView(selectedRow);

            this.setSelectedRow(index);
        }
    }

    @Override
    public void setData(PTMSite peptidesPTMSite, DPeptideInstance pepInst) {

        if ((peptidesPTMSite == m_currentPTMSite) && (pepInst == m_currentPepInst)) {
            return;
        }

        m_currentPTMSite = peptidesPTMSite;
        m_currentPepInst = pepInst;

        PeptidesOfPTMSiteTableModel model = ((PeptidesOfPTMSiteTableModel) ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()).getBaseModel());
        model.setData(peptidesPTMSite, m_displayPeptidesMatches, pepInst);
        // select the first row
        if ((m_currentPTMSite != null) && ((!m_displayPeptidesMatches) || (m_currentPepInst != null))) {
            m_peptidesPtmSiteTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(m_peptidesPtmSiteTable.getModel().getRowCount());
        }

        m_infoToggleButton.updateInfo();
        m_peptidesPtmSiteTable.setSortable(true);
    }

    private void initComponents() {

        setLayout(new BorderLayout());

        final JPanel peptidePTMSitePanel = createPeptidesPTMSitePanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptidePTMSitePanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peptidePTMSitePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), new Integer(JLayeredPane.PALETTE_LAYER + 1));

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        // Search Button
        m_exportButton = new ExportButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel()), "Peptides", m_peptidesPtmSiteTable);
        m_searchToggleButton = new SearchToggleButton(m_peptidesPtmSiteTable, m_peptidesPtmSiteTable, ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()));
        m_settingsButton = new SettingsButton(((ProgressInterface) m_peptidesPtmSiteTable.getModel()), m_peptidesPtmSiteTable);
        m_infoToggleButton = new InfoToggleButton(m_peptidesPtmSiteTable, m_peptidesPtmSiteTable);
        m_filterButton = new FilterButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel())) {
            @Override
            protected void filteringDone() {
                m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class
                );
                m_infoToggleButton.updateInfo();
            }
        };

        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_peptidesPtmSiteTable.getModel())) {

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

    private JPanel createPeptidesPTMSitePanel() {

        JPanel peptidesPTMSitePanel = new JPanel();
        peptidesPTMSitePanel.setBounds(0, 0, 500, 400);
        peptidesPTMSitePanel.setLayout(new BorderLayout());

        //--- Create Internal Pane
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        m_peptidesPtmSiteScrollPane = new JScrollPane();

        m_peptidesPtmSiteTable = new PeptidesPTMSiteTable();
        PeptidesOfPTMSiteTableModel model = new PeptidesOfPTMSiteTableModel();
        m_peptidesPtmSiteTable.setModel(new CompoundTableModel(model, true));

        // hide the id column
        m_peptidesPtmSiteTable.getColumnExt(m_peptidesPtmSiteTable.convertColumnIndexToView(PeptidesOfPTMSiteTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        //m_ptmProteinSiteTable.displayColumnAsPercentage(PtmProtenSiteTableModel_V2.COLTYPE_PEPTIDE_SCORE);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_peptidesPtmSiteTable.getModel());
        m_peptidesPtmSiteTable.setRowSorter(sorter);

        sorter.setComparator(PeptidesOfPTMSiteTableModel.COLTYPE_MODIFICATION_LOC, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                int pos1;
                if (s1.compareTo("N-term") == 0) {
                    pos1 = -1;
                } else if (s1.compareTo("C-term") == 0) {
                    pos1 = Integer.MAX_VALUE;
                } else {
                    pos1 = Integer.valueOf(s1);
                }
                int pos2;
                if (s2.compareTo("N-term") == 0) {
                    pos2 = 0;
                } else if (s2.compareTo("C-term") == 0) {
                    pos2 = Integer.MAX_VALUE;
                } else {
                    pos2 = Integer.valueOf(s2);
                }

                return pos2 - pos1;
            }

        });

        sorter.setComparator(PeptidesOfPTMSiteTableModel.COLTYPE_PROTEIN_LOC, new Comparator<Integer>() {

            @Override
            public int compare(Integer s1, Integer s2) {
                return s1 - s2;
            }

        });

        m_markerContainerPanel = new MarkerContainerPanel(m_peptidesPtmSiteScrollPane, m_peptidesPtmSiteTable);

        m_peptidesPtmSiteScrollPane.setViewportView(m_peptidesPtmSiteTable);
        m_peptidesPtmSiteTable.setFillsViewportHeight(true);
        m_peptidesPtmSiteTable.setViewport(m_peptidesPtmSiteScrollPane.getViewport());

//
//        m_countModificationTextField = new JTextField();
//        m_countModificationTextField.setEditable(false);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

//        c.gridx++;
//        c.gridy++;
//        c.weightx = 1;
//        c.gridwidth = 1;
//        internalPanel.add(m_countModificationTextField, c);
//        
        JToolBar toolbar = initToolbar();
        peptidesPTMSitePanel.add(toolbar, BorderLayout.WEST);
        peptidesPTMSitePanel.add(internalPanel, BorderLayout.CENTER);

        return peptidesPTMSitePanel;
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
        return (GlobalTableModelInterface) m_peptidesPtmSiteTable.getModel();
    }

    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_peptidesPtmSiteTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peptidesPtmSiteTable;
    }

    @Override
    public void setLoading(int id) {
        //Nothing to Do
    }

    @Override
    public void setLoading(int id, boolean calculating) {
        //Nothing to Do
    }

    @Override
    public void setLoaded(int id) {
        //Nothing to Do
    }

    public void setSelectedRow(int i) {

        this.m_peptidesPtmSiteTable.setRowSelectionInterval(i, i);
    }

    private class PeptidesPTMSiteTable extends DecoratedMarkerTable implements CrossSelectionInterface, InfoInterface, ProgressInterface {

        private boolean selectionWillBeRestored = false;

        public PeptidesPTMSiteTable() {
            super();
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

            if (m_displayPeptidesMatches) {
                m_dataBox.propagateDataChanged(DPeptideMatch.class);
                m_dataBox.propagateDataChanged(MsQueryInfoRset.class);
            } else {
                m_dataBox.propagateDataChanged(PTMSite.class);//VDS Peptide ?
            }
        }

        public void selectionWillBeRestored(boolean b) {
            selectionWillBeRestored = b;
        }

//        @Override
//        public void importSelection(HashSet selectedData) {
//            ListSelectionModel selectionTableModel = getSelectionModel();
//            selectionTableModel.clearSelection();
//
//            int firstRow = -1;
//            PtmProtenSiteTableModel_V2 model = (PtmProtenSiteTableModel_V2) ((CompoundTableModel) m_peptidesPtmSiteTable.getModel()).getBaseModel();
//            int rowCount = model.getRowCount();
//            for (int i = 0; i < rowCount; i++) {
//                Object v = model.getValueAt(i, PtmProtenSiteTableModel_V2.COLTYPE_PROTEIN_ID);
//                if (selectedData.remove(v)) {
//                    if (firstRow == -1) {
//                        firstRow = i;
//                    }
//                    selectionTableModel.addSelectionInterval(i, i);
//                    BookmarkMarker marker = new BookmarkMarker(i);
//                    m_markerContainerPanel.addMarker(marker);
//                    if (selectedData.isEmpty()) {
//                        break;
//                    }
//                }
//            }
//
//            // scroll to the first row
//            if (firstRow != -1) {
//                final int row = firstRow;
//                scrollToVisible(row);
//            }
//
//            throw new RuntimeException(" HA HA NEEDED !!");
//        }
        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(PeptidesPTMSiteTablePanel.this);

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
            StringBuilder sb = new StringBuilder(count);
            if (count > 1) {
                sb.append(" Peptides ");
            } else {
                sb.append(" Peptide ");
            }

            if (m_displayPeptidesMatches) {
                sb.append("Match ");
            }

            sb.append("for PTM Site");

            return sb.toString();
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

    }

}
