package fr.proline.mzscope.mzdb;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.FeatureDetectorConfig;
import fr.profi.mzdb.MzDbFeatureDetector;
import fr.profi.mzdb.MzDbFeatureExtractor;
import fr.profi.mzdb.MzDbReader;
//import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.profi.mzdb.algo.feature.extraction.FeatureExtractorConfig;
import fr.profi.mzdb.io.reader.RunSliceDataProvider;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peak;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.PutativeFeature;
import fr.profi.mzdb.model.RunSlice;
import fr.profi.mzdb.model.ScanData;
import fr.profi.mzdb.model.ScanHeader;
import fr.profi.mzdb.model.ScanSlice;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.ExtractionParams;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.IsotopePattern;
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
import java.util.TreeMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Tuple2;
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
      int i = 0;
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
         Peak[] peaks = reader.getXIC(minMz, maxMz, minRT, maxRT, 1, MzDbReader.XicMethod.MAX);
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
         logger.info("mzdb extract Chromato : {} - {} (MAX)", min, max);
         Peak[] peaks = reader.getXIC(min, max, 1, MzDbReader.XicMethod.MAX);
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
            return extractFeaturesFromMs2(params.mzTolPPM);
         case DETECT_FEATURES:
            return detectFeatures(params.mzTolPPM, params.minMz, params.maxMz);
         case DETECT_PEAKELS:
            return detectPeakels(params.mzTolPPM, params.minMz, params.maxMz);
      }
      return null;
   }

   private List<Feature> detectFeatures(float mzTolPPM, double minMz, double maxMz) {
      List<Feature> result = new ArrayList<>();
      FeatureDetectorConfig detectorConfig = new FeatureDetectorConfig(1, mzTolPPM, 5);
      MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
      try {
         Iterator<RunSlice> runSlices;
         if (minMz == 0 && maxMz == 0) {
            runSlices = getMzDbReader().getLcMsRunSliceIterator();
         } else {
            runSlices = getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
         }
         Peakel[] peakels = detector.detectPeakels(runSlices);
         
         Iterator<RunSlice> tmpRunSlices = (minMz == 0 && maxMz == 0) ? getMzDbReader().getLcMsRunSliceIterator() : getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
         double min = Double.MAX_VALUE;
         double max = Double.MIN_VALUE;
         for ( ; tmpRunSlices.hasNext(); ) {
            RunSlice rs = tmpRunSlices.next();
            min = Math.min(min, rs.getHeader().getBeginMz());
            max = Math.max(max, rs.getHeader().getEndMz());
         }
         logger.info("Real bounds : "+min+" - "+max);
         Arrays.sort(peakels, new Comparator<Peakel>() {
            @Override
            public int compare(Peakel p1, Peakel p2) {
               return Double.compare(p2.getApexIntensity(), p1.getApexIntensity());
            }
         });
         logger.info("Peakels detected : "+peakels.length);
         boolean[] assigned = new boolean[peakels.length];
         Arrays.fill(assigned, false);
         Pair<Double, Integer> peakelIndexesByMz[] = new Pair[peakels.length];
         for (int k = 0; k < peakels.length; k++) {
            peakelIndexesByMz[k] = new ImmutablePair<>(peakels[k].getMz(), k);
         }
         
         Arrays.sort(peakelIndexesByMz, new Comparator<Pair<Double, Integer>>() {
            @Override
            public int compare(Pair<Double, Integer> p1, Pair<Double, Integer> p2) {
               return Double.compare(p1.getLeft(), p2.getLeft());
            }
         });
         
         
         for (int k = 0; k < peakels.length; k++) {
            if ((k % 10000) == 0) {
               logger.info("processing peakel "+k);
            }
            if (!assigned[k]) {
               ScanSlice[] slices = reader.getScanSlices(peakels[k].getApexMz()-5.0, peakels[k].getApexMz()+5.0, peakels[k].getApexElutionTime()-0.1, peakels[k].getApexElutionTime()+0.1, 1);
               int i = 0; 
               while((i < slices.length) && (slices[i].getHeader().getScanId() != peakels[k].getApexScanId())) {
                i++;
               }
               ScanData data = slices[i].getData();
               //SortedMap putativePatterns = IsotopicPatternScorer.calclIsotopicPatternHypotheses(data, peakels[k].getMz(), mzTolPPM);
               TreeMap<Double, TheoreticalIsotopePattern> putativePatterns = IsotopePattern.getOrderedIPHypothesis(data, peakels[k].getMz());
               TheoreticalIsotopePattern bestPattern = (TheoreticalIsotopePattern) putativePatterns.get(putativePatterns.firstKey());
               List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount()+1);
               for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                  int idx = findPeakIndex(peakels, peakelIndexesByMz, (double)t._1, peakels[k], mzTolPPM);
                  if (idx != -1) {
                     assigned[idx] = true;
                     l.add(peakels[idx]);
                  } //else if ( ((double)t._1 > min) && ((double)t._1 < max)) {
//                     logger.info("Isotope at "+(double)t._1+" not found");
//                  }
               }
               if (l.isEmpty()) {
                  logger.warn("Strange situation : peakel not found within isotopic pattern .... "+peakels[k].getMz());
                  l.add(peakels[k]);
               }
//               logger.info("Creates feature with "+l.size()+" peakels at mz="+l.get(0).getMz()+ " from peakel "+peakels[k].getMz()+ " at "+peakels[k].getApexElutionTime()/60.0);
               Feature feature = new Feature(l.get(0).getMz(), bestPattern.charge(), JavaConverters.asScalaBufferConverter(l).asScala(), true);
               result.add(feature);
            }
         }
         
      } catch (SQLiteException | StreamCorruptedException ex) {
         logger.error("Error while getting LcMs RunSlice Iterator: " + ex);
      }

      logger.info("Features detected : "+result.size());
      return result;

   }

   private List<Feature> detectPeakels(float mzTolPPM, double minMz, double maxMz) {
      List<Feature> result = new ArrayList<>();
      // Instantiates a Run Slice Data provider
      int msLevel = 1;
      FeatureDetectorConfig detectorConfig = new FeatureDetectorConfig(msLevel, mzTolPPM, 5);
      MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
      try {
         Iterator<RunSlice> runSlices;
         if (minMz == 0 && maxMz == 0) {
            runSlices = getMzDbReader().getLcMsRunSliceIterator();
         } else {
            runSlices = getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
         }
         Peakel[] peakels = detector.detectPeakels(runSlices);
         for (Peakel peakel : peakels) {
            ArrayList<Peakel> l = new ArrayList<>();
            l.add(peakel);
            //creates a fake Feature associated to this peakel in order to always display Features
            Feature feature = new Feature(peakel.getMz(), 0, JavaConverters.asScalaBufferConverter(l).asScala(), false);
            if (minMz == 0 && maxMz == 0) {
               result.add(feature);
            } else {
               //check that the feature is in the mass range
               if (feature.getMz() >= minMz && feature.getMz() <= maxMz) {
                  result.add(feature);
               }
            }
         }
      } catch (SQLiteException | StreamCorruptedException ex) {
         logger.error("Error while getting LcMs RunSlice Iterator: " + ex);
      }

      return result;
   }

   private List<Feature> extractFeaturesFromMs2(float tolPPM) {
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
      } catch (SQLiteException | StreamCorruptedException ex) {
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
         } else {
            scan.setPrecursorMz(null);
            scan.setPrecursorCharge(null);
         }
         builder.append(", sc=").append(scanIndex).append(", rt=").append(timeFormatter.format(rawScan.getHeader().getElutionTime() / 60.0));
         builder.append(", ms").append(scan.getMsLevel());
         //scan.setTitle(builder.toString());
         scan.setTitle("");
         scan.setScanData(data);
         //logger.debug("mzdb Scan length {} rebuilded in Scan length {} ", mzList.length, xAxisData.size());
      } catch (SQLiteException | StreamCorruptedException ex) {
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
            logger.error("Exception while retrieving ScanHeader " + ex);
         }
      }
      if (ms2ScanHeaders != null) {
         logger.debug("retrieve Ms2 ScanHeader in [" + minMz + ", " + maxMz + "] ");
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
      logger.debug("retrieve MS/MS events finished in " + (System.currentTimeMillis() - startTime) + "+ ms");
      return listMsMsEventTime;
   }

   private int findPeakIndex(Peakel[] peakels, Pair<Double, Integer>[] peakelIndexesByMz, double moz, Peakel referencePeakel, float mzTolPPM) {
      double min = Double.MAX_VALUE;
      int resultIdx = -1;
      Comparator<Pair<Double, Integer>> c = new Comparator<Pair<Double, Integer>>() {

         @Override
         public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
            return Double.compare(o1.getLeft(), o2.getLeft());
         }
      };
      int lowerIdx = Arrays.binarySearch(peakelIndexesByMz,new ImmutablePair<Double, Integer>(moz - (moz*mzTolPPM/1e6), 0), c);
      lowerIdx = (lowerIdx < 0) ? Math.max(0,~lowerIdx-1) : Math.max(0,lowerIdx-1);
      int upperIdx = Arrays.binarySearch(peakelIndexesByMz, new ImmutablePair<Double, Integer>(moz + (moz*mzTolPPM/1e6),0), c);
      upperIdx = (upperIdx < 0) ? Math.min(peakelIndexesByMz.length - 1,~upperIdx) : Math.min(peakelIndexesByMz.length - 1,upperIdx+1);

      for (int i = lowerIdx; i <= upperIdx; i++) {
         int k = peakelIndexesByMz[i].getRight();
         if ( (1e6*Math.abs(peakels[k].getMz() - moz)/moz < mzTolPPM) 
                 && ((Math.abs(peakels[k].getApexElutionTime()-referencePeakel.getApexElutionTime())/referencePeakel.calcDuration()) < 0.25)
                 && (Math.abs(peakels[k].getMz() - moz) < min)) {
            min = Math.abs(peakels[k].getMz() - moz);
            resultIdx = k;
         }
      }
      return resultIdx;
   }

}
