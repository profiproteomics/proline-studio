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

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.model.IChromatogram;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MzMLRawFile implements IRawFile {

   private static Logger logger = LoggerFactory.getLogger(MzMLRawFile.class);
   private File mzMLFile;
   private List<Scan> scans;

   public MzMLRawFile(File file) {
      mzMLFile = file;
      init();
   }

   private void init() {
      try {
         long start = System.currentTimeMillis();
         logger.info("Start reading mzMLRawFile "+mzMLFile.getAbsolutePath());
         scans = mzMLReader.read(mzMLFile.getAbsolutePath());
         logger.info("File mzML read in :: " + (System.currentTimeMillis() - start) + " ms");
      } catch (Exception e) {
         logger.error("cannot read file " + mzMLFile.getAbsolutePath(), e);
      }
   }

    @Override
    public String getName() {
        return mzMLFile.getName();
    }
    
    @Override
    public File getFile(){
       return mzMLFile;
    }

   
    public IChromatogram getTIC() {
      throw new UnsupportedOperationException("Not supported yet.");      
   }
    
   @Override
   public IChromatogram getBPI() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
    
   @Override
   public IChromatogram getXIC(MsnExtractionRequest params) {
      IChromatogram chromatogram  = XICExtractor.extract(scans, (float)params.getMinMz(), (float)params.getMaxMz());      
      return chromatogram;      
   }
  
   @Override
   public Spectrum getSpectrum(int spectrumIndex) {
      return toModelSpectrum(scans.get(spectrumIndex));
   }

   @Override
   public int getSpectrumId(double retentionTime) {
      for (Scan s : scans) {
         if (Math.abs(s.getRetentionTime() - retentionTime) < 0.001)
            return s.getIndex();
      }
      return 0;
   }

  @Override
  public double[] getElutionTimes(int msLevel) {
    return scans.stream().mapToDouble(s -> s.getRetentionTime()/60.0).toArray();
  }


  @Override
  public double getSpectrumElutionTime(int spectrumIndex) {
    return scans.get(spectrumIndex).getRetentionTime();
  }

  @Override
   public int getNextSpectrumId(int spectrumIndex, int msLevel) {
      return (int)Math.min(spectrumIndex+1, scans.size());
   }

   @Override
   public int getPreviousSpectrumId(int spectrumIndex, int msLevel) {
      return (int)Math.max(spectrumIndex-1, 0);
   }

   private fr.proline.mzscope.model.Spectrum toModelSpectrum(Scan mzmlScan) {
      
      double[] masses = new double[mzmlScan.getMasses().length];
      for (int k = 0; k < masses.length; k++) {
         masses[k] = (double)mzmlScan.getMasses()[k];
      }
      return new fr.proline.mzscope.model.Spectrum(mzmlScan.getIndex(), mzmlScan.getRetentionTime(), masses, mzmlScan.getIntensities(), 1);
   }

   @Override
   public List<IFeature> extractFeatures(FeaturesExtractionRequest params) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

    @Override
    public List<Float> getMsMsEvent(double minMz, double maxMz) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   @Override
   public int getSpectrumCount() {
      return scans.size();
   }
   
   @Override
    public boolean exportRawFile(String mgfFileName, IExportParameters exportParams ){
       return true;
    }
    
    @Override
    public boolean isDIAFile(){
        return false;
    }

    @Override
    public Map<String, Object> getFileProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QCMetrics getFileMetrics() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
