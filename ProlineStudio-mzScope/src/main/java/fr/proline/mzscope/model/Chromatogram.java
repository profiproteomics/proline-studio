package fr.proline.mzscope.model;

/**
 *
 * @author CB205360
 */
public class Chromatogram {
   
   public IRawFile rawFile;
   public String title;
   public double minMz = -1.0;
   public double maxMz = -1.0;
   public double[] time;
   public double[] intensities;
   
}
