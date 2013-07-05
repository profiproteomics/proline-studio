package fr.proline.studio.utils;

import fr.proline.studio.dam.AccessDatabaseThread;
import javax.swing.table.AbstractTableModel;


/**
 * Table Model to deal with LazyData (data which will be loaded later)
 * @author JM235353
 */
public abstract class LazyTableModel extends AbstractTableModel {
    
    protected LazyTable m_table;
    protected Long m_taskId = null;
    
    public LazyTableModel(LazyTable table) {
        m_table = table;
    }

    public Long getTaskId() {
        return m_taskId;
    }
    
    /**
     * Return a LadyData to be used in the LazyTableModel.
     * The LazyData instance is always the same when the table is not sorted.
     * When the table is sorted the LazyData returned is different for each row.
     * This is necessary for the compare used for the sorting
     * @param row
     * @param col
     * @return 
     */
    protected LazyData getLazyData(int row, int col) {
        
        int sortedColumn = ((LazyTable) m_table).getSortedColumnIndex();
        sortedColumn = (sortedColumn == -1) ? -1 : m_table.convertColumnIndexToModel(sortedColumn);
        
        if ( sortedColumn == col) {
            int nb = getRowCount();
            if ((dataForSortedColumn == null) || (dataForSortedColumn.length != nb)) {
                dataForSortedColumn = new LazyData[nb];
                for (int i = 0; i < nb; i++) {
                    dataForSortedColumn[i] = new LazyData();
                }
            }
            return dataForSortedColumn[row];
        }

        return singleton;
    }
    LazyData singleton = new LazyData();
    LazyData[] dataForSortedColumn = null;
    
    /**
     * Give priority to the LazyData in (col,row) which is not already loaded
     * If there is no sorting, the row is added to a range as a priority
     * If there is a sorting, the row is not added, it replaces the old priority index
     * and the range is an unique index (after a sort, indexes in a range of displayed
     * data are no longer continuous)
     * @param taskId
     * @param row
     * @param col 
     */
    protected void givePriorityTo(Long taskId, int row, int col) {
        int sortedCol = m_table.getSortedColumnIndex();
        if (sortedCol ==-1) {
            AccessDatabaseThread.getAccessDatabaseThread().addPriorityIndex(taskId, row);
        } else {
            AccessDatabaseThread.getAccessDatabaseThread().setPriorityIndex(taskId, row);
        }
    }
    
    /**
     * Called when sorting has been change by the user. In this case, the priority
     * is given to the data of the column sorted (to be able to finish as soon as possible
     * the sorting)
     * @param col 
     */
    public void sortingChanged(int col) {
        int subTaskId = getSubTaskId(col);
        if (subTaskId != -1) {
           AccessDatabaseThread.getAccessDatabaseThread().givePriorityToSubTask(m_taskId, subTaskId);
        }
    }
    
    /**
     * Return the subTask id corresponding to the column
     * (to each column with possible Lazy Data must correspond a subTaskId)
     * @param col
     * @return 
     */
    public abstract int getSubTaskId(int col);
    
}
