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
