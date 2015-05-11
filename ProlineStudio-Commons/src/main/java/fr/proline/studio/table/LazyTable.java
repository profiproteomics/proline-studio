package fr.proline.studio.table;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.openide.windows.WindowManager;

/**
 * Table to deal with LazyData (data which will be loaded later)
 *
 * @author JM235353
 */
public abstract class LazyTable extends DecoratedMarkerTable implements AdjustmentListener, ProgressInterface {

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
    protected LastAction m_lastAction = LastAction.ACTION_NONE;

    public LazyTable(JScrollBar verticalScrollbar) {

        setDefaultRenderer(LazyData.class, new LazyTableCellRenderer());

        final LazyTable table = this;


        
        // look for sorting column
        getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (!isSortable()) {

                    ProgressInterface progressInterface = (ProgressInterface) table.getModel();
                    
                    ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), progressInterface, "Data loading", "Sorting is not available while data is loading. Please Wait.");
                    dialog.setLocation(e.getLocationOnScreen());
                    dialog.setVisible(true);

                    if (!dialog.isWaitingFinished()) {
                        return;
                    }
                    
                    int col = columnAtPoint(e.getPoint());
                    col = (col == -1) ? -1 : convertColumnIndexToModel(col);

                    DefaultRowSorter sorter = ((DefaultRowSorter) table.getRowSorter());
                    ArrayList list = new ArrayList();
                    list.add(new RowSorter.SortKey(col, SortOrder.ASCENDING));
                    sorter.setSortKeys(list);
                    sorter.sort();

                    return;
                }

                
                
                int col = columnAtPoint(e.getPoint());
                col = (col==-1) ? -1 : convertColumnIndexToModel(col);
                
                
                m_lastAction = LastAction.ACTION_SORTING;

                ((LazyTableModelInterface) getModel()).sortingChanged(col);
                sortingChanged(col);
            }
        });

        // look for scrolling
        verticalScrollbar.addAdjustmentListener(this);
        
        // sorting forbidden while data is not loaded
        setSortable(false);
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

        m_lastAction = LastAction.ACTION_SCROLLING;

        Long taskId = ((LazyTableModelInterface) getModel()).getTaskId();
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

        m_lastAction = LastAction.ACTION_SELECTING;
    }

    /**
     * To be called to keep a row visible without impacting the last action done
     * by the user
     *
     * @param row
     */
    @Override
    public void scrollRowToVisible(int row) {
        LastAction keepAction = m_lastAction;
        try {
            super.scrollRowToVisible(row);
        } finally {
            m_lastAction = keepAction;
        }
    }

    /**
     * To be called to select a row without impacting the last action done by
     * the user
     *
     * @param row
     */
    public void setSelection(int row) {
        LastAction keepAction = m_lastAction;
        try {
            getSelectionModel().setSelectionInterval(row, row);
        } finally {
            m_lastAction = keepAction;
        }
    }
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        TableModel model = getModel();
        if (model instanceof GlobalTableModelInterface) {
            int columnInModel = convertColumnIndexToModel(column);

            TableCellRenderer renderer = ((GlobalTableModelInterface) model).getRenderer(columnInModel);
            if (renderer != null) {
                Class columnClass = model.getColumnClass(columnInModel);
                if (columnClass.equals(LazyData.class)) {
                    TableCellRenderer registeredRenderer = m_rendererMap.get(column);
                    if (registeredRenderer != null) {
                        return registeredRenderer;
                    }
                    renderer = new LazyTableCellRenderer(renderer);
                    m_rendererMap.put(column, renderer);
                    return renderer;
                } else {
                    return renderer;
                }
            }
        }

        return super.getCellRenderer(row, column);
    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
}
