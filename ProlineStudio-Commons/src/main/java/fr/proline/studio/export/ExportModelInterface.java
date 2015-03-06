package fr.proline.studio.export;

/**
 *
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
    
        
    public String getExportColumnName(int col);
}
