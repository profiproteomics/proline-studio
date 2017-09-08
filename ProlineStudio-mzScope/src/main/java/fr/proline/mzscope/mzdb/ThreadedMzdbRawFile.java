package fr.proline.mzscope.mzdb;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.model.IExportParameters;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.model.QCMetrics;
import fr.proline.mzscope.model.Spectrum;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
         Future future = service.submit(new Runnable() {
            @Override
            public void run() {
               mzdbRawFile = new MzdbRawFile(file);
            }
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
         return service.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
               return mzdbRawFile.getPreviousSpectrumId(spectrumIndex, msLevel);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getPreviousSpectrumId call fail", ex);
      } 
      return -1;
   }

   @Override
   public int getSpectrumCount() {
      try {
         return service.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
               return mzdbRawFile.getSpectrumCount();
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getPreviousSpectrumId call fail", ex);
      } 
      return -1;
   }
   
   @Override
   public int getNextSpectrumId(final int spectrumIndex, final int msLevel) {
      try {
         return service.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
               return mzdbRawFile.getNextSpectrumId(spectrumIndex, msLevel);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getNextSpectrumId call fail", ex);
      } 
      return -1;
   }

   @Override
   public int getSpectrumId(final double retentionTime) {
      try {
         return service.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
               return mzdbRawFile.getSpectrumId(retentionTime);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
          logger.error("getSpectrumId call fail", ex);
      } 
      return -1;
   }

   @Override
   public Spectrum getSpectrum(final int spectrumIndex) {
      try {
         return service.submit(new Callable<Spectrum>() {
            @Override
            public Spectrum call() {
               return mzdbRawFile.getSpectrum(spectrumIndex);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getSpectrum call fail", ex);
      } 
      return null;
   }

   @Override
   public List<IFeature> extractFeatures(final FeaturesExtractionRequest params) {
     try {
         logger.info("extract feature starting");
         Future<List<IFeature>> future = service.submit(new Callable<List<IFeature>>() {
            @Override
            public List<IFeature> call() {
               List<IFeature> result = mzdbRawFile.extractFeatures(params);
               result.stream().forEach( f -> f.setRawFile(ThreadedMzdbRawFile.this));
               return result;
            }
         });
         logger.info("waiting for feature extraction ... ");
         return future.get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("extractFeatures call fail", ex);
      } 
      return null;
   }
   
   @Override
   public Chromatogram getBPI() {
      try {
         return service.submit(new Callable<Chromatogram>() {
            @Override
            public Chromatogram call() {
               Chromatogram chromatogram = mzdbRawFile.getBPI();
               return chromatogram;
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getBPI call fail", ex);
      } 
      return null;
   }
   
   @Override
    public Chromatogram getXIC(final MsnExtractionRequest params) {
      try {
         return service.submit(new Callable<Chromatogram>() {
            @Override
            public Chromatogram call() {
               Chromatogram chromatogram = mzdbRawFile.getXIC(params);
               return chromatogram;
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getXIC call fail", ex);
      } 
      return null;
   }
   
   @Override
    public Chromatogram getTIC() {
      try {
         return service.submit(new Callable<Chromatogram>() {
            @Override
            public Chromatogram call() {
               Chromatogram chromatogram =  mzdbRawFile.getTIC();
               return chromatogram;
            }
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
         return service.submit(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() {
               Map<String, Object> data =  mzdbRawFile.getFileProperties();
               return data;
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getFileMetaData call fail", ex);
      } 
      return null;
    }

    @Override
    public QCMetrics getFileMetrics() {
      try {
         return service.submit(new Callable<QCMetrics>() {
            @Override
            public QCMetrics call() {
               return mzdbRawFile.getFileMetrics();
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getFileMetaData call fail", ex);
      } 
      return null;
    }
    
}
