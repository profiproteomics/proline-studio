/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.tasklog;

import fr.proline.logviewer.gui.TaskListInterface;
import fr.proline.logviewer.model.LogTask;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelInterface;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.info.InfoInterface;
import fr.proline.studio.table.DecoratedMarkerTable;
import fr.proline.studio.table.TablePopupMenu;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.table.TableColumnExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author KX257079
 */
public class ServerLogTaskListView extends JScrollPane implements TaskListInterface {

    protected static final Logger m_logger = LoggerFactory.getLogger(ServerLogTaskListView.class);
    private ServerLogControlPanel m_ctrl;
    private ArrayList<LogTask> m_taskList;//data model used by TableModel
    private ServerLogTaskTable m_table;
    private ServerLogTaskTableModel m_tableModel;
    private CompoundTableModel m_CompoundTableModel;

    ServerLogTaskListView(ServerLogControlPanel control) {

        super();
        m_ctrl = control;
        this.setBorder(BorderFactory.createTitledBorder("List of Task"));
        this.setPreferredSize(new Dimension(1400, 250));
        m_taskList = new ArrayList();
        m_tableModel = new ServerLogTaskTableModel();
        m_CompoundTableModel = new CompoundTableModel(m_tableModel, true);//true = Filter
        m_table = new ServerLogTaskTable();
        m_table.setModel(m_CompoundTableModel);
        m_table.init();
        this.setViewportView(m_table);
        m_table.setFillsViewportHeight(true);
    }

    public CompoundTableModel getCompoundTableModel() {
        return m_CompoundTableModel;
    }

    public ServerLogTaskTable getTable() {
        return m_table;
    }

    @Override
    public void setData(ArrayList<LogTask> tasks, String fileName) {
        if (tasks != null) {
            m_logger.debug("setData {} tasks for {}", tasks.size(), fileName);
        } else {
            m_logger.debug("setData null {} tasks for {}", tasks, fileName);
        }
        ((TitledBorder) this.getBorder()).setTitle("List of Tasks" + "     " + fileName);
        //todo reuse m_taskTable.removeAll();//must
        if (tasks == null || tasks.isEmpty()) {
            m_taskList = new ArrayList<>();
        } else {
            m_taskList = tasks;
            m_tableModel.setData(m_taskList);
            m_table.getSelectionModel().setSelectionInterval(0, 0);
        }
        initColumnsize();
    }

    /**
     * @todo verify if useful
     */
    private void initColumnsize() {
        String[] example = {"198", "853bda4a-10d9-11e8-9a85-d9411af38406", "[pool-2-thread-25]", "proline/dps/msi/ImportValidateGenerateSM", " result_files :  mascot_data/20200113/F136424.dat ",
            "FINISHED_W", "104", "09:01:27.985 - 09 oct. 2019 ", "09:01:27.985 - 09 oct. 2019 ", "111:59:59.999", "+10                     "};
        TableColumn column;
        int cellWidth;
        for (int i = 0; i < m_table.getColumnCount(); i++) {
            column = m_table.getColumnModel().getColumn(i);
            int modelIndex = m_table.convertColumnIndexToModel(i);
            TableCellRenderer renderer = m_table.getDefaultRenderer(column.getClass());
            Component comp = renderer.getTableCellRendererComponent(m_table, example[modelIndex], false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;
            column.setPreferredWidth(cellWidth);
        }
    }

    class ServerLogTaskTable extends DecoratedMarkerTable implements ExportModelInterface, InfoInterface {

        public ServerLogTaskTable() {
            super();
            //setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        public void init() {
            this.setColumnsVisibility();
        }

        private void setColumnsVisibility() {
            TableColumnExt columnExt = m_table.getColumnExt(m_table.convertColumnIndexToView(ServerLogTaskTableModel.COLTYPE_MESSAGE_ID));
            if (columnExt != null) {
                columnExt.setVisible(false);
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (!lsm.isSelectionEmpty()) {
                int sortedIndex = lsm.getMinSelectionIndex();//only the first one
                int selectedIndex = m_table.convertRowIndexToModel(sortedIndex);
                LogTask task = m_taskList.get(selectedIndex);
                String taskOrder = (task == null) ? "" : "" + task.getTaskOrder();
                m_ctrl.valueChanged(task);
            }
        }

        @Override
        public String getInfo() {
            int count = getModel().getRowCount();
            return count + ((count > 1) ? " Tasks" : " Task");
        }

        @Override
        public String getExportColumnName(int col) {
            return m_tableModel.getExportColumnName(convertColumnIndexToModel(col));
        }

        @Override
        public String getExportRowCell(int row, int col) {
            return m_tableModel.getExportRowCell(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public ArrayList<ExportFontData> getExportFonts(int row, int col) {
            return m_tableModel.getExportFonts(convertRowIndexToModel(row), convertColumnIndexToModel(col));
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            TablePopupMenu popupMenu = new TablePopupMenu();
            return popupMenu;
        }

        @Override
        public void prepostPopupMenu() {
            ;
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            this.getModel().addTableModelListener(l);
        }

    }

}
