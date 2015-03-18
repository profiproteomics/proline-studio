/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope;

import java.io.File;

/**
 * mzscope interface
 * @author MB243701
 */
public interface IMzScope {
    /**
     * open the specified file, and extract at the moz specified value
     * @param file
     * @param moz 
     * @param elutionTime 
     * @param firstScanTime 
     * @param lastScanTime 
     */
    public abstract void openRawAndExtract(File file, double moz, double elutionTime, double firstScanTime, double lastScanTime);
    
    /**
     * extract chromatrogram on an existing file at the specified moz
     * @param file
     * @param moz 
     * @param elutionTime 
     * @param firstScanTime 
     * @param lastScanTime 
     */
    public abstract void extractRawFile(File file, double moz, double elutionTime, double firstScanTime, double lastScanTime);
}
