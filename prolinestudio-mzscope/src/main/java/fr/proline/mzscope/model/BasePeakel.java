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

import fr.profi.mzdb.model.Peakel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of IPeakel interface. Allows to create IPeakel "by hands"
 *
 * @author CB205360
 */
public class BasePeakel implements IPeakel {

   final private static Logger logger = LoggerFactory.getLogger(BasePeakel.class);

   private float area = 0.0f;
   private float apexIntensity = 0.0f;
   private float elutionTime;
   private float firstElutionTime;
   private float lastElutionTime;
   private int scanCount = 0;
   private double mz;

   private double parentMz;
   private IRawFile rawFile;
   private int msLevel;
   
   public BasePeakel(double mz, float elutionTime, float firstElutionTime, float lastElutionTime, IRawFile rawfile, int msLevel) {
      this.elutionTime = elutionTime;
      this.firstElutionTime = firstElutionTime;
      this.lastElutionTime = lastElutionTime;
      this.mz = mz;
      this.rawFile = rawfile;
      this.msLevel = msLevel;
   }

   public float getArea() {
      return area;
   }

   public void setArea(float area) {
      this.area = area;
   }

   @Override
   public float getApexIntensity() {
      return apexIntensity;
   }

   public void setApexIntensity(float apexIntensity) {
      this.apexIntensity = apexIntensity;
   }

   @Override
   public float getElutionTime() {
      return elutionTime;
   }

   public void setElutionTime(float elutionTime) {
      this.elutionTime = elutionTime;
   }

   @Override
   public float getFirstElutionTime() {
      return firstElutionTime;
   }

   public void setFirstElutionTime(float firstElutionTime) {
      this.firstElutionTime = firstElutionTime;
   }

   @Override
   public float getLastElutionTime() {
      return lastElutionTime;
   }

   public void setLastElutionTime(float lastElutionTime) {
      this.lastElutionTime = lastElutionTime;
   }

   @Override
   public int getScanCount() {
      return scanCount;
   }

   public void setScanCount(int scanCount) {
      this.scanCount = scanCount;
   }

   @Override
   public double getMz() {
      return mz;
   }

   public void setMz(double mz) {
      this.mz = mz;
   }

   @Override
   public float getDuration() {
      return (lastElutionTime - firstElutionTime);
   }

    @Override
    public IRawFile getRawFile() {
        return rawFile;
    }

    @Override
    public void setRawFile(IRawFile rawfile) {
        this.rawFile = rawfile;
    }

    
    @Override
    public int getMsLevel() {
        return msLevel;
    }

    @Override
    public double getParentMz() {
        return parentMz;
    }

   public void setParentMz(double parentMz) {
      this.parentMz = parentMz;
   }

   @Override
   public Peakel getPeakel() {
      return null;
   }
}
