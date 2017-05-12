package fr.proline.studio.export;

import java.util.ArrayList;

/**
 * Export interface, to have data as string and column names
 * @author JM235353
 */
public interface ExportModelInterface {
    
    /**
     * specifies the text to export for a given cell
     *
     * @param row
     * @param col
     * @return
     */
    public String getExportRowCell(int row, int col);
    
    /**
     * specifies colors for the exported text
     *
     * @param row
     * @param col
     * @return
     */
    public ArrayList<ExportSubStringFont> getSubStringFonts(int row, int col);
        
    public String getExportColumnName(int col);
}
