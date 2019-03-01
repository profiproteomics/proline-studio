/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IExportParameters;

/**
 * parameters needed for scan header export as TSV
 * @author MB243701
 */
public class ScanHeaderExportParameters implements IExportParameters{

    private ScanHeaderType scanHeadertype;
    
    public ScanHeaderExportParameters() {
    }

    public ScanHeaderExportParameters(ScanHeaderType scanHeadertype) {
        this.scanHeadertype = scanHeadertype;
    }

    @Override
    public ExportType getExportType() {
        return IExportParameters.ExportType.SCAN_HEADER;
    }

    public ScanHeaderType getScanHeadertype() {
        return scanHeadertype;
    }

    public void setScanHtype(ScanHeaderType scanHeadertype) {
        this.scanHeadertype = scanHeadertype;
    }

    
}
