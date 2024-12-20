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
package fr.proline.studio.rsmexplorer.gui.tasklog;

import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.CancelTask;
import fr.proline.studio.dpm.task.jms.PurgeConsumer;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.*;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.PercentageRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.*;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Panel used to display all logged tasks
 *
 * @author JM235353
 */
public class TasksPanel extends AbstractTasksPanel {

    private LogTable m_logTable;

    private boolean m_firstDisplay = true;
         
    public TasksPanel() {
        super();

    }
    
    
    @Override
    protected AbstractJMSCallback getServiceNotificationCallback(JMSNotificationMessage[] sysInfoResult) {
        AbstractJMSCallback notifierCallback = new AbstractJMSCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (sysInfoResult[0].getEventType().equals(JMSNotificationMessage.MessageStatus.STARTED)) {
                    TaskInfo tiToUpdate = TaskInfoManager.getTaskInfoManager().getTaskInfoWithJMSId(sysInfoResult[0].getServerUniqueMsgId());
                    if (tiToUpdate != null) {
                        tiToUpdate.setRunning(true);
                    }
                }
            }
        };
        return notifierCallback;
    }
    
    //Nothing specific to do after cancel message was done : already taken into account be message response
    @Override
    protected AbstractJMSCallback getPurgeConsumerCallback(JMSNotificationMessage[] purgerResult){
        return null;
    }
    
    // default start data collect is enough
    @Override
    protected void startOtherDataCollecting(){
    }
    
    // default stop data collect is enough
    @Override
    protected void stopOtherDataCollecting(){
    }


    @Override
    protected JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        JScrollPane scrollPane = new JScrollPane();
        m_logTable = new LogTable();
        m_logTable.setModel(new CompoundTableModel(new LogTableModel(), true));
        
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m_logTable.getModel());
        m_logTable.setRowSorter(sorter);
            
        sorter.setComparator(LogTableModel.COLTYPE_TASKINFO_CRITICALITY, new Comparator<TaskInfo>() {

            @Override
            public int compare(TaskInfo o1, TaskInfo o2) {
                int cmp = o2.getImportance() - o1.getImportance();
                if (cmp != 0) {
                    return cmp; 
                }
                return o2.getId()-o1.getId();
            }
 
                
        });

        TableColumnModel columnModel = m_logTable.getColumnModel();

        columnModel.getColumn(LogTableModel.COLTYPE_STEP).setCellRenderer(new TaskInfoStepRenderer());
        columnModel.getColumn(LogTableModel.COLTYPE_TASKINFO_CRITICALITY).setCellRenderer(new TaskInfoImportanceRenderer());

        DefaultErrorRenderer defaultErrorRenderer = new DefaultErrorRenderer(new DefaultTableRenderer(), m_logTable);
        columnModel.getColumn(LogTableModel.COLTYPE_TASKINFO_ID).setCellRenderer(defaultErrorRenderer);
        columnModel.getColumn(LogTableModel.COLTYPE_TASKINFO_CATEGORY).setCellRenderer(defaultErrorRenderer);
        columnModel.getColumn(LogTableModel.COLTYPE_DESCRIPTION).setCellRenderer(defaultErrorRenderer);


        // set preferred width of different columns
        columnModel.getColumn(LogTableModel.COLTYPE_STEP).setMaxWidth(20);
        columnModel.getColumn(LogTableModel.COLTYPE_TASKINFO_ID).setMaxWidth(40);
        columnModel.getColumn(LogTableModel.COLTYPE_TASKINFO_CATEGORY).setMaxWidth(180);

        scrollPane.setViewportView(m_logTable);
        m_logTable.setFillsViewportHeight(true);
        m_logTable.setViewport(scrollPane.getViewport());



        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(scrollPane, c);

        return internalPanel;

    }

    @Override
    protected JToolBar initToolbar() {
       return  super.initToolbar();


    }

    @Override
    protected  FilterTableModelInterface getTaskTableModel(){
       return  ((CompoundTableModel) m_logTable.getModel());
    }

    @Override
    protected void clearActionPerformed() {
        TaskInfoManager.getTaskInfoManager().clear();
        m_firstDisplay = true;
        updateData();
    }


    public void updateData() {

        if ((!m_firstDisplay) && (!TaskInfoManager.getTaskInfoManager().isUpdateNeeded())) {
            return;
        }
        m_firstDisplay = false;
        
        
        m_logTable.setUpdating(true);

        LogTableModel tableModel = (LogTableModel) ((CompoundTableModel) m_logTable.getModel()).getBaseModel();

        int selectedTaskInfoId = -1;
        int selectedIndex = m_logTable.getSelectionModel().getMinSelectionIndex();
        boolean aSelection = (selectedIndex != -1);
        if (aSelection) {
            selectedIndex = m_logTable.convertRowIndexToModel(selectedIndex);
            selectedTaskInfoId = tableModel.getTaskInfo(selectedIndex).getId();
        }

        tableModel.updateData();

        selectedIndex = (selectedTaskInfoId == -1) ? -1 : tableModel.findIndexOfTaskInfoById(selectedTaskInfoId);

        if (selectedIndex != -1) {
            // selection unchanged
            selectedIndex = m_logTable.convertRowIndexToView(selectedIndex);
            m_logTable.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
            m_logTable.setUpdating(false); // at the end reallow the update
            
            // TaskInfo selected can have been modified
            getDataBox().addDataChanged(TaskInfo.class);
            getDataBox().propagateDataChanged();
        } else if (aSelection) {
            // selection lost
            m_logTable.setUpdating(false); // allow first the update
            m_logTable.getSelectionModel().clearSelection();
            
            // TaskInfo selected can have been modified
            getDataBox().addDataChanged(TaskInfo.class);
            getDataBox().propagateDataChanged();
        } else {
            m_logTable.setUpdating(false);
        }

    }

    public TaskInfo getSelectedTaskInfo() {

        // Retrieve Selected Row
        int selectedRow = m_logTable.getSelectedRow();
        return m_logTable.getTaskInfo(selectedRow);

    }
    

    protected class LogTable extends DecoratedMarkerTable {

        private boolean m_isUpdating = false;

        public LogTable() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            //displayColumnAsPercentage(LogTableModel.COLTYPE_PERCENTAGE, AbstractLayoutPainter.HorizontalAlignment.LEFT);
            //getRelativizer().setMin(0);
            //getRelativizer().setMax(100);
            
            setDefaultRenderer(Float.class, new PercentageRenderer( getDefaultRenderer(String.class)) );            
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
            
            if (!m_isUpdating) {
                getDataBox().addDataChanged(TaskInfo.class);
                getDataBox().propagateDataChanged();
            }
        }

        public void setUpdating(boolean updating) {
            m_isUpdating = updating;
        }
        
                @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu(false);
            popupMenu.addAction(new StopTaskAction());

            return popupMenu;
        }

        // set as abstract
        @Override
        public void prepostPopupMenu() {
            // nothing to do
        }
        
        private TaskInfo getTaskInfo(int row) {
            if (row == -1) {
                return null;
            }

            // convert according to the sorting
            row = convertRowIndexToModel(row);

            CompoundTableModel compoundTableModel = ((CompoundTableModel) getModel());
            row = compoundTableModel.convertCompoundRowToBaseModelRow(row);

            // Retrieve ProteinSet selected
            LogTableModel tableModel = (LogTableModel) compoundTableModel.getBaseModel();
            return tableModel.getTaskInfo(row);
    }

    }

    private static class LogTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

        public static final int COLTYPE_STEP = 0;
        public static final int COLTYPE_TASKINFO_ID = 1;
        public static final int COLTYPE_TASKINFO_CATEGORY = 2;
        public static final int COLTYPE_TASKINFO_CRITICALITY = 3;
        public static final int COLTYPE_DESCRIPTION = 4;
        public static final int COLTYPE_PERCENTAGE = 5;
        private static final String[] columnNames = {"", "id", "Category", "Criticality", "Task Description", "Progress"};
        private ArrayList<TaskInfo> m_taskInfoList = null;

        public boolean updateData() {
            if (m_taskInfoList == null) {
                m_taskInfoList = new ArrayList<>(128);
            }
            boolean updateDone = TaskInfoManager.getTaskInfoManager().copyData(m_taskInfoList, false);

            if (updateDone) {
                fireTableDataChanged();
            }
            return updateDone;
        }

        public TaskInfo getTaskInfo(int index) {
            if ((m_taskInfoList == null) || (index >= m_taskInfoList.size()) || (index < 0)) {
                return null;
            }

            return m_taskInfoList.get(index);

        }

        public int findIndexOfTaskInfoById(int taskInfoId) {
            if (taskInfoId == -1) {
                return -1;
            }

            for (int i = 0; i < m_taskInfoList.size(); i++) {
                if (m_taskInfoList.get(i).getId() == taskInfoId) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
                case COLTYPE_STEP:
                case COLTYPE_TASKINFO_CRITICALITY:
                    return TaskInfo.class;
                case COLTYPE_TASKINFO_ID:
                    return Integer.class;
                case COLTYPE_TASKINFO_CATEGORY:
                case COLTYPE_DESCRIPTION:
                    return String.class;
                case COLTYPE_PERCENTAGE:
                    return Float.class;
            }
            return null; // should not happen
        }

        @Override
        public int getRowCount() {
            if (m_taskInfoList == null) {
                return 0;
            }

            return m_taskInfoList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            TaskInfo taskInfo = m_taskInfoList.get(rowIndex);
            switch (columnIndex) {
                case COLTYPE_STEP:
                case COLTYPE_TASKINFO_CRITICALITY: {
                    return taskInfo;
                }
                case COLTYPE_TASKINFO_ID: {
                    return taskInfo.getId();
                }
                case COLTYPE_TASKINFO_CATEGORY: {
                    return taskInfo.getIdList();
                }
                case COLTYPE_DESCRIPTION: {
                    return taskInfo.getTaskDescription();
                }
                case COLTYPE_PERCENTAGE: {
                    if (taskInfo.isRunning()) {
                        float percentage = taskInfo.getPercentage();
                        if ((percentage<0.001) || (percentage>99.999)) {
                            return Float.NaN;
                        }
                       return percentage;
                    } else {
                        return Float.NaN;
                    }
                    
                }

            }
            return null; // should not happen
        }

        @Override
        public String getToolTipForHeader(int col) {
            return null; // no tooltip
        }
        
        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

        @Override
        public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

            ConvertValueInterface stepConverter = new ConvertValueInterface() {
                @Override
                public Object convertValue(Object o) {
                    if (o == null) {
                        return null;
                    }
                    return ((TaskInfo) o).getPublicState();
                }

            };
            filtersMap.put(COLTYPE_STEP, new ValueFilter("State", TaskInfo.PUBLIC_STATE_VALUES, PUBLIC_STATE_ICONS, ValueFilter.ValueFilterType.EQUAL, stepConverter, COLTYPE_STEP));
            filtersMap.put(COLTYPE_TASKINFO_ID, new IntegerFilter(getColumnName(COLTYPE_TASKINFO_ID), null, COLTYPE_TASKINFO_ID));
            
            
            ConvertValueInterface criticalityConverter = new ConvertValueInterface() {
                @Override
                public Object convertValue(Object o) {
                    if (o == null) {
                        return null;
                    }
                    return Integer.valueOf(((TaskInfo) o).getImportance());
                }

            };
            filtersMap.put(COLTYPE_TASKINFO_CRITICALITY, new ValueFilter(getColumnName(COLTYPE_TASKINFO_CRITICALITY), TaskInfo.IMPORTANCE_VALUES, null, ValueFilter.ValueFilterType.GREATER_EQUAL, criticalityConverter, COLTYPE_TASKINFO_CRITICALITY));
            filtersMap.put(COLTYPE_DESCRIPTION, new StringDiffFilter(getColumnName(COLTYPE_DESCRIPTION), null, COLTYPE_DESCRIPTION));

        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public int getLoadingPercentage() {
            return 100;
        }

        @Override
        public Long getTaskId() {
            return -1L; // not used
        }

        @Override
        public LazyData getLazyData(int row, int col) {
            return null; // not used
        }

        @Override
        public void givePriorityTo(Long taskId, int row, int col) {
            return; // not used
        }

        @Override
        public void sortingChanged(int col) {
            return; // not used
        }

        @Override
        public int getSubTaskId(int col) {
           return -1; // not used
        }

        @Override
        public String getDataColumnIdentifier(int columnIndex) {
            return null; // not used
        }

        @Override
        public Class getDataColumnClass(int columnIndex) {
            return null; // not used
        }

        @Override
        public Object getDataValueAt(int rowIndex, int columnIndex) {
            return null; // not used
        }

        @Override
        public int[] getKeysColumn() {
            return null; // not used
        }

        @Override
        public int getInfoColumn() {
            return -1; // not used
        }

        @Override
        public void setName(String name) {
            // not used
        }

        @Override
        public String getName() {
            return null; // not used
        }

        @Override
        public Map<String, Object> getExternalData() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public PlotInformation getPlotInformation() {
            return null; // not used
        }


        @Override
        public PlotType getBestPlotType() {
            return null; // not used
        }

        @Override
        public int[] getBestColIndex(PlotType plotType) {
            return null; // not used
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
        public String getExportColumnName(int col) {
            return getColumnName(col);
        }

        @Override
        public TableCellRenderer getRenderer(int row, int col) {
            if (col == COLTYPE_PERCENTAGE) {
                return new ScoreRenderer();
            }
            return null;
        }

        @Override
        public GlobalTableModelInterface getFrozzenModel() {
            return this;
        }

        @Override
        public ArrayList<ExtraDataType> getExtraDataTypes() {
            ArrayList<ExtraDataType> list = new ArrayList<>();
            list.add(new ExtraDataType(TaskInfo.class, true));
            registerSingleValuesAsExtraTypes(list);
            return list;
        }

        @Override
        public Object getValue(Class c) {
            return getSingleValue(c);
        }

        @Override
        public Object getRowValue(Class c, int row) {
            if (c.equals(TaskInfo.class)) {
                return m_taskInfoList.get(row);
            }
            return null;
        }
        
        @Override
        public Object getColValue(Class c, int col) {
            return null;
        }

    }

    public static class TaskInfoStepRenderer extends DefaultTableCellRenderer {

        public TaskInfoStepRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            TaskInfo task = (TaskInfo) value;

            setIcon(PUBLIC_STATE_ICONS[task.getPublicState()]);

            return this;
        }
    }
    
    public static class TaskInfoImportanceRenderer extends DefaultTableCellRenderer {

        public TaskInfoImportanceRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            TaskInfo task = (TaskInfo) value;

            setText(task.getImportanceAsString());

            return this;
        }
    }

    public class DefaultErrorRenderer implements TableCellRenderer, Serializable {

        private final TableCellRenderer m_renderer;
        private final LogTable m_table;

        public DefaultErrorRenderer(TableCellRenderer renderer, LogTable table) {
            m_renderer = renderer;
            m_table = table;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = m_renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {

                JLabel l = ((JLabel) c);

                l.setHorizontalAlignment(JLabel.LEFT);

                TaskInfo taskInfo = m_table.getTaskInfo(row);
                if (taskInfo!=null && taskInfo.hasTaskError()) {
                    l.setForeground(Color.red);
                } else {
                    if (isSelected) {
                        l.setForeground(Color.white);
                    } else {
                        l.setForeground(Color.black);
                    }
                }

            }

            return c;
        }
    }
    
    
    private class StopTaskAction extends AbstractTableAction {

        public StopTaskAction() {
            super("Cancel task...");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {           
            int nbSelectedRset = selectedRows.length;
            if(nbSelectedRset != 1 ){
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),"Only one task can be aborted at the same time","Cancel Task Error",JOptionPane.ERROR_MESSAGE);
                return;
            }                
            TaskInfo currentTaskInfo = getSelectedTaskInfo();
            if(fr.profi.util.StringUtils.isEmpty(currentTaskInfo.getJmsMessageID())){
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),"Only server side tasks can be aborted","Cancel Task Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if( currentTaskInfo.getPublicState() == TaskInfo.PUBLIC_STATE_RUNNING){
                CancelTask task = new CancelTask(currentTaskInfo.getJmsMessageID());
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
            } else if ( currentTaskInfo.getPublicState() == TaskInfo.PUBLIC_STATE_WAITING){          
                PurgeConsumer.getPurgeConsumer().clearMessage(currentTaskInfo.getJmsMessageID());
            } else {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),"Only pending or running task can be aborted","Cancel Task Error",JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
                               
            boolean enable = true;
            if(selectedRows.length != 1 )
                enable = false;
            else {
                TaskInfo currentTaskInfo = getSelectedTaskInfo();
                int currentPublicState = currentTaskInfo.getPublicState();
                if( currentPublicState != TaskInfo.PUBLIC_STATE_WAITING &&currentPublicState !=TaskInfo.PUBLIC_STATE_RUNNING ){
                    enable = false;
                }
            }
            
            setEnabled(enable);            
        }
    }
}
   
   
