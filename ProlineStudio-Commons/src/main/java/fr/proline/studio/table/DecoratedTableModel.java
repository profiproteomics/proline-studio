package fr.proline.studio.table;

import javax.swing.table.AbstractTableModel;

/**
 * 
 * @author JM235353
 */
public abstract class DecoratedTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }
    
    public int uniqueId2Row(long id) {
        return (int) id;
    }
}
