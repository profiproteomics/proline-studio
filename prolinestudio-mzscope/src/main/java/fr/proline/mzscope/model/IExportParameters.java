package fr.proline.mzscope.model;

/**
 * export parameters (could be MGF or TSV for ScanHeader)
 * @author MB243701
 */
public interface IExportParameters {
    
    enum ExportType {MGF, SCAN_HEADER} ;  
    
    public ExportType getExportType();
    
}
