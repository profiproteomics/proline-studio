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
package fr.proline.studio.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/**
 * All Table wich use right click popup should use this Mouse Adapter to
 * correctly manage selection/multi-selection on right clicking
 *
 * @author JM235353
 */
public class TablePopupMouseAdapter extends MouseAdapter {

    private final DecoratedTable m_table;

    public TablePopupMouseAdapter(DecoratedTable table) {
        m_table = table;
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {

            int[] selectedRows = m_table.getSelectedRows();
            int nbSelectedRows = selectedRows.length;
            if (nbSelectedRows == 0) {
                // no row is selected, we select the current row
                int row = m_table.rowAtPoint(e.getPoint());
                if (row != -1) {
                    m_table.getSelectionModel().setSelectionInterval(row, row);
                }
            } else if (nbSelectedRows == 1) {
                // one row is selected
                int row = m_table.rowAtPoint(e.getPoint());
                if ((row != -1) && (e.isShiftDown() || e.isControlDown())) {
                    m_table.getSelectionModel().addSelectionInterval(row, row);
                } else if ((row != -1) && (row != selectedRows[0])) {
                    // we change the selection
                    m_table.getSelectionModel().setSelectionInterval(row, row);
                }
            } else {
                // multiple row are already selected
                // if ctrl or shift is down, we add the row to the selection
                if (e.isShiftDown() || e.isControlDown()) {
                    int row = m_table.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        m_table.getSelectionModel().addSelectionInterval(row, row);
                    }
                }
            }

            TablePopupMenu popup = m_table.getTablePopup();
            if (popup != null) {
                m_table.prepostPopupMenu();
                popup.show(e.getX(), e.getY(), m_table);
            }
        }

    }
}
