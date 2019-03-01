/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import java.util.Map;

/**
 *
 * @author MB243701
 */
public interface IExtractionResultsViewer {
    
    public void displayChromatogramAsSingleView(IRawFile rawfile, Chromatogram c);
    
    public void displayChromatogramAsMultiView(Map<IRawFile, Chromatogram> chromatogramByRawFile);
}
