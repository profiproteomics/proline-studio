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

    private void setContinueSelected() {
        int[] rows = getSelectedRows();
        int min, max;
        min = rows[0];
        max = rows[0];
        for (int row : rows) {
            min = Math.min(min, row);
            max = Math.max(max, row);
        }
        this.setRowSelectionInterval(min, max);//selection must be continue
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

    protected void moveUpDown(int weight, boolean isInsertMode) {
        if (!isSelectionOk()) {
            return;
        }
        ArrayList<Integer> selectedModelCols = new ArrayList();
        int[] columnList = getSelectedColumns();
        for (int col : columnList) {
            selectedModelCols.add(convertColumnIndexToModel(col));
        }
        if (isInsertMode) {
            setContinueSelected();
        }
        int[] rows = getSelectedRows();
        ArrayList<Integer> newSelectedModelRows = new ArrayList();
        ArrayList<Integer> selectedModelRows = new ArrayList();
        for (int row : rows) {
            selectedModelRows.add(convertRowIndexToModel(row));
        }

        //get selected row, range it in order ascending(for up) ou descending(for down)
        if (weight == UP) {
            Collections.sort(selectedModelRows);//lower element move first
        } else {
            Collections.sort(selectedModelRows, Collections.reverseOrder());//higher element move first
        }
        if (m_model.isEndChannel(selectedModelRows, weight)) {
            return;
        }
        //move up/down for each cell
        m_model.setSelected(selectedModelRows, selectedModelCols);
        if (isInsertMode) {
            m_model.preInsertMove(selectedModelRows.get(0), selectedModelRows.get(selectedModelRows.size() - 1), selectedModelCols, weight);
        }
        int row, targetRow;
        for (int i = 0; i < selectedModelRows.size(); i++) {
            //for (int row : rowList) {
            row = selectedModelRows.get(i);
            for (int column : selectedModelCols) {
                targetRow = m_model.moveUpDown(row, column, weight);
                m_logger.debug("targetRow: {}", targetRow);
                if (targetRow != -1) {
                    newSelectedModelRows.add(targetRow);
                }
            }
        }
        if (isInsertMode) {
            m_model.postInsertMove(selectedModelRows.get(selectedModelRows.size() - 1), selectedModelCols);
        }
        m_logger.debug("newSelectedRow index: {}", newSelectedModelRows);
        //set the new selected rows columns
        if (newSelectedModelRows.size() > 0) {
            Collections.sort(newSelectedModelRows);
            for (int i = 0; i < newSelectedModelRows.size(); i++) {
                row = newSelectedModelRows.get(i);
                if (row < this.getRowCount()) {
                    int index = convertRowIndexToView(row);
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
        int[] selectedModelRows = new int[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            selectedModelRows[i] = convertRowIndexToModel(selectedRows[i]);
        }
        int[] selectedCols = this.getSelectedColumns();
        int[] selectedModelCols = new int[selectedCols.length];
        for (int i = 0; i < selectedCols.length; i++) {
            selectedModelCols[i] = convertColumnIndexToModel(selectedCols[i]);
        }
        if (selectedRows.length != 0) {
            List<Integer> selectedRowList = Arrays.stream(selectedRows).boxed().collect(Collectors.toList());
            if (selectedRowList.contains(row)) {
                List<Integer> selectedColList = Arrays.stream(selectedCols).boxed().collect(Collectors.toList());
                if (selectedColList.contains(Coloumn)) {
                    if (m_model.isChannelSelected(selectedModelRows, selectedModelCols)) {

                        triggerPopup(e);
                    }
                }
            }
        }
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
