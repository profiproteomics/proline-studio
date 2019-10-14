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

import java.util.Arrays;

/**
 *
 * @author CB205360
 */
public class Chromatogram implements IChromatogram {

   private String rawFilename;
   private String title;
   private double minMz = -1.0;
   private double maxMz = -1.0;
   private double[] time;
   private double[] intensities;
   private double elutionStartTime = Double.NaN;
   private double elutionEndTime = Double.NaN;
   private double m_maxIntensity = Double.NaN;


   public Chromatogram(String rawFilename, String title, double[] time, double[] intensities) {
      this.rawFilename = rawFilename;
      this.title = title;
      this.time = time;
      this.intensities = intensities;
   }

   public Chromatogram(String rawFilename, String title, double[] time, double[] intensities, double start, double end) {
      this(rawFilename, title, time, intensities);
      this.elutionStartTime = start;
      this.elutionEndTime = end;
   }
   
   @Override
   public double getMaxIntensity() {
       if (Double.isNaN(m_maxIntensity)) {
           m_maxIntensity = Arrays.stream(getIntensities()).max().orElse(Double.NaN);
       }
       return m_maxIntensity;
   }

   @Override
   public String getRawFilename() {
      return rawFilename;
   }

   @Override
   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   @Override
   public double getMinMz() {
      return minMz;
   }

   @Override
   public double getMaxMz() {
      return maxMz;
   }

   @Override
   public double[] getTime() {
      return time;
   }

   @Override
   public double[] getIntensities() {
      return intensities;
   }

   @Override
   public double getElutionStartTime() {
      if (Double.isNaN(elutionStartTime)) {
         return time[0];
      }
      return elutionStartTime;
   }

   @Override
   public double getElutionEndTime() {
      if (Double.isNaN(elutionEndTime)) {
         return time[time.length - 1];
      }
      return elutionEndTime;
   }

   public void setMinMz(double minMz) {
      this.minMz = minMz;
   }

   public void setMaxMz(double maxMz) {
      this.maxMz = maxMz;
   }
}
