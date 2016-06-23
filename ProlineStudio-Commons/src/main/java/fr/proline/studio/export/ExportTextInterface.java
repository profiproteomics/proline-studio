package fr.proline.studio.export;

import java.util.ArrayList;

/**
 * Used by renderer to export data as text without rendering
 * @author JM235353
 */
public interface ExportTextInterface {
    
    public String getExportText();
    
    public ArrayList<ExportSubStringFont> getSubStringFonts();
    
}
