/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class BaseFeature implements IFeature {

   final private static Logger logger = LoggerFactory.getLogger(BaseFeature.class);

   private float area = 0.0f;
   private float apexIntensity = 0.0f;
   private int charge = 0;
   private float elutionTime;
   private float firstElutionTime;
   private float lastElutionTime;
   private int ms1Count = 0;
   private double mz;
   private int peakelsCount = 0;

   public BaseFeature(double mz, float elutionTime, float firstElutionTime, float lastElutionTime) {
      this.elutionTime = elutionTime;
      this.firstElutionTime = firstElutionTime;
      this.lastElutionTime = lastElutionTime;
      this.mz = mz;
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
   public int getCharge() {
      return charge;
   }

   public void setCharge(int charge) {
      this.charge = charge;
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
   public int getMs1Count() {
      return ms1Count;
   }

   public void setMs1Count(int ms1Count) {
      this.ms1Count = ms1Count;
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
   public int getPeakelsCount() {
      return peakelsCount;
   }

}
