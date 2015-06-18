package fr.proline.mzscope.ui.event;

import fr.proline.mzscope.model.IRawFile;
import java.util.EventListener;
import java.util.List;

/**
 * display raw event listener
 * @author MB243701
 */
public interface RawFileListener extends EventListener {
    
    /**
     * display raw for a given raw file
     * @param rawfile 
     */
    public void displayRaw(IRawFile rawfile);
    
    /**
     * display raw for a given list of raw files
     * @param rawfiles 
     */
    public void displayRaw(List<IRawFile> rawfiles);
    
    /**
     * opne raw file command
     */
    public void openRawFile() ;
    
    /**
     * close the given raw file
     * @param rawfile 
     */
    public void closeRawFile(IRawFile rawfile);
    
    /**
     * close all raw files in the list
     */
    public void closeAllFiles();
    
    /**
     * extract the features for the given raw file
     * @param rawfile 
     */
    public void extractFeatures(IRawFile rawfile);
    
    /**
     * extract the features for the givel raw file list
     * @param rawfiles 
     */
    public void extractFeatures(List<IRawFile> rawfiles);
    
    /**
     * detect peakels fo rthe given raw file
     * @param rawfile 
     */
    public void detectPeakels(IRawFile rawfile);
    
    /**
     * detect peakels for the given raw files list
     * @param rawfiles 
     */
    public void detectPeakels(List<IRawFile> rawfiles);
     
    /**
     * export chromatogram for the given raw file
     * @param rawfile 
     */
    public void exportChromatogram(IRawFile rawfile);
    
    /**
     * export chromatogram for the given raw files list
     * @param rawfiles 
     */
    public void exportChromatogram(List<IRawFile> rawfiles);
    
    
    
    
}
