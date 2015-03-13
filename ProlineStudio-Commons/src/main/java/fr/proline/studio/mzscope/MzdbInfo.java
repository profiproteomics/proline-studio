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

    public MzdbInfo() {
    }

    public MzdbInfo(String fileName, double moz) {
        this.fileName = fileName;
        this.moz = moz;
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
    
    
}
