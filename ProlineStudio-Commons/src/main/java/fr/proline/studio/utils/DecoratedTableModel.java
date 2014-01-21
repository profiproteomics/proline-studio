package fr.proline.studio.utils;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public abstract class DecoratedTableModel extends AbstractTableModel {

    public abstract String getToolTipForHeader(int col);
    
}
