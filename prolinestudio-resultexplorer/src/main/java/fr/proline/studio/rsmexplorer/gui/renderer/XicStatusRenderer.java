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

import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon.PepIonStatus;
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
public class XicStatusRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {

    private final RendererMouseCallback m_callback;
    private final int m_column;
    
    public XicStatusRenderer(RendererMouseCallback callback, int column) {
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

    
    public static class SelectLevel implements Comparable<SelectLevel> {
        public SelectLevelEnum m_status;
        public SelectLevelEnum m_globalStatus;

        public SelectLevel(SelectLevelEnum status, SelectLevelEnum globalStatus) {
            m_status = status;
            m_globalStatus = globalStatus;
        }

        public String getDescription() {
            return m_status.getDescription();
        }

        public SelectLevelEnum getStatus() {
            return m_status;
        }

        @Override
        public int compareTo(SelectLevel o) {
            int deltaStatus = m_globalStatus.getIntValue()-o.m_globalStatus.getIntValue();
            if (deltaStatus != 0) {
                return deltaStatus;
            }
            return m_status.getIntValue()-o.m_status.getIntValue();

        }

    }



    public static String getPepIonStatusText(PepIonStatus status) {
        switch (status) {
            case UNKNOWN:
                return "Unknown";
            case INVALIDATED:
                return "Invalidated";
            case VALIDATED:
                return "Validated";
            case USED_VALIDATED:
                return "Validated/Used";
            case UNUSED_VALIDATED:
                return "Validated/Not Used";
            default:
                return "Other";
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Object status = value;

        if (status instanceof SelectLevel) {
            SelectLevelEnum globalStatusValue = ((SelectLevel) status).m_globalStatus;
            SelectLevelEnum statusValue = ((SelectLevel) status).m_status;

            boolean locallyModified = statusValue.getIntValue() != globalStatusValue.getIntValue();
            switch (statusValue) {
                case DESELECTED_MANUAL: //0 
                case DESELECTED_AUTO: //1
                    if (globalStatusValue.isDeselected()) {
                        setIcon(IconManager.getIcon(IconManager.IconType.INVALIDATED));
                    } else {
                        setIcon(IconManager.getIcon(IconManager.IconType.INVALIDATED_LOCALLY));
                    }
                    
                    break;
                case SELECTED_AUTO: //2
                case SELECTED_MANUAL: //3
                    if (globalStatusValue.isSelected()) {
                        setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED));
                    } else {
                        setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED_LOCALLY));
                    }
                    break;
                default:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));

            }
            this.setToolTipText(statusValue.getDescription());

        } else if (status instanceof PepIonStatus) {    
            switch ((PepIonStatus) status) {

                case INVALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.INVALIDATED));
                    break;
                case VALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED));
                    break;
                case USED_VALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED_AND_USED));
                    break;
                case UNUSED_VALIDATED:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.VALIDATED_AND_NOTUSED));
                    break;
                case UNKNOWN:
                default:
                    this.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16));
            }
            this.setToolTipText(getPepIonStatusText((PepIonStatus) status));
        }

        if (isSelected) {
            this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
            this.setForeground(Color.WHITE);
        } else {
            this.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background"));
            this.setForeground(Color.BLACK);
        }

        return this;
    }
}
