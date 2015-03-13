/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.mzscope;

/**
 * represents the info to  pass to mzscope
 * @author MB243701
 */
public class MzdbInfo {
    
    // mzdb filename 
    private String fileName;
    
    // m/z
    private double moz;
    
    // elution time
    private double elutionTime;
    
    //first scan
    private double firstScan;
    
    //last Scan
    private double lastScan;

    public MzdbInfo() {
    }

    public MzdbInfo(String fileName, double moz, double elutionTime, double firstScan, double lastScan) {
        this.fileName = fileName;
        this.moz = moz;
        this.elutionTime = elutionTime;
        this.firstScan = firstScan;
        this.lastScan = lastScan;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getMoz() {
        return moz;
    }

    public void setMoz(double moz) {
        this.moz = moz;
    }

    public double getElutionTime() {
        return elutionTime;
    }

    public void setElutionTime(double elutionTime) {
        this.elutionTime = elutionTime;
    }

    public double getFirstScan() {
        return firstScan;
    }

    public void setFirstScan(double firstScan) {
        this.firstScan = firstScan;
    }

    public double getLastScan() {
        return lastScan;
    }

    public void setLastScan(double lastScan) {
        this.lastScan = lastScan;
    }
    
    
}
