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
     * @param config
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
