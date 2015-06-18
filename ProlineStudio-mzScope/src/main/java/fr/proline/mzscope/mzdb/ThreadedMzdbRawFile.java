package fr.proline.mzscope.mzdb;

import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.ScanHeader;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.ExtractionParams;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.util.ScanUtils;
import java.io.File;
import java.util.List;
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
   
   private ScanHeader[] ms2ScanHeaders = null;
   
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
   
   private void sortScanHeader(ScanHeader[] scans) {
       ms2ScanHeaders = ScanUtils.sortScanHeader(scans);
   }

    @Override
    public String getName() {
        return file.getName();
    }
   
   
    @Override
   public String toString(){
       return file.getName();
   }
   
   @Override
   public int getPreviousScanId(final int scanIndex, final int msLevel) {
      try {
         return service.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
               return mzdbRawFile.getPreviousScanId(scanIndex, msLevel);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getPreviousScanId call fail", ex);
      } 
      return -1;
   }

   @Override
   public int getNextScanId(final int scanIndex, final int msLevel) {
      try {
         return service.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
               return mzdbRawFile.getNextScanId(scanIndex, msLevel);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getNextScanId call fail", ex);
      } 
      return -1;
   }

   @Override
   public int getScanId(final double retentionTime) {
      try {
         return service.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
               return mzdbRawFile.getScanId(retentionTime);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
          logger.error("getScanId call fail", ex);
      } 
      return -1;
   }

   @Override
   public Scan getScan(final int scanIndex) {
      try {
         return service.submit(new Callable<Scan>() {
            @Override
            public Scan call() {
               return mzdbRawFile.getScan(scanIndex);
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getScan call fail", ex);
      } 
      return null;
   }

   @Override
   public List<Feature> extractFeatures(final ExtractionType type, final ExtractionParams params) {
     try {
         logger.info("extract feature starting");
         Future<List<Feature>> future = service.submit(new Callable<List<Feature>>() {
            @Override
            public List<Feature> call() {
               return mzdbRawFile.extractFeatures(type, params);
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
   public Chromatogram getXIC(final double min, final double max) {
      try {
         return service.submit(new Callable<Chromatogram>() {
            @Override
            public Chromatogram call() {
               Chromatogram chromatogram = mzdbRawFile.getXIC(min, max);
               chromatogram.rawFile = ThreadedMzdbRawFile.this;
               return chromatogram;
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getXIC call fail", ex);
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
               chromatogram.rawFile = ThreadedMzdbRawFile.this;
               return chromatogram;
            }
         }).get();
      } catch (InterruptedException | ExecutionException ex ) {
         logger.error("getBPI call fail", ex);
      } 
      return null;
   }
   
   @Override
    public Chromatogram getXIC(final double minMz,final  double maxMz, final float minRT,final  float maxRT) {
      try {
         return service.submit(new Callable<Chromatogram>() {
            @Override
            public Chromatogram call() {
               Chromatogram chromatogram = mzdbRawFile.getXIC(minMz, maxMz, minRT, maxRT);
               chromatogram.rawFile = ThreadedMzdbRawFile.this;
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
                chromatogram.rawFile = ThreadedMzdbRawFile.this;
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

}
