package fr.proline.studio.mzscope;

import java.io.File;
import java.util.List;

/**
 * represents the info to  pass to mzscope
 * @author MB243701
 */
public class MzdbInfo {
    
    public final static int MZSCOPE_DETECT_PEAKEL = 0;
    public final static int MZSCOPE_VIEW = 1;
    public final static int MZSCOPE_EXTRACT = 2;
    
    // type of action to pass to mzscope
    private int action;
    
    // mzdb file 
    private File mzdbFile;
    
    //
    private String fileName;
    
    //could be multi files
    private List<File> mzdbFiles;
    
    // m/z
    private Double moz;
    
    // elution time
    private Double elutionTime;
    
    //first scan
    private Double firstScan;
    
    //last Scan
    private Double lastScan;


    public MzdbInfo(String fileName, Double moz, Double elutionTime, Double firstScan, Double lastScan) {
        this.action = MZSCOPE_EXTRACT;
        this.mzdbFile = null;
        this.mzdbFiles = null;
        this.fileName = fileName;
        this.moz = moz;
        this.elutionTime = elutionTime;
        this.firstScan = firstScan;
        this.lastScan = lastScan;
    }
    
    public MzdbInfo(int action, File file) {
        this.action = action;
        this.mzdbFile = file;
        this.mzdbFiles = null;
        this.moz = null;
        this.elutionTime = null;
        this.firstScan = null;
        this.lastScan = null;
    }
    
    public MzdbInfo(int action, List<File> files) {
        this.action = action;
        this.mzdbFile = null;
        this.mzdbFiles = files;
        this.moz = null;
        this.elutionTime = null;
        this.firstScan = null;
        this.lastScan = null;
    }

    public boolean isMultiFile(){
        return this.mzdbFiles != null && this.mzdbFile == null;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFile(File file){
        this.mzdbFile = file;
    }

    public File getMzdbFile() {
        return mzdbFile;
    }

    public void setMzdbFile(File mzdbFile) {
        this.mzdbFile = mzdbFile;
    }

    public List<File> getMzdbFiles() {
        return mzdbFiles;
    }

    public void setMzdbFiles(List<File> mzdbFiles) {
        this.mzdbFiles = mzdbFiles;
    }


    public Double getMoz() {
        return moz;
    }

    public void setMoz(Double moz) {
        this.moz = moz;
    }

    public Double getElutionTime() {
        return elutionTime;
    }

    public void setElutionTime(Double elutionTime) {
        this.elutionTime = elutionTime;
    }

    public Double getFirstScan() {
        return firstScan;
    }

    public void setFirstScan(Double firstScan) {
        this.firstScan = firstScan;
    }

    public Double getLastScan() {
        return lastScan;
    }

    public void setLastScan(Double lastScan) {
        this.lastScan = lastScan;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
    
    
}
