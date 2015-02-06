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
