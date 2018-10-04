package fr.proline.mzscope.model;

import java.util.Arrays;

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
   public double elutionStartTime;
   public double elutionEndTime;
   private double m_maxIntensity = Double.NaN;
   
   public Chromatogram(String rawFilename) {
      this.rawFilename = rawFilename;
   }
   
   public Chromatogram(String rawFilename, String title) {
      this(rawFilename);
      this.title = title;
   }
   
   public double getMaxIntensity() {
       if (Double.isNaN(m_maxIntensity)) {
           m_maxIntensity = Arrays.stream(intensities).max().orElse(Double.NaN);
       }
       return m_maxIntensity;
   }
}
