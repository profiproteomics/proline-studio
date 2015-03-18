package fr.proline.mzscope.mzdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import fr.profi.mzdb.MzDbFeatureExtractor;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.MzDbReader.XicMethod;
import fr.profi.mzdb.algo.feature.extraction.FeatureExtractorConfig;
import fr.profi.mzdb.io.reader.RunSliceDataProvider;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peak;
import fr.profi.mzdb.model.PutativeFeature;
import fr.profi.mzdb.model.ScanHeader;
import scala.Option;

public class Main {

   public static void main(String[] args) {

      String filepath = args[0];
      try {
         long start = System.currentTimeMillis();
         MzDbReader reader = new MzDbReader(filepath, true);
         System.out.println("File mzDb read in :: " + (System.currentTimeMillis() - start) + " ms");
         int[] range = reader.getMzRange(1);
         System.out.println("mz Range :: " + range[0] + "-" + range[1]);
         start = System.currentTimeMillis();

         float tolPPM = 5.0f;
         FeatureExtractorConfig extractorConfig = new FeatureExtractorConfig(tolPPM, 5, 1, 3, 1200.0f, 0.05f, Option.empty() , 90, Option.empty());
         MzDbFeatureExtractor extractor = new MzDbFeatureExtractor(reader, 5, 5, extractorConfig);
         System.out.println("retrieve scan headers  ...");
         ScanHeader[] scanHeaders = reader.getScanHeaders();
         Iterator<ScanHeader> ms2ScanHeaders = Iterators.filter(Iterators.forArray(scanHeaders), new Predicate<ScanHeader>() {
            public boolean apply(ScanHeader sh) {
               return sh.getMsLevel() == 2;
            }
         });

         List<PutativeFeature> pfs = new ArrayList<PutativeFeature>();

         System.out.println("building putative features list from MS2 scan events...");
         while (ms2ScanHeaders.hasNext()) {
            ScanHeader scanH = ms2ScanHeaders.next();
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
         RunSliceDataProvider rsdProv = new RunSliceDataProvider(reader.getRunSliceIterator(1));

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
         Peak[] chromato = reader.getXIC(f.getMz() - f.getMz() * tolPPM / 1e6, f.getMz() + f.getMz() * tolPPM / 1e6, 1, XicMethod.SUM);
         System.out.println("Chromato length: " + chromato.length);


      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } 

   }
}
