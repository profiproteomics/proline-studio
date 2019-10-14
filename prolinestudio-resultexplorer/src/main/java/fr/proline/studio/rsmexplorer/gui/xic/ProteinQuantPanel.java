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

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.actions.table.DisplayTablePopupMenu;
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
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author MB243701
 */
public class ProteinQuantPanel  extends HourglassPanel implements DataBoxPanelInterface, GlobalTabelModelProviderInterface{

    private AbstractDataBox m_dataBox;

    private JScrollPane m_proteinScrollPane;
    private ProteinQuantTable m_proteinQuantTable;
    private MarkerContainerPanel m_markerContainerPanel;
    
    private boolean m_isXICMode;
    private DQuantitationChannel[] m_quantChannels;
    private DMasterQuantProteinSet m_proteinSet;
    
     public ProteinQuantPanel() {
        initComponents();
    }
    
     private void initComponents() {
        setLayout(new BorderLayout());

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        
        final JPanel peptidePanel = createProteinSetPanel();

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


    }
        
    private JPanel createProteinSetPanel() {

        JPanel proteinSetPanel = new JPanel();
        proteinSetPanel.setBounds(0, 0, 500, 400);
        proteinSetPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        proteinSetPanel.add(toolbar, BorderLayout.WEST);
        proteinSetPanel.add(internalPanel, BorderLayout.CENTER);

        return proteinSetPanel;
    }
    
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
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
        m_proteinScrollPane = new JScrollPane();
        
        m_proteinQuantTable = new ProteinQuantTable();
        m_proteinQuantTable.setModel(new CompoundTableModel(new ProteinQuantTableModel((LazyTable)m_proteinQuantTable), true));
        
        
        m_proteinQuantTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_proteinScrollPane, m_proteinQuantTable);
        
        m_proteinScrollPane.setViewportView(m_proteinQuantTable);
        m_proteinQuantTable.setFillsViewportHeight(true);
        m_proteinQuantTable.setViewport(m_proteinScrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_markerContainerPanel, c);
        

        
        return internalPanel;
    }          
    
    public void setData(DQuantitationChannel[] quantChannels,  DMasterQuantProteinSet proteinSet, boolean isXICMode) {
        m_quantChannels = quantChannels;
        m_proteinSet = proteinSet ;
        m_isXICMode = isXICMode;
        ((ProteinQuantTableModel) ((CompoundTableModel) m_proteinQuantTable.getModel()).getBaseModel()).setData( quantChannels, proteinSet, m_isXICMode);
        // select the first row
        if (proteinSet != null && m_quantChannels != null && m_quantChannels.length > 0) {
            m_proteinQuantTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(m_quantChannels.length);
            
        }

        m_proteinQuantTable.setSortable(true);
    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        m_proteinQuantTable.dataUpdated(subTask, finished);
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
        getGlobalTableModelInterface().addSingleValue(v);
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_proteinQuantTable.getModel();
    }
    
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_proteinQuantTable;
    }


    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_proteinQuantTable;
    }
    
    private class ProteinQuantTable extends LazyTable {
        
        private boolean selectionWillBeRestored = false;
        
        public ProteinQuantTable() {
            super(m_proteinScrollPane.getVerticalScrollBar() );

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        
        @Override
        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
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
        
        public void dataUpdated(SubTask subTask, boolean finished) {
            
            LastAction keepLastAction = m_lastAction;
            try {
            
            
            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((ProteinQuantTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
        public TablePopupMenu initPopupMenu() {
            m_popupMenu = new DisplayTablePopupMenu(ProteinQuantPanel.this);

            return m_popupMenu;
        }
        private DisplayTablePopupMenu m_popupMenu;



        @Override
        public void prepostPopupMenu() {
            m_popupMenu.prepostPopupMenu();
        }
        

    }
    
}
