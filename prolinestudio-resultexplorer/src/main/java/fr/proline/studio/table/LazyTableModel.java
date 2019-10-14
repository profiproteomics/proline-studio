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

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.graphics.PlotDataSpec;

/**
 * Table Model to deal with LazyData (data which will be loaded later)
 *
 * @author JM235353
 */
public abstract class LazyTableModel extends DecoratedTableModel implements LazyTableModelInterface {

    protected LazyTable m_table;
    protected Long m_taskId = null;

    public LazyTableModel(LazyTable table) {
        m_table = table;
    }

    @Override
    public Long getTaskId() {
        return m_taskId;
    }

    /**
     * Return a LadyData to be used in the LazyTableModel. The LazyData instance
     * is the same for each column, but different for each row, due to sorting
     * problems
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public LazyData getLazyData(int row, int col) {

        int nb = getRowCount();
        if ((m_lazyDataArray == null) || (m_lazyDataArray.length != nb)) {
            m_lazyDataArray = new LazyData[nb];
            for (int i = 0; i < nb; i++) {
                m_lazyDataArray[i] = new LazyData();
            }
        }
        return m_lazyDataArray[row];

    }
    private LazyData[] m_lazyDataArray = null;

    /**
     * Give priority to the LazyData in (col,row) which is not already loaded If
     * there is no sorting, the row is added to a range as a priority If there
     * is a sorting, the row is not added, it replaces the old priority index
     * and the range is an unique index (after a sort, indexes in a range of
     * displayed data are no longer continuous)
     *
     * @param taskId
     * @param row
     * @param col
     */
    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        int sortedCol = m_table.getSortedColumnIndex();
        if (sortedCol == -1) {
            AccessDatabaseThread.getAccessDatabaseThread().addPriorityIndex(taskId, row);
        } else {
            AccessDatabaseThread.getAccessDatabaseThread().setPriorityIndex(taskId, row);
        }
    }

    /**
     * Called when sorting has been change by the user. In this case, the
     * priority is given to the data of the column sorted (to be able to finish
     * as soon as possible the sorting)
     *
     * @param col
     */
    @Override
    public void sortingChanged(int col) {
        int subTaskId = getSubTaskId(col);
        if (subTaskId != -1) {
            AccessDatabaseThread.getAccessDatabaseThread().givePriorityToSubTask(m_taskId, subTaskId);
        }
    }

    /**
     * Return the subTask id corresponding to the column (to each column with
     * possible Lazy Data must correspond a subTaskId)
     *
     * @param col
     * @return
     */
    @Override
    public abstract int getSubTaskId(int col);

    @Override
    public PlotDataSpec getDataSpecAt(int row) {
        return null;
    }

}
