package fr.proline.studio.utils;

import fr.proline.studio.dam.AccessDatabaseThread;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollBar;
import javax.swing.event.ListSelectionEvent;

/**
 * Table to deal with LazyData (data which will be loaded later)
 *
 * @author JM235353
 */
public class LazyTable extends DecoratedTable implements AdjustmentListener {

    // used to register the last action done by the user.
    // this last action is used to know if we automatically scroll
    // to keep the selected row when new data is loaded in the sorting
    // column
    public enum LastAction {

        ACTION_NONE,
        ACTION_SORTING,
        ACTION_SELECTING,
        ACTION_SCROLLING
    }
    protected LastAction lastAction = LastAction.ACTION_NONE;

    public LazyTable(JScrollBar verticalScrollbar) {

        setDefaultRenderer(LazyData.class, new LazyTableCellRenderer());

        // look for sorting column
        getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int col = columnAtPoint(e.getPoint());

                lastAction = LastAction.ACTION_SORTING;

                ((LazyTableModel) getModel()).sortingChanged(col);
                sortingChanged(col);
            }
        });

        // look for scrolling
        verticalScrollbar.addAdjustmentListener(this);
    }

    /**
     * Called when sorting is changed by the user
     *
     * @param col
     */
    public void sortingChanged(int col) {
    }

    /**
     * Called when the user scroll the table.
     *
     * @param e
     */
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {

        lastAction = LastAction.ACTION_SCROLLING;

        Long taskId = ((LazyTableModel) getModel()).getTaskId();
        if (taskId != null) {
            AccessDatabaseThread.getAccessDatabaseThread().clearIndexPriorityTo(taskId);
        }

    }

    /**
     * Called when the user select a new row
     *
     * @param e
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);

        lastAction = LastAction.ACTION_SELECTING;
    }

    /**
     * To be called to keep a row visible without impacting the last action done
     * by the user
     *
     * @param row
     */
    @Override
    public void scrollRowToVisible(int row) {
        LastAction keepAction = lastAction;
        try {
            super.scrollRowToVisible(row);
        } finally {
            lastAction = keepAction;
        }
    }

    /**
     * To be called to select a row without impacting the last action done by
     * the user
     *
     * @param row
     */
    public void setSelection(int row) {
        LastAction keepAction = lastAction;
        try {
            getSelectionModel().setSelectionInterval(row, row);
        } finally {
            lastAction = keepAction;
        }
    }
}
