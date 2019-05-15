/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 24 avr. 2019
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
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
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
    //private boolean m_isAltDown;

    public QCMappingTreeTable(QCMappingTreeTableModel treeModel) {
        super(treeModel);
        m_model = treeModel;
        m_model.cloneMapping();
        setCellSelectionEnabled(true);
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

    protected void moveUpDown(int weight, boolean isInsertMode) {
        if (getSelectedRows().length == 0) {
            return;
        }
        if (isInsertMode) {
            setContinueSelected();
        }
        int[] rows = getSelectedRows();
        //get selected row, range it in order ascending(for up) ou descending(for down)
        List<Integer> newSelectedRows = new ArrayList();
        List<Integer> rowList = Arrays.stream(rows).boxed().collect(Collectors.toList());
        if (weight == UP) {
            Collections.sort(rowList);//lower element move first

        } else {
            Collections.sort(rowList, Collections.reverseOrder());//higher element move first
        }
        if (m_model.isEndChannel(rowList, weight)) {
            return;
        }
        //move up/down for each cell
        int[] columnList = getSelectedColumns();
        m_model.setSelected(getSelectedRows(), getSelectedColumns());
        if (isInsertMode) {
            m_model.preInsertMove(rowList.get(0), rowList.get(rowList.size() - 1), columnList, weight);
        }
        int row, targetRow;
        for (int i = 0; i < rowList.size(); i++) {
            //for (int row : rowList) {
            row = rowList.get(i);
            for (int column : columnList) {
                if (column == 0) {
                    continue;
                }
                targetRow = m_model.moveUpDown(row, column, weight);
                m_logger.debug("targetRow: {}",targetRow );
                if (targetRow != -1) {
                    newSelectedRows.add(targetRow);
                }
            }
        }
        if (isInsertMode) {
            m_model.postInsertMove(rowList.get(rowList.size() - 1), columnList);
        }
        m_logger.debug("newSelectedRow index: {}", newSelectedRows);
        //set the new selected rows colomns
        if (newSelectedRows.size() > 0) {
            Collections.sort(newSelectedRows);
            int firstRow = newSelectedRows.get(0);
            for (int i = 0; i < newSelectedRows.size(); i++) {
                row = newSelectedRows.get(i);
                if (i == 0) {
                    setRowSelectionInterval(row, row);
                } else {
                    addRowSelectionInterval(row, row);;
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
        int[] selectedCols = this.getSelectedColumns();
        if (selectedRows.length != 0) {
            List<Integer> selectedRowList = Arrays.stream(selectedRows).boxed().collect(Collectors.toList());
            if (selectedRowList.contains(row)) {
                List<Integer> selectedColList = Arrays.stream(selectedCols).boxed().collect(Collectors.toList());
                if (selectedColList.contains(Coloumn)) {
                    if (m_model.isChannelSelected(selectedRows, selectedCols)) {
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
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
//                if (SwingUtilities.isRightMouseButton(e)) {
//                    manageSelectionOnRightClick(e);
//                }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                manageSelectionOnRightClick(e);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}
