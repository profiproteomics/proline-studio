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
package fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation;

import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.xic.XICBiologicalSampleAnalysisNode;
import fr.proline.studio.utils.IconManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTreeTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class QCMappingTreeTable extends JXTreeTable {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.AggregationQuant");
    private static final int UP = -1;
    private static final int DOWN = 1;
    public static final String ERRASE = "Remove";
    public static final String MOVE_UP = "Move Up";
    public static final String MOVE_DOWN = "Move Down";
    public static final String INSERT_UP = "Insert Up";
    public static final String INSERT_DOWN = "Insert Down";
    private QCMappingTreeTableModel m_model;

    public QCMappingTreeTable(QCMappingTreeTableModel treeModel) {
        super(treeModel);
        m_model = treeModel;
        setCellSelectionEnabled(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        super.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.addMouseListener(new PopupAdapter());
    }

    
    @Override
    public String getToolTipText(MouseEvent event) {
        int column = columnAtPoint(event.getPoint());
        if (column > 0 && column < getColumnCount()) {
            int row = rowAtPoint(event.getPoint());
            if (row < getRowCount()) {
                return m_model.getToolTipText(getNodeForRow(row), column);
            }
        }
        return super.getToolTipText(event);
    }

    public Object getNodeForRow(int row) {
        TreePath path = getPathForRow(row);
        return path != null ? path.getLastPathComponent() : null;
    }

    public void removeAssociateChannel() {
        int[] rowList = getSelectedRows();
        int[] columnList = getSelectedColumns();
        for (int row : rowList) {
            for (int column : columnList) {
                m_model.remove(row, column);
            }
        }
        this.repaint();
    }

    private boolean isSelectionOk() {
        if (getSelectedRows().length == 0) {
            return false;
        }
        //don't treat ignored channel
        int[] selectRows = getSelectedRows();
        int[] selectCols = getSelectedColumns();

        for (int i = 0; i < selectRows.length; i++) {
            for (int j = 0; j < selectCols.length; j++) {
                int row = convertRowIndexToModel(selectRows[i]);
                int col = convertColumnIndexToModel(selectCols[j]);
                if (m_model.hasEmptyChannel(row, col)) {
                    return false;
                }
            }
        }
        //don't treat first column
        int[] columnList = getSelectedColumns();
        for (int nb = 0; nb < columnList.length; nb++) {
            if (columnList[nb] == 0) {
                return false;
            }
        }
        return true;
    }

    protected synchronized void moveUpDown(int weight, boolean isInsertMode) {
        if (!isSelectionOk()) {
            return;
        }
        ArrayList<Integer> selectedModelCols = new ArrayList();
        int[] columnList = getSelectedColumns();
        for (int col : columnList) {
            selectedModelCols.add(convertColumnIndexToModel(col));
        }
        int[] rows = getSelectedRows();

        ArrayList<Integer> selectedModelRows = new ArrayList();
        for (int row : rows) {
            selectedModelRows.add(convertRowIndexToModel(row));
        }
        ArrayList<Integer> newSelectedModelRows = new ArrayList();
        //get selected row, range it in order ascending(for up) ou descending(for down)
        if (weight == UP) {
            Collections.sort(selectedModelRows);//lower element move first
        } else {
            Collections.sort(selectedModelRows, Collections.reverseOrder());//higher element move first
        }
        if (isEndChannel(selectedModelRows, weight)) {
            return;
        }
        //move up/down for each cell
        //m_model.setSelected(selectedModelRows, selectedModelCols);
        if (isInsertMode) {//set m_holdChannelMapping        
            //the row next to first row will be recoverd by firstRow, and the last row will be empty. 
            //We create a row which has the last row index, and the first DQuantitationChannelMapping
            DQuantitationChannelMapping targetMapping = getNextRow(selectedModelRows.get(0), weight);
            int parentQcNumber = m_model.getRowMapping(getNodeForRow(selectedModelRows.get(selectedModelRows.size() - 1))).getParentQCNumber();
            m_holdChannelMapping = new DQuantitationChannelMapping(parentQcNumber);
            for (int column : columnList) {
                if (column == 0) {
                    continue;
                }
                DDataset quanti = m_model.getDatasetAt(column);
                QuantitationChannel srcChannel = targetMapping.getQuantChannel(quanti);
                m_holdChannelMapping.put(quanti, srcChannel);
            }

        }
        int targetRow;
        for (int row : selectedModelRows) {
            for (int column : selectedModelCols) {
                targetRow = this.setNextRow(row, column, weight);
                if (targetRow != -1) {
                    newSelectedModelRows.add(targetRow);
                }
            }
        }
        if (isInsertMode) {//put m_holdChannelMapping at the last place of moved row(empty now)
            Object targetNode = getNodeForRow(selectedModelRows.get(selectedModelRows.size() - 1));
            if (targetNode instanceof XICBiologicalSampleAnalysisNode) {
                for (int column : columnList) {
                    if (column == 0) {
                        continue;
                    }
                    DQuantitationChannelMapping mapping = m_model.getRowMapping(targetNode);
                    DDataset quanti = m_model.getDatasetAt(column);
                    mapping.put(quanti, m_holdChannelMapping.getQuantChannel(quanti));
                }
            }
        }
        //set the new selected rows columns
        if (newSelectedModelRows.size() > 0) {
            Collections.sort(newSelectedModelRows);
            for (int i = 0; i < newSelectedModelRows.size(); i++) {
                int nRow = newSelectedModelRows.get(i);
                if (nRow < this.getRowCount()) {
                    int index = convertRowIndexToView(nRow);
                    if (i <= 0) {
                        setRowSelectionInterval(index, index);
                    } else {
                        addRowSelectionInterval(index, index);
                    }
                }
            }
            this.repaint();
        }
    }

    /**
     * determinate if the move up or move down reach the top/bottm
     *
     * @param selectedRowList model row index
     * @param weight 1=down -1=up
     * @return
     */
    private boolean isEndChannel(List<Integer> selectedRowList, int weight) {
        for (int row : selectedRowList) {
            AbstractNode srcNode = (AbstractNode) this.getNodeForRow(row);
            if (!XICBiologicalSampleAnalysisNode.class.isInstance(srcNode)) {
                continue;
            } else {
                int nextIndex = row + weight;

                if (nextIndex < 0 || nextIndex >= this.getRowCount()) {
                    return true;
                } else {
                    if (this.getNextChannelRowIndex(row, weight) == -1) {
                        return true;
                    }
                    return false;
                }
            }

        }
        return false;
    }

    /**
     * hode the original(DDataset, Channel), used for insertMove
     */
    DQuantitationChannelMapping m_holdChannelMapping;

    /**
     * Continue to browse the table,util to find the next
     * DQuantitationChannelMapping or at the end of the table.
     *
     * @param row
     * @param weight
     * @return the next DQuantitationChannelMapping, or null if it is the end
     * row (up/down end ).
     */
    private DQuantitationChannelMapping getNextRow(int row, int weight) {
        int recoveredRow = row + weight;
        DQuantitationChannelMapping targetMapping;
        while (recoveredRow >= 0 && recoveredRow < this.getRowCount()) {
            targetMapping = m_model.getRowMapping(getNodeForRow(recoveredRow));
            if (targetMapping == null) {
                recoveredRow += weight;
            } else {
                return targetMapping;
            }
        }
        return null;
    }

    private int getNextChannelRowIndex(int row, int weight) {
        int recoveredRow = row + weight;
        DQuantitationChannelMapping targetMapping;
        while (recoveredRow >= 0 && recoveredRow < this.getRowCount()) {
            targetMapping = m_model.getRowMapping(getNodeForRow(recoveredRow));
            if (targetMapping == null) {
                recoveredRow += weight;
            } else {
                return recoveredRow;
            }
        }
        return -1;
    }

    /**
     *
     * @param row model row index
     * @param column model column index
     * @param weight UP/DOWN
     * @return new model row index
     */
    private int setNextRow(int row, int column, int weight) {

        Object srcNode = getNodeForRow(row);
        if (srcNode instanceof XICBiologicalSampleAnalysisNode) {
            DDataset col = m_model.getDatasetAt(column);//col
            //if (XICBiologicalSampleAnalysisNode.class.isInstance(srcNode)) {
            DQuantitationChannelMapping srcRowMapping = m_model.getRowMapping(srcNode);

            QuantitationChannel srcChannel = srcRowMapping.getQuantChannel(col);//value
            int targetIndexChannelNode = this.getNextChannelRowIndex(row, weight);
            if (targetIndexChannelNode != -1) {
                DQuantitationChannelMapping targetRowMapping = m_model.getRowMapping(getNodeForRow(targetIndexChannelNode));

                if (srcChannel != null) {
                    //m_logger.debug("selectedChannelId {}", m_selectedChannelIds.toString());
                    srcRowMapping.remove(col);
                    targetRowMapping.put(col, srcChannel);
                } else {
                    targetRowMapping.put(col, null);

                }
            }
            return targetIndexChannelNode;//row index of the table
        }
        return -1;
    }

    public void moveUp() {
        moveUpDown(UP, false);
    }

    public void moveDown() {
        moveUpDown(DOWN, false);
    }

    public void moveInsertUp() {
        moveUpDown(UP, true);
    }

    public void moveInsertDown() {
        moveUpDown(DOWN, true);
    }

    //select column, row
    protected void manageSelectionOnRightClick(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Point p = new Point(x, y);
        int Coloumn = columnAtPoint(p);
        int row = rowAtPoint(p);
        int[] selectedRows = this.getSelectedRows();
        int[] selectedCols = this.getSelectedColumns();
        if (selectedRows.length != 0) {
            List<Integer> selectedRowList = Arrays.stream(selectedRows).boxed().collect(Collectors.toList());
            if (selectedRowList.contains(row)) {
                List<Integer> selectedColList = Arrays.stream(selectedCols).boxed().collect(Collectors.toList());
                if (selectedColList.contains(Coloumn)) {
                    if (this.isChannel(selectedRowList, selectedColList)) {
                        triggerPopup(e);
                    }
                }
            }
        }
    }

    /**
     * test if all selected cell are Channel, used to determine if trigger Popup
     * Menu
     *
     * @param rowList, view row index
     * @param columnList, view row index
     * @return
     */
    private boolean isChannel(List<Integer> rowList, List<Integer> columnList) {
        Object value;
        for (int row : rowList) {
            Object o = getNodeForRow(convertRowIndexToModel(row));
            for (int column : columnList) {
                int col = convertColumnIndexToModel(column);
                if (col == 0) {
                    return false;
                }
                value = m_model.getChannelAt(o, col);
                if (value == null) {
                    return false;
                }
            }
        }
        return true;
    }

    //remove, up, down action
    private void triggerPopup(MouseEvent e) {
        JPopupMenu popup;
        popup = new JPopupMenu();
        ActionListener menuListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String command = event.getActionCommand();
                if (command.equals(ERRASE)) {
                    removeAssociateChannel();
                } else if (command.equals(INSERT_UP)) {
                    moveUpDown(UP, true);
                } else if (command.equals(INSERT_DOWN)) {
                    moveUpDown(DOWN, true);
                } else if (command.equals(MOVE_UP)) {
                    moveUpDown(UP, false);
                } else if (command.equals(MOVE_DOWN)) {
                    moveUpDown(DOWN, false);
                }
            }
        };
        JMenuItem item;
        popup.add(item = new JMenuItem(ERRASE, IconManager.getIcon(IconManager.IconType.ERASER)));
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.addActionListener(menuListener);
        popup.addSeparator();
        popup.add(item = new JMenuItem(INSERT_UP, IconManager.getIcon(IconManager.IconType.ARROW_INSERT_UP)));
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem(INSERT_DOWN, IconManager.getIcon(IconManager.IconType.ARROW_INSERT_DOWN)));
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.addActionListener(menuListener);
        popup.addSeparator();
        popup.add(item = new JMenuItem(MOVE_UP, IconManager.getIcon(IconManager.IconType.ARROW_MOVE_UP)));
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.addActionListener(menuListener);
        popup.add(item = new JMenuItem(MOVE_DOWN, IconManager.getIcon(IconManager.IconType.ARROW_MOVE_DOWN)));
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.addActionListener(menuListener);

        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
    }

    class PopupAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                manageSelectionOnRightClick(e);
            }
        }
    }
}
