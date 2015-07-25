package fr.proline.mzscope.model;

import fr.profi.mzdb.model.Feature;
import java.util.List;

/**
 *
 * @author CB205360
 */
public interface IRawFile {
   
   public String getName();

   public Chromatogram getXIC(Ms1ExtractionRequest params);
   
   public Chromatogram getTIC();

   public Chromatogram getBPI();

   public List<Feature> extractFeatures(FeaturesExtractionRequest params);
   
   public Scan getScan(int scanIndex);

   public int getScanCount();
   
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
