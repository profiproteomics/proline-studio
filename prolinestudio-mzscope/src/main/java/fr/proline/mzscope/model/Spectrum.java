/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.model;

import fr.profi.mzdb.model.SpectrumData;

public class Spectrum {

   public enum ScanType {

      CENTROID, PROFILE
   }
   private String title;
   private Integer index;
   private int msLevel;
   private ScanType dataType;
   private float retentionTime;
   private double[] masses;
   private float[] intensities;
   private SpectrumData spectrumData;

   // msLevel2 only
   private Double precursorMz;
   private Integer precursorCharge;

   public Spectrum(Integer index, float rt, double[] masses, float[] intensities, int msLevel) {
      this(index, rt, masses, intensities, msLevel, (msLevel == 2) ? ScanType.CENTROID : ScanType.PROFILE);
   }

   public Spectrum(Integer index, float rt, double[] masses, float[] intensities, int msLevel, ScanType type) {
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

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public int getMsLevel() {
      return msLevel;
   }

   public SpectrumData getSpectrumData() {
      return spectrumData;
   }

   public void setSpectrumData(SpectrumData spectrumData) {
      this.spectrumData = spectrumData;
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
