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



import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.extendedtablemodel.AddDataAnalyzerButton;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.export.ExportButton;
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
import fr.proline.studio.rsmexplorer.gui.model.ProteinTableModel;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.URLCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
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
import org.jdesktop.swingx.JXTable;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * In : Window which display Protein Sets of a Result Summary - Panel used to display Proteins of a Protein Set (at the
 * center of the window)
 *
 * @author JM235353
 */
public class RsmProteinsOfProteinSetPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;
    private DProteinSet m_proteinSetCur = null;

    private JTextField m_proteinNameTextField;
    private ProteinTable m_proteinTable;
    private JScrollPane m_scrollPane;

    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private AddDataAnalyzerButton m_addCompareDataButton;
    private InfoToggleButton m_infoToggleButton;
    
    private MarkerContainerPanel m_markerContainerPanel;
    
    /**
     * Creates new form RsmProteinsOfProteinSetPanel
     */
    public RsmProteinsOfProteinSetPanel() {
        
        initComponents();
    }
    
    private void initComponents() {


        setLayout(new BorderLayout());

        final JPanel proteinPanel = createProteinPanel();


        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                proteinPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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

        layeredPane.add(proteinPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);  


    }
    
    
    
    private JPanel createProteinPanel() {
        JPanel proteinPanel = new JPanel();
        proteinPanel.setBounds(0, 0, 500, 400);

        proteinPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        proteinPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        proteinPanel.add(toolbar, BorderLayout.WEST);


        return proteinPanel;

    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        
        m_proteinNameTextField = new JTextField();
        m_proteinNameTextField.setEditable(false);
        m_proteinNameTextField.setBackground(Color.white);
        
        
        m_scrollPane = new javax.swing.JScrollPane();
        m_proteinTable = new ProteinTable();
        CompoundTableModel model = new CompoundTableModel(new ProteinTableModel((ProgressInterface) m_proteinTable), true);
        m_proteinTable.setModel(model);
        
        
        m_markerContainerPanel = new MarkerContainerPanel(m_scrollPane, m_proteinTable);
        
        m_scrollPane.setViewportView(m_proteinTable);
        m_proteinTable.setFillsViewportHeight(true);
        m_proteinTable.setViewport(m_scrollPane.getViewport());

        
        URLCellRenderer urlRenderer = (URLCellRenderer) model.getRenderer(0, ProteinTableModel.Column.PROTEIN_NAME.ordinal());
        m_proteinTable.addMouseListener(urlRenderer);
        m_proteinTable.addMouseMotionListener(urlRenderer);
        
        // hide the id column  (must be done after the URLCellRenderer is set)
        m_proteinTable.getColumnExt(m_proteinTable.convertColumnIndexToView(ProteinTableModel.Column.PROTEIN_ID.ordinal())).setVisible(false);
        
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(new JLabel("Typical Protein:"), c);
        
        c.gridx++;
        c.weightx = 1;
        internalPanel.add(m_proteinNameTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weighty = 1;
        internalPanel.add(m_markerContainerPanel, c);
        
        return internalPanel;
        

    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_settingsButton = new SettingsButton(((ProgressInterface) m_proteinTable.getModel()), m_proteinTable);
        
        
        m_filterButton = new FilterButton(((CompoundTableModel) m_proteinTable.getModel())) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }
            
        };
        m_exportButton = new ExportButton(((CompoundTableModel) m_proteinTable.getModel()), "Proteins", m_proteinTable);
        m_addCompareDataButton = new AddDataAnalyzerButton(((CompoundTableModel) m_proteinTable.getModel())) {
           
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
        
        m_infoToggleButton = new InfoToggleButton(m_proteinTable, m_proteinTable);
        
        
        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_addCompareDataButton);
        toolbar.add(m_infoToggleButton);

        return toolbar;
    }

    public DProteinMatch getSelectedProteinMatch() {

        ProteinTable table = (ProteinTable) m_proteinTable;

        // Retrieve Selected Row
        int selectedRow = table.getSelectedRow();


        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        CompoundTableModel compoundTableModel = (CompoundTableModel) table.getModel();
        if (compoundTableModel.getRowCount() == 0) {
            return null; // this is a wart, for an unknown reason, it happens that the first row
            // is selected although it does not exist.
        }
        
        // convert according to the sorting
        selectedRow = table.convertRowIndexToModel(selectedRow);
        selectedRow = compoundTableModel.convertCompoundRowToBaseModelRow(selectedRow);


        // Retrieve ProteinMatch selected
        ProteinTableModel tableModel = (ProteinTableModel) compoundTableModel.getBaseModel();
        return tableModel.getProteinMatch(selectedRow);
    }

    public void setData(DProteinSet proteinSet, String searchedText) {

        if (proteinSet == m_proteinSetCur) {
            return;
        }
        m_proteinSetCur = proteinSet;

        if (proteinSet == null) {
            clearData();
            return;
        }

        // retrieve sameset and subset
        DProteinMatch[] sameSetArray = proteinSet.getSameSet();
        DProteinMatch[] subSetArray = proteinSet.getSubSet();

        // retrieve Typical Protein Match
        DProteinMatch typicalProtein = proteinSet.getTypicalProteinMatch();

        
        if (typicalProtein == null) {
            // data not ready
            clearData();
            return;
        }
        

        // Modify protein description
        m_proteinNameTextField.setText(typicalProtein.getDescription());


        // Modify the Model
        ((ProteinTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).setData(proteinSet.getResultSummaryId(), proteinSet.getTypicalProteinMatch().getId() ,sameSetArray, subSetArray);
        
        
        // Select the Row
        int row = ((ProteinTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).findRowToSelect(searchedText);
        if (row != -1) {
            row = ((CompoundTableModel) m_proteinTable.getModel()).convertBaseModelRowToCompoundRow(row);
        }

        m_proteinTable.getSelectionModel().setSelectionInterval(row, row);
        m_markerContainerPanel.setMaxLineNumber(proteinSet.getSameSetCount()+proteinSet.getSubSetCount());
        m_markerContainerPanel.removeAllMarkers();

        m_infoToggleButton.updateInfo();
        
        m_proteinTable.setSortable(true);
        
        
    }

    private void clearData() {
        m_proteinNameTextField.setText("");
        ((ProteinTableModel) (((CompoundTableModel)  m_proteinTable.getModel()).getBaseModel())).setData(-1, -1, null, null);

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
        return (GlobalTableModelInterface) m_proteinTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_proteinTable;
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
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_proteinTable;
    }





    private class ProteinTable extends DecoratedMarkerTable implements ProgressInterface, InfoInterface {

        public ProteinTable() {

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

            if (e.getValueIsAdjusting()) {
                // value is adjusting, so valueChanged will be called again
                return;
            }
            
            m_dataBox.addDataChanged(DProteinMatch.class);
            m_dataBox.propagateDataChanged();

        }

        @Override
        public boolean isLoaded() {
            return m_dataBox.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return m_dataBox.getLoadingPercentage();
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(RsmProteinsOfProteinSetPanel.this);

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
            return count+((count>1) ? " Proteins" : " Protein");
        }
        
    }
}
