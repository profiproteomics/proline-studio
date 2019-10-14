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

import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.DecoratedMarkerTable;
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
import fr.proline.studio.table.renderer.GrayedRenderer;

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

    private boolean m_sortForbidden = false;
    
    public LazyTable(JScrollBar verticalScrollbar) {

        setDefaultRenderer(LazyData.class, new LazyTableCellRenderer());

        final LazyTable table = this;


        
        // look for sorting column
        getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (m_sortForbidden) {
                    return;
                }
                
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

    public void forbidSort(boolean sortForbidden) {
        m_sortForbidden = sortForbidden;
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
            int rowInModel = convertRowIndexToModel(row);
            
            TableCellRenderer renderer = ((GlobalTableModelInterface) model).getRenderer(rowInModel, columnInModel);
            if (renderer != null) {
                //Class columnClass = model.getColumnClass(columnInModel);
                Object value = model.getValueAt(rowInModel, columnInModel);
                
                if ((value != null) && value.getClass().equals(LazyData.class)) {
                    
                    m_sb.append(columnInModel);
                    if (renderer instanceof GrayedRenderer) {
                        m_sb.append("grayed" );
                    }
                    if (((LazyData)value).getData() != null) {
                        m_sb.append(((LazyData)value).getData().getClass().getName());
                    }
                    String key = m_sb.toString();
                    m_sb.setLength(0);
                    
                    TableCellRenderer registeredRenderer = m_rendererMap.get(key);
                    if (registeredRenderer != null) {
                        return registeredRenderer;
                    }
                    
                    
                    renderer = new LazyTableCellRenderer(renderer);
                    m_rendererMap.put(key, renderer);
                    return renderer;
                } else {
                    return renderer;
                }
            }
        }

        return super.getCellRenderer(row, column);
    }
    private final HashMap<String, TableCellRenderer> m_rendererMap = new HashMap();
    private final StringBuilder m_sb = new StringBuilder();
}
