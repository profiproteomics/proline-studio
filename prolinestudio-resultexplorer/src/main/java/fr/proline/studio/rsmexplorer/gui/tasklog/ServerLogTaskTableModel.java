/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.tasklog;

import fr.proline.logviewer.gui.TaskExecutionPanel;
import fr.proline.logviewer.model.LogTask;
import fr.proline.logviewer.model.Utility;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.LongFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.table.AbstractDecoratedGlobalTableModel;
import java.awt.Color;
import java.awt.Component;
import java.util.LinkedHashMap;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author KX257079
 */
public class ServerLogTaskTableModel extends AbstractDecoratedGlobalTableModel<LogTask> {

    static final int COLTYPE_ORDER_ID = 0;
    static final int COLTYPE_MESSAGE_ID = 1;
    static final int COLTYPE_THREAD_NAME = 2;
    static final int COLTYPE_CALL_SERVICE = 3;
    static final int COLTYPE_META_INFO = 4;
    static final int COLTYPE_STATUS = 5;
    static final int COLTYPE_PROJECT_ID = 6;
    static final int COLTYPE_START_TIME = 7;
    static final int COLTYPE_STOP_TIME = 8;
    static final int COLTYPE_DURATION = 9;
    static final int COLTYPE_NB_TASK_PARALELLE = 10;

    private static final String[] m_columnNames = {
        "Task N°",
        "Message Id",
        "Thread Name",
        "Call Service",
        "Meta Info",
        "Status",
        "Project Id",
        "Start Time",
        "Stop Time",
        "Duration",
        "Nb Task Paralelle"};
    private static final String[] m_columnTooltips = m_columnNames;

    public ServerLogTaskTableModel() {
        super();
    }

    @Override
    public int getRowCount() {
        return m_entities.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    public LogTask getTask(int row) {
        return (LogTask) m_entities.get(row);
    }

    /**
     * useful for compare, sort
     *
     * @param columnIndex
     * @return
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_ORDER_ID: {
                return Integer.class;
            }
            case COLTYPE_PROJECT_ID: {
                return Integer.class;
            }
            case COLTYPE_STATUS: {
                return String.class;
            }
            case COLTYPE_START_TIME: {
                return String.class;
            }
            case COLTYPE_STOP_TIME: {
                return String.class;
            }
            case COLTYPE_THREAD_NAME: {
                return String.class;
            }
            case COLTYPE_MESSAGE_ID: {
                return String.class;
            }
            case COLTYPE_CALL_SERVICE: {
                return String.class;
            }
            case COLTYPE_META_INFO: {
                return String.class;
            }
            case COLTYPE_DURATION: {
                return Long.class;
            }
            case COLTYPE_NB_TASK_PARALELLE: {
                return Integer.class;
            }
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return m_columnNames[column]; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= m_entities.size()) {
            return null;
        }
        LogTask taskInfo = m_entities.get(rowIndex);
        switch (columnIndex) {
            case COLTYPE_ORDER_ID: {
                return rowIndex;
            }
            case COLTYPE_PROJECT_ID: {
                return taskInfo.getProjectId();
            }
            case COLTYPE_STATUS: {
                LogTask.STATUS s = taskInfo.getStatus();
                if (s == LogTask.STATUS.FAILED) {
                    return "<html><font color=\"red\">" + s + "</font></html>";
                } else if (s == LogTask.STATUS.WARNING || s == LogTask.STATUS.FINISHED_WARN) {
                    return "<html><font color=\"orange\">" + s + "</font></html>";
                } else {
                    return s.toString();
                }
            }
            case COLTYPE_THREAD_NAME: {
                return taskInfo.getThreadName();
            }
            case COLTYPE_MESSAGE_ID: {
                return taskInfo.getMessageId();
            }
            case COLTYPE_CALL_SERVICE: {
                return taskInfo.getCallService();
            }
            case COLTYPE_META_INFO: {
                return taskInfo.getDataSet();
            }
            case COLTYPE_START_TIME: {
                return Utility.formatTime(taskInfo.getStartTime());
            }
            case COLTYPE_STOP_TIME: {
                return Utility.formatTime(taskInfo.getStopTime());
            }
            case COLTYPE_DURATION: {
                return taskInfo.getDuration();
            }
            case COLTYPE_NB_TASK_PARALELLE: {
                return taskInfo.getNbParallelTask();
            }
            default:
                return null;
        }
    }

    //@tod Attention to non  used col, 
    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        TableCellRenderer renderer = null;
        switch (col) {
            case COLTYPE_NB_TASK_PARALELLE:
                renderer = new TaskNbCellRenderer();
                break;
            case COLTYPE_DURATION:
                renderer = new DurationCellRenderer();
                break;
            default:

        }

        return renderer;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(LogTask.class)) {
            return m_entities.get(row);
        }
        return null;
    }

    @Override
    public int[] getKeysColumn() {
        return null;
    }

    @Override
    public void addFilters(LinkedHashMap filtersMap) {
        filtersMap.put(COLTYPE_ORDER_ID, new IntegerFilter(getColumnName(COLTYPE_ORDER_ID), null, COLTYPE_ORDER_ID));//Integer
        filtersMap.put(COLTYPE_THREAD_NAME, new StringDiffFilter(getColumnName(COLTYPE_THREAD_NAME), null, COLTYPE_THREAD_NAME));//String
        filtersMap.put(COLTYPE_CALL_SERVICE, new StringDiffFilter(getColumnName(COLTYPE_CALL_SERVICE), null, COLTYPE_CALL_SERVICE));//String
        filtersMap.put(COLTYPE_META_INFO, new StringDiffFilter(getColumnName(COLTYPE_META_INFO), null, COLTYPE_META_INFO));//String
        filtersMap.put(COLTYPE_STATUS, new StringDiffFilter(getColumnName(COLTYPE_STATUS), null, COLTYPE_STATUS));//String
        filtersMap.put(COLTYPE_PROJECT_ID, new IntegerFilter(getColumnName(COLTYPE_PROJECT_ID), null, COLTYPE_PROJECT_ID));//Integer

        filtersMap.put(COLTYPE_START_TIME, new StringDiffFilter(getColumnName(COLTYPE_START_TIME), null, COLTYPE_START_TIME));//String
        filtersMap.put(COLTYPE_STOP_TIME, new StringDiffFilter(getColumnName(COLTYPE_STOP_TIME), null, COLTYPE_STOP_TIME));//String

        filtersMap.put(COLTYPE_DURATION, new LongFilter(getColumnName(COLTYPE_DURATION), null, COLTYPE_DURATION));//Long
        filtersMap.put(COLTYPE_NB_TASK_PARALELLE, new IntegerFilter(getColumnName(COLTYPE_NB_TASK_PARALELLE), null, COLTYPE_NB_TASK_PARALELLE));//Integer

    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_columnNames[col];
    }

    public class DurationCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lb = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelIndex = table.convertRowIndexToModel(row);
            LogTask task = ServerLogTaskTableModel.this.getTask(modelIndex);
            //we don't use value in order to avoid exception
            String toShow = Utility.formatDurationInHour(task.getDuration());
            lb.setText(toShow);
            return lb;
        }
    }

    public class TaskNbCellRenderer extends DefaultTableCellRenderer {

        private TaskExecutionPanel m_valuePanel;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (m_valuePanel == null) {
                m_valuePanel = new TaskExecutionPanel(1, -1);
            }

            LogTask task = ServerLogTaskTableModel.this.getTask(row);
            String toShow = ((int) value == 0) ? "" : "+" + value;
            m_valuePanel.setData(task.getTimeStamp(), task.getNbOtherTaskMoment(), toShow);
            if (isSelected) {
                m_valuePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
                m_valuePanel.setForeground(Color.WHITE);
            } else {
                m_valuePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
                m_valuePanel.setForeground(Color.BLACK);
            }
            return m_valuePanel;
        }

    }

}
