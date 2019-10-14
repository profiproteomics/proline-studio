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
package fr.proline.studio.export;

/**
 *
 * Define the Excel Export configuration
 */
public class ExportConfig {
    String format = "xlsx";
    String decimal_separator = ".";
    String date_format = "YYYY:MM:DD HH:mm:ss";
    ExportDataExport data_export;

    //tab title, and it's table
    ExportExcelSheet[] sheets;

    // extra infos for default options (sent from server only)
    String[] format_values; //["xlsx","tsv"],
    String[] decimal_separator_values; //": [".",","],
    String[] date_format_values; //": ["YYYY:MM:DD HH:mm:ss","YYYY:MM:DD"],
    String[] sheet_presentation_values; //": ["rows","columns"]

    /**
     * get the index of sheet, a sheet means the title of the tab
     *
     * @param sheetId
     * @return
     */
    public int getIndexOfSheet(String sheetId) {
        int index = -1;
        for (int i = 0; i < sheets.length; i++) {
            if (sheets[i].id.equals(sheetId)) {
                index = i;
            }
        }
        return index;

    }
    
}
