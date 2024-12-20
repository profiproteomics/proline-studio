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

import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.actions.ClearRestrainAction;
import fr.proline.studio.filter.actions.RestrainAction;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.info.InfoToggleButton;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.search.SearchToggleButton;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
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
 * @author JM235353
 */
public class GenericPanel extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface {

    private AbstractDataBox m_dataBox;


    private SettingsButton m_settingsButton;
    private FilterButton m_filterButton;
    private ExportButton m_exportButton;
    private SearchToggleButton m_searchToggleButton;
    private InfoToggleButton m_infoToggleButton;
    
    private JScrollPane m_dataScrollPane = new JScrollPane();
    private DataTable m_dataTable;
    private MarkerContainerPanel m_markerContainerPanel;
    
    public GenericPanel(boolean removeStripAndSort) {
        
        setLayout(new BorderLayout());

        final JPanel resultPanel = createResultPanel(removeStripAndSort);

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                resultPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
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
        
        layeredPane.add(resultPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_infoToggleButton.getInfoPanel(), JLayeredPane.PALETTE_LAYER);  
        layeredPane.add(m_searchToggleButton.getSearchPanel(), Integer.valueOf(JLayeredPane.PALETTE_LAYER+1));
    }
    
    private JPanel createResultPanel(boolean removeStripAndSort) {
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBounds(0, 0, 500, 400);


        JPanel internalPanel = initComponents(removeStripAndSort);
        resultPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        resultPanel.add(toolbar, BorderLayout.WEST);
        
        return resultPanel;
    }
    
    
    private JPanel initComponents(boolean removeStripAndSort) {


        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // create objects
        m_dataScrollPane = new JScrollPane();

        m_dataTable = new DataTable();
        m_dataTable.setModel(new CompoundTableModel(null, true));
        if (removeStripAndSort) {
            m_dataTable.removeStriping();

            m_dataTable.forbidSort(true);
            m_dataTable.setRowSorter(null);
        }
        


        m_markerContainerPanel = new MarkerContainerPanel(m_dataScrollPane, m_dataTable);

        m_dataScrollPane.setViewportView(m_dataTable);
        m_dataTable.setFillsViewportHeight(true);
        m_dataTable.setViewport(m_dataScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);


        return internalPanel;

    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);


        m_settingsButton = new SettingsButton(((ProgressInterface) m_dataTable.getModel()), m_dataTable);
        
        // Search Button
        m_searchToggleButton = new SearchToggleButton(m_dataTable, m_dataTable, ((CompoundTableModel) m_dataTable.getModel()));
        
        m_filterButton = new FilterButton((((CompoundTableModel) m_dataTable.getModel()))) {

            @Override
            protected void filteringDone() {
                m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                m_dataBox.propagateDataChanged();
                m_infoToggleButton.updateInfo();
            }
            
        };  // does not work for the moment, finish it later
        m_exportButton = new ExportButton(((ProgressInterface) m_dataTable.getModel()), "Data", m_dataTable);

        m_infoToggleButton = new InfoToggleButton(m_dataTable, m_dataTable);
        
        
        
        toolbar.add(m_searchToggleButton);
        toolbar.add(m_filterButton);
        toolbar.add(m_settingsButton);
        toolbar.add(m_exportButton);
        toolbar.add(m_infoToggleButton);

        return toolbar;
    }
    
    
    public void setData(GlobalTableModelInterface dataInterface) {

        CompoundTableModel model = new CompoundTableModel(dataInterface, true);
        m_dataTable.setModel(model);
        m_settingsButton.setProgressInterface(model); // model changed
        m_filterButton.setModelFilterInterface(model);
        m_exportButton.setProgressInterface(model);
        m_searchToggleButton.init(m_dataTable, m_dataTable, model);

        m_infoToggleButton.updateInfo();
        
        m_markerContainerPanel.setMaxLineNumber(model.getRowCount());
    }
    
    public void setMaxLineNumber(int nb) {
        m_markerContainerPanel.setMaxLineNumber(nb);
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
    public void addSingleValue(Object v) {
        GlobalTableModelInterface model = getGlobalTableModelInterface();
        if (model != null) {
            getGlobalTableModelInterface().addSingleValue(v);
        }
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return ((CompoundTableModel) m_dataTable.getModel()).getBaseModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_dataTable;
    }

    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_dataTable;
    }

    public Object getValue(Class c, boolean isList) {
        return m_dataTable.getValue(c, isList);
    }
    
    private class DataTable extends LazyTable implements ExportModelInterface, InfoInterface {

        public DataTable() {
            super(m_dataScrollPane.getVerticalScrollBar());
            
            setSortable(true);
        }


        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }
        
        public void dataUpdated() {

        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();

            popupMenu.addAction(new RestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                    m_dataBox.propagateDataChanged();
                }
            });
            popupMenu.addAction(new ClearRestrainAction() {
                @Override
                public void filteringDone() {
                    m_dataBox.addDataChanged(ExtendedTableModelInterface.class);
                    m_dataBox.propagateDataChanged();
                }
            });

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }

        @Override
        public boolean isLoaded() {
            CompoundTableModel model = ((CompoundTableModel) getModel());
            return (model==null) ? false : model.isLoaded();
        }

        @Override
        public int getLoadingPercentage() {
            return ((CompoundTableModel) getModel()).getLoadingPercentage();
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return ((ExportModelInterface) getModel()).getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return ((ExportModelInterface) getModel()).getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public String getExportColumnName(int col) {
            return ((ExportModelInterface) getModel()).getExportColumnName(col);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            if (e.getValueIsAdjusting()) {
                // value is adjusting, so valueChanged will be called again
                return;
            }

            CompoundTableModel compoundTableModel = ((CompoundTableModel) getModel());
            ArrayList<ExtraDataType> extraDataTypeList = compoundTableModel.getExtraDataTypes();
             if (extraDataTypeList != null) {
                 for (ExtraDataType extraDataType : extraDataTypeList) {
                     if (extraDataType.isList()) {
                         m_dataBox.addDataChanged(extraDataType.getTypeClass());
                         m_dataBox.propagateDataChanged();
                     }
                 }
             }
        }
        
         public Object getValue(Class c, boolean isList) {
             CompoundTableModel compoundTableModel = ((CompoundTableModel) getModel());
             if (isList) {
                 
                 int selectedIndex = getSelectionModel().getMinSelectionIndex();

                 if (selectedIndex == -1) {
                     return null;
                 }
        
                int indexInModelSelected = convertRowIndexToModel(selectedIndex);
                indexInModelSelected = compoundTableModel.convertCompoundRowToBaseModelRow(indexInModelSelected);
        
        
                return ((GlobalTableModelInterface) compoundTableModel.getBaseModel()).getRowValue(c, indexInModelSelected);

             } else {
                 return compoundTableModel.getValue(c);
             }
             
         }

        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
            return count+((count>1) ? " Rows" : "Row");
        }
        
    }
    
    

}
