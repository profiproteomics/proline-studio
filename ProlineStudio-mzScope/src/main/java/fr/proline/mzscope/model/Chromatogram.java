package fr.proline.mzscope.model;

/**
 *
 * @author CB205360
 */
public class Chromatogram {
   
   public String rawFilename;
   public String title;
   public double minMz = -1.0;
   public double maxMz = -1.0;
   public double[] time;
   public double[] intensities;
   
   public Chromatogram(String rawFilename) {
      this.rawFilename = rawFilename;
   }
   
   public Chromatogram(String rawFilename, String title) {
      this(rawFilename);
      this.title = title;
   }
}
