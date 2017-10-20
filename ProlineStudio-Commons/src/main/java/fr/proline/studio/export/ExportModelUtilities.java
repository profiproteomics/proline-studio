package fr.proline.studio.export;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 *
 * @author JM235353
 */
public class ExportModelUtilities {
    
    public static String getExportRowCell(ExtendedTableModelInterface dataInterface, int row, int col) {
        Object o = dataInterface.getDataValueAt(row, col);
        if (o != null) {
            if ((o instanceof Double) && (((Double) o).isNaN())) {
                return "";
            } 
            if ((o instanceof Float) && (((Float) o).isNaN())) {
                return "";
            }
            return o.toString();
        }
        return null;
    }
    
}
