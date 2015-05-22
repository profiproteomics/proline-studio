/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import fr.profi.mzdb.model.Feature;
import java.util.List;

/**
 *
 * @author CB205360
 */
public interface IRawFile {

   public enum ExtractionType { EXTRACT_MS2_FEATURES, DETECT_PEAKELS, DETECT_FEATURES};
   
   public String getName();

   public Chromatogram getXIC(double minMz, double maxMz, float minRT, float maxRT);
      
   public Chromatogram getXIC(double min, double max);
   
   public Chromatogram getTIC();

   public Chromatogram getBPI();

   public List<Feature> extractFeatures(ExtractionType type, ExtractionParams params);
   
   public Scan getScan(int scanIndex);
   
   public int getScanId(double retentionTime);
   
   public int getNextScanId(int scanIndex, int msLevel);
   
   public int getPreviousScanId(int scanIndex, int msLevel);
   
   /**
    * return the list of MS/MS events times (sec) for the specified mass range
    * @param minMz
    * @param maxMz
    * @return 
    */
   public List<Float> getMsMsEvent(double minMz, double maxMz);
   
}
