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
package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.dam.data.SelectLevelEnum;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *
 * @author KX257079
 */
public class SelectLevelRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {

    private final RendererMouseCallback m_callback;
    private final int m_column;

    public SelectLevelRenderer(RendererMouseCallback callback, int column) {
        m_callback = callback;
        m_column = column;
    }


    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (m_callback == null) {
            return;
        }

        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (e.isShiftDown() || e.isControlDown()) {
            return; // multi selection ongoing
        }

        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);

        int modelCol = table.convertColumnIndexToModel(col);

        if ((modelCol != m_column) || (row == -1)) {
            return;
        }

        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();

        int columnStart = 0;
        for (int i=0;i<col;i++) {
            TableColumn column = tcm.getColumn(i);
            columnStart += column.getWidth();
        }

        // check that the user has clicked on the icon
        if (columnStart+20<e.getX()) {
            return;
        }

        m_callback.mouseAction(e);

    }

    private void checkCursor(MouseEvent e) {

        if (m_callback == null) {
            return;
        }

        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);
        int modelCol = table.convertColumnIndexToModel(col);

        if ((modelCol != m_column) || (row == -1)) {
            table.setCursor(Cursor.getDefaultCursor());
            return;
        }

        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();

        int columnStart = 0;
        for (int i = 0; i < col; i++) {
            TableColumn column = tcm.getColumn(i);
            columnStart += column.getWidth();
        }

        // check that the user is over the icon
        if (columnStart + 20 < e.getX()) {
            table.setCursor(Cursor.getDefaultCursor());
            return;
        }

        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof SelectLevelEnum) {

            switch ((SelectLevelEnum) value) {
                case DESELECTED_MANUAL: //0
                case DESELECTED_AUTO: //1
                    setIcon(IconManager.getIcon(IconManager.IconType.INVALIDATED));
                    break;
                case SELECTED_AUTO: //2
                case SELECTED_MANUAL: //3
                    setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED));
                    break;
                default:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));

            }
            this.setToolTipText( ((SelectLevelEnum) value).getDescription());

        }

        if (isSelected) {
            this.setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
            this.setForeground(Color.WHITE);
        } else {
            this.setBackground(UIManager.getDefaults().getColor("Table.background"));
            this.setForeground(Color.BLACK);
        }

        return this;
    }
}
