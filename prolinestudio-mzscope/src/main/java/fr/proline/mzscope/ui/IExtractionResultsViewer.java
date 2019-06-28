/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IChromatogram;
import fr.proline.mzscope.model.IRawFile;
import java.util.Map;

/**
 *
 * @author MB243701
 */
public interface IExtractionResultsViewer extends IFeatureViewer {
    
    public void displayChromatogramAsSingleView(IRawFile rawfile, IChromatogram c);
    
    public void displayChromatogramAsMultiView(Map<IRawFile, IChromatogram> chromatogramByRawFile);
}
