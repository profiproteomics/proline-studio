/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.export;

/**
 * This interface allows a table to specify the text of a cell to export
 * @author MB243701
 */
public interface ExportRowTextInterface {
    
    /**
     * specifies the text to export for a given cell
     * @param row
     * @param col
     * @return 
     */
    public String getExportRowCell(int row, int col);
}
