package fr.proline.studio.export;

public class ExportConfig {
	String format = "xlsx";
	String decimal_separator = ".";
	String date_format = "YYYY:MM:DD HH:mm:ss";
	ExportDataExport data_export;
	ExportExcelSheet[] sheets;
	
	// extra infos for default options (sent from server only)
	String[] format_values; //["xlsx","tsv"],
    String[] decimal_separator_values; //": [".",","],
    String[] date_format_values ; //": ["YYYY:MM:DD HH:mm:ss","YYYY:MM:DD"],
    String[] sheet_presentation_values; //": ["rows","columns"]

	
}
