package fr.proline.mzscope.mzml;

public class Scan {

   private Integer index;
   private float[] masses;
   private float[] intensities;
   private float retentionTime;
   
   public Scan(Integer index, float rt, float[] masses, float[] intensities) {
      this.index = index;
      this.masses = masses;
      this.intensities = intensities;
      this.retentionTime = rt;
   }

   public float[] getMasses() {
      return this.masses;
   }

   public float[] getIntensities() {
      return this.intensities;
   }

   public Integer getIndex() {
      return index;
   }

   public float getRetentionTime() {
      return retentionTime;
   }

}
