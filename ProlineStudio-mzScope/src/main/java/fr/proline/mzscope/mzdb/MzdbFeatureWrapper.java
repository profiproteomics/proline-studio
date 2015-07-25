/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.mzdb;

import fr.proline.mzscope.model.IFeature;
import fr.profi.mzdb.model.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MzdbFeatureWrapper implements IFeature {

    final private static Logger logger = LoggerFactory.getLogger(MzdbFeatureWrapper.class);
    
    private final Feature mzdbFeature;

   public MzdbFeatureWrapper(Feature mzdbFeature) {
      this.mzdbFeature = mzdbFeature;
   }

   @Override
   public float getArea() {
      return mzdbFeature.getArea();
   }

   @Override
   public int getMs1Count() {
      return mzdbFeature.getMs1Count();
   }

   @Override
   public int getPeakelsCount() {
      return mzdbFeature.getPeakelsCount();
   }

   @Override
   public double getMz() {
      return mzdbFeature.getMz();
   }

   @Override
   public int getCharge() {
      return mzdbFeature.getCharge();
   }

   @Override
   public float getElutionTime() {
      return mzdbFeature.getElutionTime();
   }
   
   @Override
   public float getDuration() {
      return mzdbFeature.getBasePeakel().calcDuration();
   }
    
   @Override
   public float getApexIntensity() {
      return mzdbFeature.getBasePeakel().getApexIntensity();
   }
   
   @Override
   public float getFirstElutionTime() {
      return mzdbFeature.getBasePeakel().getFirstElutionTime();
   }

   @Override
   public float getLastElutionTime() {
      return mzdbFeature.getBasePeakel().getLastElutionTime();
   }
   
}
