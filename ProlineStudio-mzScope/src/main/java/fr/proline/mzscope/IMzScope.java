package fr.proline.mzscope;

import java.io.File;
import java.util.List;

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
     * open the specified file
     * @param file 
     */
    public abstract void openRaw(File file);
    
    
    /**
     * open multi files
     * @param files 
     */
    public abstract void openRaw(List<File> files);
    
    /**
     * launch the detectPeeakels dialog for 1 file
     * @param file 
     */
    public abstract void detectPeakels(File file);
    
    /**
     * launch the detectPeakels dialog for all selected files
     * @param fileList 
     */
    public abstract void detectPeakels(List<File> fileList);
    
}
