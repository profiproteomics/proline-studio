package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.renderer.PercentageRenderer;
import fr.proline.studio.utils.DecoratedMarkerTable;
import fr.proline.studio.utils.DecoratedTableModel;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.painter.AbstractLayoutPainter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 * Panel used to display all logged tasks
 *
 * @author JM235353
 */
public class TasksPanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    private LogTable m_logTable;

    private boolean m_firstDisplay = true;
    
    public TasksPanel() {
        initComponents();

    }

    private void initComponents() {

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        JScrollPane scrollPane = new JScrollPane();
        m_logTable = new LogTable();
        m_logTable.setModel(new LogTableModel());

        TableColumnModel columnModel = m_logTable.getColumnModel();

        columnModel.getColumn(LogTableModel.COLTYPE_STEP).setCellRenderer(new TaskInfoStepRenderer());

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
        add(scrollPane, c);



    }

    public void updateData() {

        if ((!m_firstDisplay) && (!TaskInfoManager.getTaskInfoManager().isUpdateNeeded())) {
            return;
        }
        m_firstDisplay = false;
        
        
        m_logTable.setUpdating(true);

        LogTableModel tableModel = ((LogTableModel) m_logTable.getModel());

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
            m_dataBox.propagateDataChanged(TaskInfo.class);
        } else if (aSelection) {
            // selection lost
            m_logTable.setUpdating(false); // allow first the update
            m_logTable.getSelectionModel().clearSelection();
            
            // TaskInfo selected can have been modified
            m_dataBox.propagateDataChanged(TaskInfo.class);
        } else {
            m_logTable.setUpdating(false);
        }

        

    }

    public TaskInfo getSelectedTaskInfo() {

        // Retrieve Selected Row
        int selectedRow = m_logTable.getSelectedRow();

        // nothing selected
        if (selectedRow == -1) {
            return null;

        }

        // convert according to the sorting
        selectedRow = m_logTable.convertRowIndexToModel(selectedRow);



        // Retrieve ProteinSet selected
        LogTableModel tableModel = (LogTableModel) m_logTable.getModel();
        return tableModel.getTaskInfo(selectedRow);
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    private class LogTable extends DecoratedMarkerTable {

        private boolean m_isUpdating = false;

        public LogTable() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            displayColumnAsPercentage(LogTableModel.COLTYPE_PERCENTAGE, AbstractLayoutPainter.HorizontalAlignment.LEFT);
            getRelativizer().setMin(0);
            getRelativizer().setMax(100);
            
            setDefaultRenderer(Float.class, new PercentageRenderer( getDefaultRenderer(String.class)) );
        }

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);

            if (!m_isUpdating) {
                m_dataBox.propagateDataChanged(TaskInfo.class);
            }
        }

        public void setUpdating(boolean updating) {
            m_isUpdating = updating;
        }
    }

    private static class LogTableModel extends DecoratedTableModel {

        public static final int COLTYPE_STEP = 0;
        public static final int COLTYPE_TASKINFO_ID = 1;
        public static final int COLTYPE_TASKINFO_CATEGORY = 2;
        public static final int COLTYPE_DESCRIPTION = 3;
        public static final int COLTYPE_PERCENTAGE = 4;
        private static final String[] columnNames = {"", "id", "Category", "Task Description", "Progress"};
        private ArrayList<TaskInfo> m_taskInfoList = null;

        public boolean updateData() {
            if (m_taskInfoList == null) {
                m_taskInfoList = new ArrayList<>(128);
            }
            boolean updateDone = TaskInfoManager.getTaskInfoManager().copyData(m_taskInfoList);

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
                case COLTYPE_STEP: {
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
    }

    public class TaskInfoStepRenderer extends DefaultTableCellRenderer {

        public TaskInfoStepRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            TaskInfo task = (TaskInfo) value;

            if (task.isWaiting()) {
                setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16));
            } else if (task.isRunning()) {
                setIcon(IconManager.getIcon(IconManager.IconType.ARROW_RIGHT_SMALL));
            } else if (task.isFinished()) {
                if (task.isSuccess()) {
                    setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
                } else {
                    setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
                }
            } else if (task.isAborted()) {
                setIcon(IconManager.getIcon(IconManager.IconType.CROSS_BLUE_SMALL16));
            }

            return this;
        }
    }

    public class DefaultErrorRenderer implements TableCellRenderer, Serializable {

        private TableCellRenderer m_renderer;
        private JXTable m_table;
        
        public DefaultErrorRenderer(TableCellRenderer renderer, JXTable table) {
            m_renderer = renderer;
            m_table = table;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = m_renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {

                JLabel l = ((JLabel) c);

                l.setHorizontalAlignment(JLabel.LEFT);

                
                row = m_table.convertRowIndexToModel(row);
                TaskInfo taskInfo = ((LogTableModel) table.getModel()).getTaskInfo(row);
                if (taskInfo.hasTaskError()) {
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
    
    
}
