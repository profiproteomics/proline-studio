/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.mzdb;

import fr.proline.mzscope.model.IFeature;
import fr.profi.mzdb.model.Peakel;
import fr.proline.mzscope.model.IRawFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MzdbPeakelWrapper implements IFeature {

    final private static Logger logger = LoggerFactory.getLogger(MzdbPeakelWrapper.class);
    
    private final Peakel peakel;
    //TODO set to final as soon as setRawFile will remove
    private IRawFile rawFile;
    private final int msLevel;
    private double parentMz;
    
    public MzdbPeakelWrapper(Peakel mzdbPeakel, IRawFile rawfile) {
      this(mzdbPeakel,rawfile,1);
   }
    
    public MzdbPeakelWrapper(Peakel mzdbPeakel, IRawFile rawfile, double parentMz) {
      this(mzdbPeakel,rawfile,2);
      this.parentMz = parentMz;
   }
    
   public MzdbPeakelWrapper(Peakel mzdbPeakel, IRawFile rawfile, int msLevel) {
      this.peakel = mzdbPeakel;
      this.rawFile = rawfile;
      this.msLevel = msLevel;
   }

   @Override
   public float getArea() {
      return peakel.getArea();
   }

   @Override
   public int getScanCount() {
      return peakel.getSpectrumIds().length;
   }

   @Override
   public int getPeakelsCount() {
      return 1;
   }

   @Override
   public double getMz() {
      return peakel.getMz();
   }

   @Override
   public int getCharge() {
      return 0;
   }

   @Override
   public float getElutionTime() {
      return peakel.getElutionTime();
   }
   
   @Override
   public float getDuration() {
      return peakel.calcDuration();
   }
    
   @Override
   public float getApexIntensity() {
      return peakel.getApexIntensity();
   }
   
   @Override
   public float getFirstElutionTime() {
      return peakel.getFirstElutionTime();
   }

   @Override
   public float getLastElutionTime() {
      return peakel.getLastElutionTime();
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

    public double getParentMz() {
        return parentMz;
    }
   
    public Peakel[] getPeakels() {
        return new Peakel[] { peakel };
    }
}
