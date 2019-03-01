package fr.proline.mzscope.mzdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import fr.profi.mzdb.FeatureDetectorConfig;
import fr.profi.mzdb.MzDbFeatureDetector;

import fr.profi.mzdb.MzDbFeatureExtractor;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.SmartPeakelFinderConfig;
import fr.profi.mzdb.XicMethod;
import fr.profi.mzdb.algo.feature.extraction.FeatureExtractorConfig;
import fr.profi.mzdb.io.reader.provider.RunSliceDataProvider;
import fr.profi.mzdb.model.AcquisitionMode;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peak;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.PutativeFeature;
import fr.profi.mzdb.model.RunSlice;
import fr.profi.mzdb.model.SpectrumHeader;
import scala.Option;

public class Main {

   public static void main(String[] args) {

      //String filepath = args[0];
      String filepath = "C:\\Local\\bruley\\Data\\Proline\\Data\\mzdb\\TTOF_00063.mzdb";
      
      try {
         long start = System.currentTimeMillis();
         MzDbReader reader = new MzDbReader(filepath, true);
         System.out.println("Ohh ... File mzDb read in :: " + (System.currentTimeMillis() - start) + " ms");
         int[] range = reader.getMzRange(1);
         System.out.println("MS mz Range :: " + range[0] + "-" + range[1]);
         range = reader.getMzRange(2);
         System.out.println("MS2 mz Range :: " + range[0] + "-" + range[1]);
         start = System.currentTimeMillis();

         AcquisitionMode acqMode = reader.getAcquisitionMode();
         if (acqMode != null && acqMode.equals(AcquisitionMode.SWATH)){
                System.out.println("this is a DIA file");
         }
         float tolPPM = 5.0f;
         
         
        Iterator<RunSlice> runSlices = reader.getLcMsnRunSliceIterator(450.0, 455.0, 200, 1000);
        FeatureDetectorConfig detectorConfig = new FeatureDetectorConfig(2, tolPPM, 5, new SmartPeakelFinderConfig(5, 3, 0.75f, false, 10, false, false, true));
        MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
            
        Peakel[] peakels = detector.detectPeakels(runSlices, Option.apply(1));
         
         
         FeatureExtractorConfig extractorConfig = new FeatureExtractorConfig(tolPPM, 5, 1, 3, 1200.0f, 0.05f, Option.empty() , 90, Option.empty());
         MzDbFeatureExtractor extractor = new MzDbFeatureExtractor(reader, 5, 5, extractorConfig);
         System.out.println("retrieve scan headers  ...");
         SpectrumHeader[] spectrumHeaders = reader.getSpectrumHeaders();
         Iterator<SpectrumHeader> ms2SpectrumHeaders = Iterators.filter(Iterators.forArray(spectrumHeaders), new Predicate<SpectrumHeader>() {
            public boolean apply(SpectrumHeader sh) {
               return sh.getMsLevel() == 2;
            }
         });

         List<PutativeFeature> pfs = new ArrayList<PutativeFeature>();

         System.out.println("building putative features list from MS2 scan events...");
         while (ms2SpectrumHeaders.hasNext()) {
            SpectrumHeader scanH = ms2SpectrumHeaders.next();
            if ((scanH.getElutionTime() > 30 * 60) && (scanH.getElutionTime() > 33 * 60)) {
               pfs.add(new PutativeFeature(
                       PutativeFeature.generateNewId(),
                       scanH.getPrecursorMz(),
                       scanH.getPrecursorCharge(),
                       scanH.getId(),
                       2
               ));
            }
         }

         // Instantiates a Run Slice Data provider
         RunSliceDataProvider rsdProv = new RunSliceDataProvider(reader.getLcMsRunSliceIterator());

         // Extract features
         List<Feature> mzDbFts = scala.collection.JavaConversions.seqAsJavaList(extractor.extractFeatures(rsdProv, scala.collection.JavaConversions.asScalaBuffer(pfs), tolPPM));

         System.out.println("Elapsed time (ms) = "+(System.currentTimeMillis() - start));
         
         for (Feature f : mzDbFts) {
            System.out.println(f.getMz() + " - " + f.getArea());
         }

         System.out.println("Select feature number 4 = ");
         Feature f = mzDbFts.get(4);
         System.out.println("Feature m/z: " + f.getMz());
         System.out.println("Feature range: " + (f.getMz() - f.getMz() * tolPPM / 1e6) + "-" + (f.getMz() + f.getMz() * tolPPM / 1e6));
         System.out.println("Feature retentionTime: " + f.getElutionTime() / 60.0);
         System.out.println("Feature intentitySum: " + f.getIntensitySum());
         System.out.println("Feature area: " + f.getArea());

//         System.out.println("headers length: " + f.getScanHeaders().length);
         System.out.println("Extract Chromatogram from selected feature ...");
         Peak[] chromato = reader.getMsXicInMzRange(f.getMz() - f.getMz() * tolPPM / 1e6, f.getMz() + f.getMz() * tolPPM / 1e6,  XicMethod.SUM);
         System.out.println("Chromato length: " + chromato.length);


      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } 

   }
}
