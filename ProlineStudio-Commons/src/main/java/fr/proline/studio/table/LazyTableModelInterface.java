package fr.proline.studio.table;

/**
 *
 * @author JM235353
 */
public interface LazyTableModelInterface {
    
    public Long getTaskId();
    
    /**
     * Return a LadyData to be used in the LazyTableModel.
     * The LazyData instance is always the same when the table is not sorted.
     * When the table is sorted the LazyData returned is different for each row.
     * This is necessary for the compare used for the sorting
     * @param row
     * @param col
     * @return 
     */
    public LazyData getLazyData(int row, int col);
    
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
    public void givePriorityTo(Long taskId, int row, int col);
    
    /**
     * Called when sorting has been change by the user. In this case, the priority
     * is given to the data of the column sorted (to be able to finish as soon as possible
     * the sorting)
     * @param col 
     */
    public void sortingChanged(int col);
    
    /**
     * Return the subTask id corresponding to the column
     * (to each column with possible Lazy Data must correspond a subTaskId)
     * @param col
     * @return 
     */
    public int getSubTaskId(int col);
}
