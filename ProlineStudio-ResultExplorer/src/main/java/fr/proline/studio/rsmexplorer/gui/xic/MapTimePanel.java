/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.markerbar.MarkerContainerPanel;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * map alignement data
 * @author MB243701
 */
public class MapTimePanel extends HourglassPanel implements DataBoxPanelInterface , GlobalTabelModelProviderInterface {
    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private AbstractDataBox m_dataBox;
    
    private JScrollPane m_timeScrollPane;
    private MapAlignmentTable m_timeTable;
    private MarkerContainerPanel m_markerContainerPanel;
    
    
    public MapTimePanel() {
        super();
        initComponents();
    }
    
    private void initComponents(){
        setLayout(new BorderLayout());
        JPanel expDesignPanel = createMapAlignmentPanel();
        this.add(expDesignPanel, BorderLayout.CENTER);
    }
    
    private JPanel createMapAlignmentPanel(){
        JPanel mapAlignmentPanel = new JPanel();
        mapAlignmentPanel.setBounds(0, 0, 500, 400);
        mapAlignmentPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();

        JToolBar toolbar = initToolbar();
        mapAlignmentPanel.add(toolbar, BorderLayout.WEST);
        mapAlignmentPanel.add(internalPanel, BorderLayout.CENTER);
        return mapAlignmentPanel;
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        return toolbar;
    }
    
    private JPanel createInternalPanel(){
        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        // create objects
        m_timeScrollPane = new JScrollPane();
        
        m_timeTable = new MapAlignmentTable();
        m_timeTable.setModel(new CompoundTableModel(new MapTimeTableModel((LazyTable)m_timeTable), true));
        
        
        m_timeTable.setSortable(false);

        m_markerContainerPanel = new MarkerContainerPanel(m_timeScrollPane, m_timeTable);
        
        m_timeScrollPane.setViewportView(m_timeTable);
        m_timeTable.setFillsViewportHeight(true);
        m_timeTable.setViewport(m_timeScrollPane.getViewport());

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
    
    public void setData(Long taskId,  MapAlignment mapAlignment,  List<MapTime> mapTimes, Color color, String title, boolean finished, String fromMapName) {
        ((MapTimeTableModel)((CompoundTableModel) m_timeTable.getModel()).getBaseModel()).setData(taskId, mapAlignment, mapTimes, color, title,fromMapName );

        // select the first row
        if ((mapTimes != null) && (mapTimes.size() > 0)) {
            m_timeTable.getSelectionModel().setSelectionInterval(0, 0);
            m_markerContainerPanel.setMaxLineNumber(mapTimes.size());
        }
        if (finished) {
            m_timeTable.setSortable(true);
        }

    }
    
    public void dataUpdated(SubTask subTask, boolean finished) {
        updateData();
    }
    
    private void updateData(){
        
    }
    
    @Override
    public CrossSelectionInterface getCrossSelectionInterface() {
        return m_timeTable;
    }
    
    @Override
    public void addSingleValue(Object v) {
        getGlobalTableModelInterface().addSingleValue(v);
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return (GlobalTableModelInterface) m_timeTable.getModel();
    }
    @Override
    public JXTable getGlobalAssociatedTable() {
        return m_timeTable;
    }
    
    

private class MapAlignmentTable extends LazyTable  {

        public MapAlignmentTable() {
            super(m_timeScrollPane.getVerticalScrollBar());
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
            if (m_dataBox != null) {
                m_dataBox.propagateDataChanged(MapAlignment.class);
            }

        }

        public void dataUpdated(SubTask subTask, boolean finished) {
            
            LazyTable.LastAction keepLastAction = m_lastAction;
            try {
            // retrieve selected row
            int rowSelected = getSelectionModel().getMinSelectionIndex();
            int rowSelectedInModel = (rowSelected == -1) ? -1 : convertRowIndexToModel(rowSelected);

            // Update Model (but protein set table must not react to the model update)
            
            selectionWillBeRestored(true);
            try {
                ((MapTimeTableModel) (((CompoundTableModel) getModel()).getBaseModel())).dataUpdated();
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
                if (((keepLastAction == LazyTable.LastAction.ACTION_SELECTING ) || (keepLastAction == LazyTable.LastAction.ACTION_SORTING)) && (subTask.getSubTaskId() == ((CompoundTableModel) getModel()).getSubTaskId( getSortedColumnIndex() )) ) {
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
        
        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }



        
    }
}

