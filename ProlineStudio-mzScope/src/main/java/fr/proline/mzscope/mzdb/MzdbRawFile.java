/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.mzdb;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import fr.profi.mzdb.FeatureDetectorConfig;
import fr.profi.mzdb.MzDbFeatureDetector;
import fr.profi.mzdb.MzDbFeatureExtractor;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.algo.feature.extraction.FeatureExtractorConfig;
import fr.profi.mzdb.io.reader.RunSliceDataProvider;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peak;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.PutativeFeature;
import fr.profi.mzdb.model.RunSlice;
import fr.profi.mzdb.model.ScanData;
import fr.profi.mzdb.model.ScanHeader;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.ExtractionParams;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.util.ScanUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.collection.JavaConverters;

/**
 *
 * @author CB205360
 */
public class MzdbRawFile implements IRawFile {

   private static final Logger logger = LoggerFactory.getLogger(MzdbRawFile.class);
   final private static DecimalFormat massFormatter = new DecimalFormat("0.####");
   final private static DecimalFormat timeFormatter = new DecimalFormat("0.00");

   private final File mzDbFile;
   private MzDbReader reader;
   private Map<Integer, ScanHeader> scanHeadersById = null;
   private ScanHeader[] ms2ScanHeaders = null;
   private double[] ms2ScanHeaderByMz = null;

   public MzdbRawFile(File file) {
      mzDbFile = file;
      init();
   }

   public MzDbReader getMzDbReader() {
      return reader;
   }

   private void init() {
      try {
         reader = new MzDbReader(mzDbFile, true);
      } catch (ClassNotFoundException | FileNotFoundException | SQLiteException e) {
         logger.error("cannot read file " + mzDbFile.getAbsolutePath(), e);
      }
   }
   
   private void sortScanHeader(ScanHeader[] scans) {
       ms2ScanHeaders = ScanUtils.sortScanHeader(scans);
       ms2ScanHeaderByMz = new double[ms2ScanHeaders.length];
       int i=0;
       for (ScanHeader scan : ms2ScanHeaders) {
           ms2ScanHeaderByMz[i] = scan.getPrecursorMz();
           i++;
       }
   }

   @Override
   public String getName() {
      return mzDbFile.getName();
   }

   private Map<Integer, ScanHeader> getScanHeadersById() {
      try {
         if (scanHeadersById == null) {
            long start = System.currentTimeMillis();
            scanHeadersById = reader.getScanHeaderById();
            logger.info("File mzDb headers red in :: " + (System.currentTimeMillis() - start) + " ms");
         }
      } catch (SQLiteException ex) {
         logger.error("cannot read file " + mzDbFile.getAbsolutePath(), ex);
      }
      return scanHeadersById;
   }

   @Override
   public String toString() {
      return mzDbFile.getName();
   }

   @Override
   public Chromatogram getTIC() {
      logger.info("mzdb extract TIC Chromatogram");
      Chromatogram chromatogram = null;
      try {
         ScanHeader[] headers = sortHeadersByTime(reader.getScanHeaders());
         double[] xAxisData = new double[headers.length];
         double[] yAxisData = new double[headers.length];
         for (int i = 0; i < headers.length; i++) {
            xAxisData[i] = (headers[i].getElutionTime() / 60.0);
            yAxisData[i] = ((double) headers[i].getTIC());
         }

         chromatogram = new Chromatogram();
         chromatogram.time = xAxisData;
         chromatogram.intensities = yAxisData;
         chromatogram.rawFile = this;
         return chromatogram;
      } catch (SQLiteException ex) {
         logger.error("Cannot generate TIC chromatogram", ex);
      }
      return chromatogram;
   }

   private ScanHeader[] sortHeadersByTime(ScanHeader[] headers) {
      Arrays.sort(headers, new Comparator<ScanHeader>() {

         @Override
         public int compare(ScanHeader sh1, ScanHeader sh2) {
            return sh1.getScanId() - sh2.getScanId();
         }
      
      });
      return headers;
   }
   @Override
   public Chromatogram getBPI() {
      logger.info("mzdb extract BPI Chromatogram");
      Chromatogram chromatogram = null;
      try {
         ScanHeader[] headers = sortHeadersByTime(reader.getScanHeaders());
         double[] xAxisData = new double[headers.length];
         double[] yAxisData = new double[headers.length];
         for (int i = 0; i < headers.length; i++) {
            xAxisData[i] = (headers[i].getElutionTime() / 60.0);
            yAxisData[i] = ((double) headers[i].getBasePeakIntensity());
         }

         chromatogram = new Chromatogram();
         chromatogram.time = xAxisData;
         chromatogram.intensities = yAxisData;
         chromatogram.rawFile = this;
         return chromatogram;
      } catch (SQLiteException ex) {
         logger.error("Cannot generate BPI chromatogram", ex);
      }
      return chromatogram;
   }

   @Override
   public Chromatogram getXIC(double minMz, double maxMz, float minRT, float maxRT) {
      Chromatogram chromatogram = null;
      try {
         logger.info("mzdb extract Chromato : {} - {} in time range {} - {}", minMz, maxMz, minRT, maxRT);
         Peak[] peaks = reader.getXIC(minMz, maxMz, minRT, maxRT, 1, MzDbReader.XicMethod.SUM);
         chromatogram = createChromatoFromPeak(peaks);
         chromatogram.minMz = minMz;
         chromatogram.maxMz = maxMz;
         StringBuilder builder = new StringBuilder("Mass range: ");
         builder.append(massFormatter.format(minMz)).append("-").append(massFormatter.format(maxMz));
         chromatogram.title = builder.toString();

      } catch (SQLiteException | StreamCorruptedException e) {
         logger.error("Error during chromatogram extraction", e);
      }
      return chromatogram;
   }

   @Override
   public Chromatogram getXIC(double min, double max) {
      Chromatogram chromatogram = null;
      try {
         logger.info("mzdb extract Chromato : {} - {}", min, max);
         Peak[] peaks = reader.getXIC(min, max, 1, MzDbReader.XicMethod.SUM);
         chromatogram = createChromatoFromPeak(peaks);
         chromatogram.minMz = min;
         chromatogram.maxMz = max;

         StringBuilder builder = new StringBuilder("Mass range: ");
         builder.append(massFormatter.format(min)).append("-").append(massFormatter.format(max));
         chromatogram.title = builder.toString();

      } catch (SQLiteException | StreamCorruptedException e) {
         logger.error("Error during chromatogram extraction", e);
      }
      return chromatogram;
   }

   private Chromatogram createChromatoFromPeak(Peak[] peaks) {
      Chromatogram chromatogram = null;
      List<Double> xAxisData = new ArrayList<Double>(peaks.length);
      List<Double> yAxisData = new ArrayList<Double>(peaks.length);
      int previousScanId = 1;
      for (Peak peak : peaks) {
         int scanId = peak.getLcContext().getScanId();
         if (previousScanId != getPreviousScanId(scanId, 1)) {
            // there is a gap between peaks, add 0 values after the previous peak and before this one
            xAxisData.add(getScanHeadersById().get(getNextScanId(previousScanId, 1)).getElutionTime() / 60.0);
            yAxisData.add(0.0);
            xAxisData.add(getScanHeadersById().get(getPreviousScanId(scanId, 1)).getElutionTime() / 60.0);
            yAxisData.add(0.0);
         }
         double rt = peak.getLcContext().getElutionTime() / 60.0;
         xAxisData.add(rt);
         yAxisData.add((double) peak.getIntensity());
         previousScanId = peak.getLcContext().getScanId();
      }

      chromatogram = new Chromatogram();
      chromatogram.time = Doubles.toArray(xAxisData);
      chromatogram.intensities = Doubles.toArray(yAxisData);

      return chromatogram;
   }

   @Override
   public List<Feature> extractFeatures(ExtractionType type, ExtractionParams params) {
      switch (type) {
         case EXTRACT_MS2_FEATURES:
            return extractFeatures(params.mzTolPPM);
         case DETECT_PEAKELS:
            return detectPeakels(params.mzTolPPM, params.minMz, params.maxMz);
      }
      return null;
   }

    private List<Feature> detectPeakels(float mzTolPPM, double minMz, double maxMz) {
        List<Feature> result = new ArrayList<>();
        // Instantiates a Run Slice Data provider
        int msLevel = 1;
        FeatureDetectorConfig detectorConfig = new FeatureDetectorConfig(msLevel, mzTolPPM, 5);
        MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
        try {
            Iterator<RunSlice> runSlices;
            if (minMz == 0 && maxMz == 0){
                runSlices = getMzDbReader().getLcMsRunSliceIterator();
            }else {
                runSlices = getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
            }
            Peakel[] peakels = detector.detectPeakels(runSlices);
            for (Peakel peakel : peakels) {
                ArrayList<Peakel> l = new ArrayList<>();
                l.add(peakel);
                Peakel[] a = {peakel};
                //creates a Feature associated to this peakel
                Feature feature = new Feature(peakel.getMz(), 0, JavaConverters.asScalaBufferConverter(l).asScala(), false);
                result.add(feature);
            }
        } catch (SQLiteException | StreamCorruptedException ex) {
            logger.error("Error while getting LcMs RunSlice Iterator: "+ ex);
        }

        return result;
    }

   private List<Feature> extractFeatures(float tolPPM) {
      List<Feature> result = null;
      try {
         logger.info("retrieve scan headers...");
         ScanHeader[] ms2ScanHeaders = reader.getMs2ScanHeaders();
         
         List<PutativeFeature> pfs = new ArrayList<PutativeFeature>();
         logger.info("building putative features list from MS2 scan events...");
         for (ScanHeader scanH : ms2ScanHeaders) {
            pfs.add(new PutativeFeature(
                    PutativeFeature.generateNewId(),
                    scanH.getPrecursorMz(),
                    scanH.getPrecursorCharge(),
                    scanH.getId(),
                    2
            ));

         }
         result = extractFeatures(pfs, tolPPM);
      } catch (SQLiteException ex) {
         logger.error("error while extracting features", ex);
      }
      return result;
   }

   private List<Feature> extractFeatures(List<PutativeFeature> pfs, float tolPPM) {
      List<Feature> result = null;
      try {
         // Instantiates a Run Slice Data provider
         RunSliceDataProvider rsdProv = new RunSliceDataProvider(reader.getRunSliceIterator(1)); // TODO getLcmsRunSliceIte...
         FeatureExtractorConfig extractorConfig = new FeatureExtractorConfig(tolPPM, 5, 1, 3, 1200.0f, 0.05f, Option.empty(), 90, Option.empty());
         MzDbFeatureExtractor extractor = new MzDbFeatureExtractor(reader, 5, 5, extractorConfig);
         // Extract features
         result = scala.collection.JavaConversions.seqAsJavaList(extractor.extractFeatures(rsdProv, scala.collection.JavaConversions.asScalaBuffer(pfs), tolPPM));
      } catch (SQLiteException|StreamCorruptedException ex) {
         logger.error("error while extracting features", ex);
      } 
      return result;
   }

   @Override
   public Scan getScan(int scanIndex) {
      Scan scan = null;
      try {
         fr.profi.mzdb.model.Scan rawScan = reader.getScan(scanIndex);
         ScanData data = rawScan.getData();
         final double[] mzList = data.getMzList();
         final double[] leftSigma = new double[mzList.length];
         final double[] rightSigma = new double[mzList.length];
         final float[] intensityList = data.getIntensityList();
         List<Float> xAxisData = new ArrayList<Float>(mzList.length);
         List<Float> yAxisData = new ArrayList<Float>(mzList.length);
         for (int count = 0; count < mzList.length; count++) {
            if ((data.getLeftHwhmList() != null)) {
               leftSigma[count] = 2.0 * data.getLeftHwhmList()[count] / 2.35482;
               double x = mzList[count] - 4.0 * leftSigma[count];
               //search for the first left value less than x
               if (!xAxisData.isEmpty()) {
                  int k = xAxisData.size() - 1;
                  while (k >= 0 && xAxisData.get(k) >= x) {
                     k--;
                  }
                  k++;
                  for (; k < xAxisData.size(); k++) {
                     x = xAxisData.get(k);
                     double y = getGaussianPoint(x, mzList[count], intensityList[count], leftSigma[count]);
                     yAxisData.set(k, yAxisData.get(k) + (float) y);
                  }
               }
               for (; x < mzList[count]; x += leftSigma[count] / 2.0) {
                  double y = getGaussianPoint(x, mzList[count], intensityList[count], leftSigma[count]);
                  xAxisData.add((float) x);
                  yAxisData.add((float) y);
               }
            }
            xAxisData.add((float) mzList[count]);
            yAxisData.add(intensityList[count]);
            if (data.getRightHwhmList() != null) {
               rightSigma[count] = 2.0 * data.getRightHwhmList()[count] / 2.35482;
               double x = mzList[count] + rightSigma[count] / 2.0;
               if (!xAxisData.isEmpty()) {
                  int k = xAxisData.size() - 1;
                  while (k >= 0 && xAxisData.get(k) > x) {
                     k--;
                  }
                  k++;
                  for (; k < xAxisData.size(); k++) {
                     x = xAxisData.get(k);
                     double y = getGaussianPoint(x, mzList[count], intensityList[count], rightSigma[count]);
                     yAxisData.set(k, yAxisData.get(k) + (float) y);
                  }
               }
               for (; x < mzList[count] + 4.0 * rightSigma[count]; x += rightSigma[count] / 2.0) {
                  double y = getGaussianPoint(x, mzList[count], intensityList[count], rightSigma[count]);
                  xAxisData.add((float) x);
                  yAxisData.add((float) y);
               }
            }

         }
         scan = new Scan(scanIndex, rawScan.getHeader().getElutionTime(), Doubles.toArray(xAxisData), Floats.toArray(yAxisData), rawScan.getHeader().getMsLevel());
         StringBuilder builder = new StringBuilder(getName());

         if (scan.getMsLevel() == 2) {
            builder.append(massFormatter.format(rawScan.getHeader().getPrecursorMz())).append(" (");
            builder.append(rawScan.getHeader().getPrecursorCharge()).append("+) - ");
            scan.setPrecursorMz(rawScan.getHeader().getPrecursorMz());
            scan.setPrecursorCharge(rawScan.getHeader().getPrecursorCharge());
         }else{
            scan.setPrecursorMz(null);
            scan.setPrecursorCharge(null);
         }
         builder.append(", sc=").append(scanIndex).append(", rt=").append(timeFormatter.format(rawScan.getHeader().getElutionTime() / 60.0));
         builder.append(", ms").append(scan.getMsLevel());
         //scan.setTitle(builder.toString());
         scan.setTitle("");
         scan.setPeaksMz(mzList);
         scan.setPeaksIntensities(intensityList);
         //logger.debug("mzdb Scan length {} rebuilded in Scan length {} ", mzList.length, xAxisData.size());
      } catch (SQLiteException|StreamCorruptedException ex) {
         logger.error("enable to retrieve Scan data", ex);
      }
      return scan;
   }

   @Override
   public int getScanId(double retentionTime) {
      try {
         return reader.getScanHeaderForTime((float) retentionTime, 1).getScanId();
      } catch (Exception ex) {
         logger.error("enable to retrieve Scan Id", ex);
      }
      return 0;
   }

   @Override
   public int getNextScanId(int scanIndex, int msLevel) {
      return getNextSiblingScanId(scanIndex, msLevel, 1);
   }

   @Override
   public int getPreviousScanId(int scanIndex, int msLevel) {
      return getNextSiblingScanId(scanIndex, msLevel, -1);
   }

   private int getNextSiblingScanId(int scanIndex, int msLevel, int way) {
      try {
         ScanHeader header = getScanHeadersById().get(scanIndex);
         int maxScan = reader.getScansCount();
         int k = header.getScanId() + way;
         for (; (k > 0) && (k < maxScan); k += way) {
            if (getScanHeadersById().get(k).getMsLevel() == msLevel) {
               break;
            }
         }
         return k;
      } catch (SQLiteException e) {
         logger.error("Error while reading scansCount", e);
      }
      return 0;
   }

   private static double getGaussianPoint(double mz, double peakMz, double intensity, double sigma) {
      return intensity * Math.exp(-((mz - peakMz) * (mz - peakMz)) / (2.0 * sigma * sigma));
   }

   public static void main(String[] args) {
      for (double m = 500; m < 510; m += 0.1) {
         System.out.println("" + m + "\t" + getGaussianPoint(m, 502, 100, 1));
      }
   }

    @Override
    public List<Float> getMsMsEvent(double minMz, double maxMz) {
        Long startTime = System.currentTimeMillis();
        logger.debug("retrieve MS/MS events");
        List<Float> listMsMsEventTime = new ArrayList();
        if (ms2ScanHeaders == null) {
            try {
                logger.debug("retrieve Ms2 ScanHeader");
                sortScanHeader(reader.getMs2ScanHeaders());
            } catch (SQLiteException ex) {
                logger.error("Exception while retrieving ScanHeader "+ex);
            }
        }
        if (ms2ScanHeaders != null) {
            logger.debug("retrieve Ms2 ScanHeader in ["+minMz+", "+maxMz+"] ");
            int minId = ~Arrays.binarySearch(ms2ScanHeaderByMz, minMz);
            int maxId = ~Arrays.binarySearch(ms2ScanHeaderByMz, maxMz);
            if (minId != -1 && maxId != -1) {
                for (int i = minId; i <= maxId; i++) {
                    ScanHeader scanHeader = ms2ScanHeaders[i];
                    double mz = scanHeader.getPrecursorMz();
                    if (mz >= minMz && mz <= maxMz) {
                        listMsMsEventTime.add(scanHeader.getElutionTime());
                    }
                }
            }
        }
        logger.debug("retrieve MS/MS events finished in "+(System.currentTimeMillis() - startTime)+"+ ms");
        return listMsMsEventTime;
    }
}
