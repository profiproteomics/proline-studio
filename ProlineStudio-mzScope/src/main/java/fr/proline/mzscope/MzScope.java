/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope;

import fr.proline.mzscope.ui.MzScopePanel;
import java.awt.Frame;
import java.io.File;
import java.util.List;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main entry point for mzscope
 * @author MB243701
 */
public class MzScope implements IMzScope{

    private final static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope");
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


    @Override
    public void openRaw(File file) {
        logger.debug("openRaw for "+file.getName());
        mzScopePanel.openRaw(file);
    }


    @Override
    public void openRaw(List<File> files) {
        logger.debug("openRaw for list");
        mzScopePanel.openRaw(files);
    }

    @Override
    public void detectPeakels(File file) {
        logger.debug("detectPeakel on "+file.getName());
        mzScopePanel.detectPeakels(file);
    }

    @Override
    public void detectPeakels(List<File> fileList) {
        logger.debug("detectPeakel for list ");
        mzScopePanel.detectPeakels(fileList);
    }

}
