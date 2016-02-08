package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterButton;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.filter.ValueFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.renderer.PercentageRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.ScoreRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
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

    private final static ImageIcon[] PUBLIC_STATE_ICONS = { IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16), IconManager.getIcon(IconManager.IconType.ARROW_RIGHT_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_BLUE_SMALL16), IconManager.getIcon(IconManager.IconType.TICK_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_SMALL16)};
   
    
    private AbstractDataBox m_dataBox;
    private LogTable m_logTable;

    private boolean m_firstDisplay = true;
    
    public TasksPanel() {
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.weightx = 1;
        c.weighty = 1;
        add(createToolbarPanel(), c);


    }

    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel();

        toolbarPanel.setLayout(new BorderLayout());

        JPanel internalPanel = createInternalPanel();
        toolbarPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        toolbarPanel.add(toolbar, BorderLayout.WEST);

        return toolbarPanel;

    }

    private JPanel createInternalPanel() {

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
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        FilterButton filterButton = new FilterButton(((CompoundTableModel) m_logTable.getModel())) {

            @Override
            protected void filteringDone() {
            }
            
        };
        EraserButton taskEraserButton = new EraserButton();
        
        toolbar.add(filterButton);
        toolbar.add(taskEraserButton);
        
        return toolbar;
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
        return m_logTable.getTaskInfo(selectedRow);

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
        // should not be used
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

    private class LogTable extends DecoratedMarkerTable {

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

            if (!m_isUpdating) {
                m_dataBox.propagateDataChanged(TaskInfo.class);
            }
        }

        public void setUpdating(boolean updating) {
            m_isUpdating = updating;
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
                case COLTYPE_STEP: {
                    return taskInfo;
                }
                case COLTYPE_TASKINFO_ID: {
                    return taskInfo.getId();
                }
                case COLTYPE_TASKINFO_CATEGORY: {
                    return taskInfo.getIdList();
                }
                case COLTYPE_TASKINFO_CRITICALITY: {
                    return taskInfo;
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
                    return Integer.valueOf(((TaskInfo) o).getPublicState());
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
            filtersMap.put(COLTYPE_DESCRIPTION, new StringFilter(getColumnName(COLTYPE_DESCRIPTION), null, COLTYPE_DESCRIPTION));

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
            return -1l; // not used
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
        public int getBestXAxisColIndex(PlotType plotType) {
            return -1; // not used
        }

        @Override
        public int getBestYAxisColIndex(PlotType plotType) {
            return -1; // not used
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return null; // no specific export
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

    public class TaskInfoStepRenderer extends DefaultTableCellRenderer {

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
    
    public class TaskInfoImportanceRenderer extends DefaultTableCellRenderer {

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
    
    public class EraserButton extends JButton implements ActionListener {

        public EraserButton() {

            setIcon(IconManager.getIcon(IconManager.IconType.ERASER));
            setToolTipText("Erase All Finished Tasks");

            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TaskInfoManager.getTaskInfoManager().clear();
            m_firstDisplay = true;
            updateData();
        }
    }




}
