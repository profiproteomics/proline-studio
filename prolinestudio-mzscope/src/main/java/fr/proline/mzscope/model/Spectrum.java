/* 
 * Copyright (C) 2019
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
   protected String title;
   protected Integer index;
   protected int msLevel;
   protected ScanType dataType;
   protected float retentionTime;
   protected SpectrumData spectrumData;
   // msLevel2 only
   protected Double precursorMz;
   protected Integer precursorCharge;


   public Spectrum(Integer index, float rt, double[] masses, float[] intensities, int msLevel) {
      this(index, rt, masses, intensities, msLevel, (msLevel == 2) ? ScanType.CENTROID : ScanType.PROFILE);
   }

   public Spectrum(Integer index, float rt, double[] masses, float[] intensities, int msLevel, ScanType type) {
      this(index, rt, new SpectrumData(masses, intensities), msLevel, type);
   }

   public Spectrum(Integer index, float rt, SpectrumData spectrumData, int msLevel, ScanType type) {
      this.index = index;
      this.retentionTime = rt;
      this.msLevel = msLevel;
      this.dataType = type;
      this.spectrumData = spectrumData;
   }

   public double[] getMasses() {
      return spectrumData.getMzList();
   }

   public float[] getIntensities() {
      return spectrumData.getIntensityList();
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

   public boolean hasIonMobilitySeparation() {
      return spectrumData.getMobilityIndexList() != null && spectrumData.getMobilityIndexList().length > 0;
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
