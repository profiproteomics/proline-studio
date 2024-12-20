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

import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.*;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.TableInfo;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.rsmexplorer.gui.dialog.CalcDialog;
import fr.proline.studio.rsmexplorer.gui.model.PeptideInstanceTableModel;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.WindowManager;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Panel for Peptides Instances
 *
 * @author JM235353
 */
public class RsmPeptidesPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    private PeptideInstanceTable m_peptideInstanceTable;
    private JScrollPane m_scrollPane;
    private MarkerContainerPanel m_markerContainerPanel;
    private JButton m_decoyButton;
    private InfoToggleButton m_infoToggleButton;
    private SearchToggleButton m_searchToggleButton;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    private JButton m_calcButton;
    
    public RsmPeptidesPanel() {
        initComponents();
    }

    private void initComponents() {


        setLayout(new BorderLayout());

        final JPanel peptidesPanel = createPeptidesPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peptidesPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peptidesPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);  
        layeredPane.add(m_searchToggleButton.getSearchPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER+1));

    }

    private String getTopComponentName() {

        Container c = getParent();
        while ((c != null) && !(c instanceof AbstractTopPanel)) {
            c = c.getParent();
        }
        if ((c != null) && (c instanceof AbstractTopPanel)) {
            return ((AbstractTopPanel) c).getTitle();
        }
        return "";
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_decoyButton = new JButton(IconManager.getIcon(IconManager.IconType.DATASET_RSM_DECOY));
        m_decoyButton.setToolTipText("Display Decoy Data");
        m_decoyButton.setEnabled(false);

        m_decoyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                ResultSummary rsm = (ResultSummary) m_dataBox.getData(ResultSummary.class);
                ResultSummary decoyRsm = rsm.getDecoyResultSummary();
                if (decoyRsm == null) {
                    return;
                }

                String savedWindow = SaveDataBoxActionListener.saveParentContainer("tmp", m_decoyButton);

                AbstractDataBox[] databoxes = WindowSavedManager.readBoxes(savedWindow);
                WindowBox wbox = WindowBoxFactory.getFromBoxesWindowBox("Decoy " + getTopComponentName(), databoxes, true, false, WindowSavedManager.SAVE_WINDOW_FOR_RSM);
                wbox.setEntryData(m_dataBox.getProjectId(), decoyRsm);

                // open a window to display the window box
                DataBoxViewerTopPanel win = new DataBoxViewerTopPanel(wbox);
                WindowManager.getDefault().getMainWindow().displayWindow(win);


            }
        });

        m_settingsButton = new SettingsButton(((ProgressInterface) m_peptideInstanceTable.getModel()), m_peptideInstanceTable);
        
        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_peptideInstanceTable, m_peptideInstanceTable, ((CompoundTableModel) m_peptideInstanceTable.getModel()));

        m_filterButton = new FilterButton(((CompoundTableModel) m_peptideInstanceTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }
            
        };
        
        m_exportButton = new ExportButton(((CompoundTableModel) m_peptideInstanceTable.getModel()), "Peptide Instances", m_peptideInstanceTable);
        
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_peptideInstanceTable.getModel())) {
           
            @Override
            public void actionPerformed() {
                JXTable table = getGlobalAssociatedTable();
                TableInfo tableInfo = new TableInfo(m_dataBox.getId(), m_dataBox.getUserName(), m_dataBox.getDataName(), m_dataBox.getTypeName(), table);
                Image i = m_dataBox.getIcon();
                if (i!=null) {
                    tableInfo.setIcon(new ImageIcon(i));
                }
                DataAnalyzerWindowBoxManager.addTableInfo(tableInfo);
            }
        };
        
        m_calcButton = new JButton(IconManager.getIcon(IconManager.IconType.CALCULATOR));
        m_calcButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CalcDialog dialog = CalcDialog.getCalcDialog(WindowManager.getDefault().getMainWindow(), m_peptideInstanceTable);
                dialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);

            }

        });

        m_infoToggleButton = new InfoToggleButton(m_peptideInstanceTable, m_peptideInstanceTable);
        
        
        toolbar.add(m_decoyButton);
        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        toolbar.add(m_infoToggleButton);
        toolbar.add(m_calcButton);
        
        return toolbar;
    }

    private JPanel createPeptidesPanel() {
        JPanel peptidesPanel = new JPanel();
        peptidesPanel.setBounds(0, 0, 500, 400);

        peptidesPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        peptidesPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        peptidesPanel.add(toolbar, BorderLayout.WEST);

        return peptidesPanel;

    }

    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);



        // create objects
        m_scrollPane = new JScrollPane();

        m_peptideInstanceTable = new PeptideInstanceTable();
        m_peptideInstanceTable.setModel(new CompoundTableModel(new PeptideInstanceTableModel((LazyTable) m_peptideInstanceTable), true));
        m_peptideInstanceTable.getColumnExt(m_peptideInstanceTable.convertColumnIndexToView(PeptideInstanceTableModel.COLTYPE_PEPTIDE_ID)).setVisible(false);
        
        
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, (PeptideInstanceTable) m_peptideInstanceTable);


        m_scrollPane.setViewportView(m_peptideInstanceTable);
        m_peptideInstanceTable.setFillsViewportHeight(true);
        m_peptideInstanceTable.setViewport(m_scrollPane.getViewport());




        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);

        return internalPanel;
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
        return (GlobalTableModelInterface) m_peptideInstanceTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_peptideInstanceTable;
    }
    
    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peptideInstanceTable;
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

    public void setData(long taskId, PeptideInstance[] peptideInstances, boolean finished) {

        ResultSummary rsm = (ResultSummary) m_dataBox.getData(ResultSummary.class);
        if (rsm != null) {
            m_decoyButton.setEnabled(rsm.getDecoyResultSummary() != null);
        }

        ((PeptideInstanceTableModel) ((CompoundTableModel) m_peptideInstanceTable.getModel()).getBaseModel()).setData(taskId, peptideInstances);


        // select the first row
        if ((peptideInstances != null) && (peptideInstances.length > 0)) {
            m_peptideInstanceTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peptideInstances.length);
            
        }
        
        m_infoToggleButton.updateInfo();

        if (finished) {
            m_peptideInstanceTable.setSortable(true);
        }
    }

    public void dataUpdated(SubTask subTask, boolean finished) {
        m_peptideInstanceTable.dataUpdated(subTask, finished);
    }

    public PeptideInstance getSelectedPeptideInstance() {

        // Retrieve Selected Row
        int selectedRow = m_peptideInstanceTable.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = m_peptideInstanceTable.convertRowIndexToModel(selectedRow);

        CompoundTableModel compoundTableModel = ((CompoundTableModel)m_peptideInstanceTable.getModel());
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);

        // Retrieve ProteinSet selected
        PeptideInstanceTableModel tableModel = (PeptideInstanceTableModel) compoundTableModel.getBaseModel();
        return tableModel.getPeptideInstance(selectedRow);
    }

    private class PeptideInstanceTable extends LazyTable implements InfoInterface {

        public PeptideInstanceTable() {
            super(m_scrollPane.getVerticalScrollBar());
            //displayColumnAsPercentage(PeptideInstanceTableModel.COLTYPE_PEPTIDE_SCORE);
        }


        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
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
            
            m_dataBox.addDataChanged(PeptideInstance.class);
            m_dataBox.addDataChanged(DPeptideMatch.class);
            m_dataBox.propagateDataChanged();

        }

        public boolean selectPeptideInstance(Long peptideInstanceId, String searchText) {

            PeptideInstanceTableModel tableModel = (PeptideInstanceTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(peptideInstanceId);
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
                    ((PeptideInstanceTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
                } finally {
                    selectionWillBeRestored(false);
                }



                // restore selected row
                if (rowSelectedInModel != -1) {
                    int rowSelectedInView = convertRowIndexToView(rowSelectedInModel);
                    setSelection(rowSelectedInView);

                    // if the subtask correspond to the loading of the data of the sorted column,
                    // we keep the row selected visible
                    if (((keepLastAction == LastAction.ACTION_SELECTING) || (keepLastAction == LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId(getSortedColumnIndex()))) {
                        ((PeptideInstanceTable) m_peptideInstanceTable).scrollRowToVisible(rowSelectedInView);
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
            //((SearchButton)searchButton).sortingChanged();
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
        
        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(RsmPeptidesPanel.this);

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
