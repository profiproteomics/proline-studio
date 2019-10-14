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
