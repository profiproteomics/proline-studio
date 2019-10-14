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
package fr.proline.mzscope.mzdb;

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.model.IChromatogram;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class ThreadedMzdbRawFile implements IRawFile {

   private static final Logger logger = LoggerFactory.getLogger(ThreadedMzdbRawFile.class);
   
   private final ExecutorService service;
   private final File file;
   private MzdbRawFile mzdbRawFile;
      
   public ThreadedMzdbRawFile(File file) {
      this.file = file;
      this.service = Executors.newSingleThreadExecutor();
      init();
   }

   private void init() {
      try {
         Future future = service.submit(() -> {
            mzdbRawFile = new MzdbRawFile(file);
         });
         future.get();
      } catch (InterruptedException | ExecutionException ex) {
         logger.error("mzdbRawFile initialisation failed", ex);
      } 
   }
   
    @Override
    public String getName() {
        return file.getName();
    }
    
    @Override
    public File getFile(){
       return file;
    }
   
   
    @Override
   public String toString(){
       return file.getName();
   }
   
   @Override
   public int getPreviousSpectrumId(final int spectrumIndex, final int msLevel) {
      try {
         return service.submit(() -> mzdbRawFile.getPreviousSpectrumId(spectrumIndex, msLevel)).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getPreviousSpectrumId call fail", ex);
      } 
      return -1;
   }

   @Override
   public int getSpectrumCount() {
      try {
         return service.submit(() -> mzdbRawFile.getSpectrumCount()).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getPreviousSpectrumId call fail", ex);
      } 
      return -1;
   }
   
   @Override
   public int getNextSpectrumId(final int spectrumIndex, final int msLevel) {
      try {
         return service.submit(() -> mzdbRawFile.getNextSpectrumId(spectrumIndex, msLevel)).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getNextSpectrumId call fail", ex);
      } 
      return -1;
   }

   @Override
   public int getSpectrumId(final double retentionTime) {
      try {
         return service.submit(() -> mzdbRawFile.getSpectrumId(retentionTime)).get();
      } catch (InterruptedException | ExecutionException ex ) {
          logger.error("getSpectrumId call fail", ex);
      } 
      return -1;
   }

  @Override
  public double[] getElutionTimes(int msLevel) {
    try {
      return service.submit(() -> mzdbRawFile.getElutionTimes(msLevel)).get();
    } catch (InterruptedException | ExecutionException ex ) {
      logger.error("getElutionTimes call fail", ex);
    }
    return null;
  }

  @Override
  public double getSpectrumElutionTime(int spectrumIndex) {
    try {
      return service.submit(() -> mzdbRawFile.getSpectrumElutionTime(spectrumIndex)).get();
    } catch (InterruptedException | ExecutionException ex ) {
      logger.error("getSpectrumElutionTime call fail", ex);
    }
    return -1.0;
  }

  @Override
   public Spectrum getSpectrum(final int spectrumIndex) {
      try {
         return service.submit(() -> mzdbRawFile.getSpectrum(spectrumIndex)).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getSpectrum call fail", ex);
      } 
      return null;
   }

   @Override
   public List<IFeature> extractFeatures(final FeaturesExtractionRequest params) {
     try {
         logger.info("extract feature starting");
         Future<List<IFeature>> future = service.submit(() -> {
            List<IFeature> result = mzdbRawFile.extractFeatures(params);
            result.stream().forEach( f -> f.setRawFile(ThreadedMzdbRawFile.this));
            return result;
         });
         logger.info("waiting for feature extraction ... ");
         return future.get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("extractFeatures call fail", ex);
      } 
      return null;
   }
   
   @Override
   public IChromatogram getBPI() {
      try {
         return service.submit(() -> {
            IChromatogram chromatogram = mzdbRawFile.getBPI();
            return chromatogram;
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getBPI call fail", ex);
      } 
      return null;
   }
   
   @Override
    public IChromatogram getXIC(final MsnExtractionRequest params) {
      try {
         return service.submit(() -> {
            IChromatogram chromatogram = mzdbRawFile.getXIC(params);
            return chromatogram;
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getXIC call fail", ex);
      } 
      return null;
   }
   
   @Override
    public IChromatogram getTIC() {
      try {
         return service.submit(() -> {
            IChromatogram chromatogram =  mzdbRawFile.getTIC();
            return chromatogram;
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getTIC call fail", ex);
      } 
      return null;
   }

    @Override
    public List<Float> getMsMsEvent(double minMz, double maxMz) {
        return mzdbRawFile.getMsMsEvent(minMz, maxMz);
    }
    
    @Override
    public boolean exportRawFile(String mgfFileName, IExportParameters exportParams ){
        try {
         return service.submit(() -> mzdbRawFile.exportRawFile(mgfFileName, exportParams)).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("export  call fail", ex);
      } 
      return false;
    }
    
    @Override
    public boolean isDIAFile(){
        return mzdbRawFile.isDIAFile();
    }

    @Override
    public Map<String, Object> getFileProperties() {
              try {
         return service.submit(() -> {
            Map<String, Object> data =  mzdbRawFile.getFileProperties();
            return data;
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getFileMetaData call fail", ex);
      } 
      return null;
    }

    @Override
    public QCMetrics getFileMetrics() {
      try {
         return service.submit(() -> {
            QCMetrics metrics = mzdbRawFile.getFileMetrics();
            metrics.setRawFile(ThreadedMzdbRawFile.this);
            return metrics;
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getFileMetaData call fail", ex);
      } 
      return null;
    }
    
}
