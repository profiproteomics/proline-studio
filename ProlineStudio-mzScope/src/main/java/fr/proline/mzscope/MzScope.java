/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope;

import fr.proline.mzscope.ui.MzScopePanel;
import java.awt.Frame;
import java.io.File;
import javax.swing.JPanel;

/**
 * main entry point for mzscope
 * @author MB243701
 */
public class MzScope implements IMzScope{

    private MzScopePanel mzScopePanel;
    
    public MzScope() {
    }
    
    /**
     * create the mzscope panel, relative to the specified frame
     * @param frame
     * @return 
     */
    public JPanel createMzScopePanel(Frame frame){
        mzScopePanel = new MzScopePanel(frame);
        return mzScopePanel;
        
    }

    /**
     * open the specified file, and extract at the moz specified value
     * @param file
     * @param moz 
     * @param elutionTime 
     * @param firstScanTime 
     * @param lastScanTime 
     */
    @Override
    public void openRawAndExtract(File file, double moz, double elutionTime, double firstScanTime, double lastScanTime) {
        mzScopePanel.openRawAndExtract(file, moz, elutionTime, firstScanTime, lastScanTime);
    }

    
    /**
     * extract chromatrogram on an existing file at the specified moz
     * @param file
     * @param moz 
     * @param elutionTime 
     * @param firstScanTime 
     * @param lastScanTime 
     */
    @Override
    public void extractRawFile(File file, double moz, double elutionTime, double firstScanTime, double lastScanTime) {
        mzScopePanel.extractRawFile(file, moz, elutionTime,  firstScanTime, lastScanTime);
    }
}
