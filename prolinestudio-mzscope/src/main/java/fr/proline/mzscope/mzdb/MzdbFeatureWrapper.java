/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.mzdb;

import fr.proline.mzscope.model.IFeature;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peakel;
import fr.proline.mzscope.model.IRawFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptor that convert fr.profi.mzdb.model.Feature object to IFeature implementation
 *
 * @author CB205360
 */
public class MzdbFeatureWrapper implements IFeature {

    final private static Logger logger = LoggerFactory.getLogger(MzdbFeatureWrapper.class);
    
    private final Feature mzdbFeature;
    //TODO set to final as soon as setRawFile will remove
    private IRawFile rawFile;
    private final int msLevel;
    
   public MzdbFeatureWrapper(Feature mzdbFeature, IRawFile rawfile, int msLevel) {
      this.mzdbFeature = mzdbFeature;
      this.rawFile = rawfile;
      this.msLevel = msLevel;
   }

   @Override
   public float getArea() {
      return mzdbFeature.getArea();
   }

   @Override
   public int getScanCount() {
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Peakel[] getPeakels() {
        return mzdbFeature.getPeakels();
    }
}
