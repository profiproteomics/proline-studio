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

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Peakel;
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
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.WindowManager;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class XicPeakelPanel  extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;

    private JScrollPane m_peakelScrollPane;
    private PeakelTable m_peakelTable;

    private MarkerContainerPanel m_markerContainerPanel;
    
    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private JButton m_graphicsButton;
    

    public XicPeakelPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());

        final JPanel peakelPanel = createPeakelPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                peakelPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(peakelPanel, JLayeredPane.DEFAULT_LAYER);


    }
        
    private JPanel createPeakelPanel() {

        JPanel peakelPanel = new JPanel();
        peakelPanel.setBounds(0, 0, 500, 400);
        peakelPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        peakelPanel.add(toolbar, BorderLayout.WEST);
        peakelPanel.add(internalPanel, BorderLayout.CENTER);

        return peakelPanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_peakelTable.getModel()), m_peakelTable);
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_peakelTable.getModel())) {

            @Override
            protected void filteringDone() {
                 m_dataBox.propagateDataChanged(ExtendedTableModelInterface.class);
            }
        };

        m_exportButton = new ExportButton(((CompoundTableModel) m_peakelTable.getModel()), "Peakels", m_peakelTable);

        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        
        // graphics button
        m_graphicsButton = new JButton(IconManager.getIcon(IconManager.IconType.CHART));
        m_graphicsButton.setToolTipText("Graphics : Histogram / Scatter Plot");
        m_graphicsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!((CompoundTableModel) m_peakelTable.getModel()).isLoaded()) {

                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), ((CompoundTableModel) m_peakelTable.getModel()), "Data loading", "Histogram functionnality is not available while data is loading. Please Wait.");
                    dialog.setLocation(getLocationOnScreen().x + m_graphicsButton.getWidth() + 5, m_graphicsButton.getLocationOnScreen().y + getHeight() + 5);
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        return;
                    }
                }
                // prepare window box
                WindowBox wbox = WindowBoxFactory.getGraphicsWindowBox("Peakel Graphic", m_dataBox, true);

                wbox.setEntryData(m_dataBox.getProjectId(), m_dataBox.getData(false, ExtendedTableModelInterface.class));

                // open a window to display the window box
                DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
                win.open();
                win.requestActive();
            }
        });
        toolbar.add(m_graphicsButton);
        
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
        m_peakelScrollPane = new JScrollPane();
        
        m_peakelTable = new PeakelTable();
        m_peakelTable.setModel(new CompoundTableModel(new PeakelTableModel((LazyTable)m_peakelTable), true));
        
        // hide the id column
        m_peakelTable.getColumnExt(m_peakelTable.convertColumnIndexToView(PeakelTableModel.COLTYPE_PEAKEL_ID)).setVisible(false);
        
        m_peakelTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_peakelScrollPane, m_peakelTable);
        
        m_peakelScrollPane.setViewportView(m_peakelTable);
        m_peakelTable.setFillsViewportHeight(true);
        m_peakelTable.setViewport(m_peakelScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        
        return internalPanel;
    }                 
    
    public void setData(Long taskId, Feature feature, List<Peakel> peakels, Color color, String title, boolean finished) {
        ((PeakelTableModel) m_peakelTable.getModel()).setData(taskId,  feature, peakels, color, title);

        // select the first row
        if ((peakels != null) && (peakels.size() > 0)) {
            m_peakelTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(peakels.size());
        }
        if (finished) {
            m_peakelTable.setSortable(true);
        }

    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_peakelTable.dataUpdated(subTask, finished);
        if (finished) {
           m_peakelTable.setSortable(true);
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
        return (GlobalTableModelInterface) m_peakelTable.getModel();
    }
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_peakelTable;
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

    public Peakel getSelectedPeakel() {
        return m_peakelTable.getSelectedPeakel();
    }
    
    public Feature getFeature(){
        return this.m_peakelTable.getFeature();
    }
    
    public Color getColor(){
        return this.m_peakelTable.getColor();
    }
    
    public String getTitle(){
        return this.m_peakelTable.getTitle();
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_peakelTable;
    }
    
    private class PeakelTable extends LazyTable implements ExportModelInterface {

        public PeakelTable() {
            super(m_peakelScrollPane.getVerticalScrollBar() );
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
 
            m_dataBox.propagateDataChanged(Peakel.class);

        }
        
        public boolean selectPeakel(Long peakelId, String searchText) {

            PeakelTableModel tableModel = (PeakelTableModel) ((CompoundTableModel)getModel()).getBaseModel();
            int row = tableModel.findRow(peakelId);
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
                ((PeakelTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
        


        public Peakel getSelectedPeakel() {

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
            PeakelTableModel peakelTableModel = (PeakelTableModel) compoundTableModel.getBaseModel();
            return peakelTableModel.getPeakel(selectedRow);

        }
        
        public Feature getFeature() {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            return tableModel.getFeature();
        }
        
        public Color getColor() {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            return tableModel.getColor();
        }
        
        public String getTitle() {
            PeakelTableModel tableModel = (PeakelTableModel) getModel();
            return tableModel.getTitle();
        }

        @Override
        public String getExportColumnName(int col) {
            return ((CompoundTableModel) m_peakelTable.getModel()).getExportColumnName(convertColumnIndexToModel(col));
        }
        
        @Override
        public String getExportRowCell(int row, int col) {
            return null; // no specific export
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return null;
        }


        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(XicPeakelPanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }



        
    }

}
