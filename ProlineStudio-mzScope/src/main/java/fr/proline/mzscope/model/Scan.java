package fr.proline.mzscope.model;

public class Scan {
   
   public static enum ScanType { CENTROID, PROFILE }
   private String title;
   private Integer index;
   private int msLevel;
   private ScanType dataType; 
   private float retentionTime;
   private double[] masses;
   private float[] intensities;
   private double[] peaksMz = null;
   private float[] peaksIntensities = null;
   
   // msLevel2 only
   private Double precursorMz;
   private Integer precursorCharge;
   
   public Scan(Integer index, float rt, double[] masses, float[] intensities, int msLevel) {
      this(index, rt, masses, intensities, msLevel, (msLevel == 2) ? ScanType.CENTROID : ScanType.PROFILE);
   }
   
     public Scan(Integer index, float rt, double[] masses, float[] intensities, int msLevel, ScanType type) {
      this.index = index;
      this.masses = masses;
      this.intensities = intensities;
      this.retentionTime = rt;
      this.msLevel = msLevel;
      this.dataType = type;
   }

   public double[] getMasses() {
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

   /**
    * @return the peaksMz
    */
   public double[] getPeaksMz() {
      return peaksMz;
   }

   /**
    * @param peaksMz the peaksMz to set
    */
   public void setPeaksMz(double[] peaksMz) {
      this.peaksMz = peaksMz;
   }

   /**
    * @return the peaksIntensities
    */
   public float[] getPeaksIntensities() {
      return peaksIntensities;
   }

   /**
    * @param peaksIntensities the peaksIntensities to set
    */
   public void setPeaksIntensities(float[] peaksIntensities) {
      this.peaksIntensities = peaksIntensities;
   }
   
   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public int getMsLevel() {
      return msLevel;
   }
   
   /**
    * Returns the data type : CENTROID or PROFILE
    * 
    * @return 
    */
   public ScanType getDataType() {
      return dataType;
   }

    public Double getPrecursorMz() {
        return precursorMz;
    }

    public void setPrecursorMz(Double precursorMz) {
        this.precursorMz = precursorMz;
    }

    public Integer getPrecursorCharge() {
        return precursorCharge;
    }

    public void setPrecursorCharge(Integer precursorCharge) {
        this.precursorCharge = precursorCharge;
    }


}
