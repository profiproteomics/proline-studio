package fr.proline.mzscope.ui;

import fr.profi.mzdb.io.writer.mgf.PrecursorMzComputation;
import fr.proline.mzscope.model.IExportParameters;

/**
 * parameters needed for mgf export
 * @author MB243701
 */
public class MgfExportParameters implements IExportParameters{

    private PrecursorMzComputation precComp;
    
    private float mzTolPPM;
    private float intensityCutoff;
    private boolean exportProlineTitle;

    public MgfExportParameters() {
    }

    public MgfExportParameters(PrecursorMzComputation precComp, float mzTolPPM, float intensityCutoff, boolean exportProlineTitle) {
        this.precComp = precComp;
        this.mzTolPPM = mzTolPPM;
        this.intensityCutoff = intensityCutoff;
        this.exportProlineTitle = exportProlineTitle;
    }
    
   
    
    @Override
    public ExportType getExportType() {
        return IExportParameters.ExportType.MGF;
    }

    public PrecursorMzComputation getPrecComp() {
        return precComp;
    }

    public void setPrecComp(PrecursorMzComputation precComp) {
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
