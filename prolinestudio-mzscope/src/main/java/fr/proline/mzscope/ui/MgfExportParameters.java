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

import fr.profi.mzdb.io.writer.mgf.IPrecursorComputation;
import fr.proline.mzscope.model.IExportParameters;

/**
 * parameters needed for mgf export
 * @author MB243701
 */
public class MgfExportParameters implements IExportParameters{

    private IPrecursorComputation precComp;
    
    private float mzTolPPM;
    private float intensityCutoff;
    private boolean exportProlineTitle;

    public MgfExportParameters() {
    }

    public MgfExportParameters(IPrecursorComputation precComp, float mzTolPPM, float intensityCutoff, boolean exportProlineTitle) {
        this.precComp = precComp;
        this.mzTolPPM = mzTolPPM;
        this.intensityCutoff = intensityCutoff;
        this.exportProlineTitle = exportProlineTitle;
    }
    
   
    
    @Override
    public ExportType getExportType() {
        return IExportParameters.ExportType.MGF;
    }

    public IPrecursorComputation getPrecComp() {
        return precComp;
    }

    public void setPrecComp(IPrecursorComputation precComp) {
        this.precComp = precComp;
    }

    public float getMzTolPPM() {
        return mzTolPPM;
    }

    public void setMzTolPPM(float mzTolPPM) {
        this.mzTolPPM = mzTolPPM;
    }

    public float getIntensityCutoff() {
        return intensityCutoff;
    }

    public void setIntensityCutoff(float intensityCutoff) {
        this.intensityCutoff = intensityCutoff;
    }

    public boolean isExportProlineTitle() {
        return exportProlineTitle;
    }

    public void setExportProlineTitle(boolean exportProlineTitle) {
        this.exportProlineTitle = exportProlineTitle;
    }
    
    
}
