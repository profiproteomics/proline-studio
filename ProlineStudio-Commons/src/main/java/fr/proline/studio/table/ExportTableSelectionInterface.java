package fr.proline.studio.table;

import java.util.HashSet;

/**
 * Interface to export a selection from a table
 * @author JM235353
 */
public interface ExportTableSelectionInterface {
    
    public HashSet exportSelection(int[] rows);
    
}
