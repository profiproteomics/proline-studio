package fr.proline.mzscope.mzml;

import fr.proline.mzscope.model.Chromatogram;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.proline.mzscope.utils.BinarySearch;

public class XICExtractor {

   static Logger logger = LoggerFactory.getLogger(XICExtractor.class);

   static void search(Scan s, float v) {
      BinarySearch bs = new BinarySearch();
      float[] values = s.getMasses();
      int index = bs.searchIndex(values, v, 0, values.length);
      logger.debug("value " + v + " found at " + index);
      logger.debug("average iterations " + (bs.getIterations()));
   }

   static Chromatogram extract(List<Scan> scans, float vmin, float vmax) {

      BinarySearch bs = new BinarySearch();
      long start = System.currentTimeMillis();

      double[] XIintensities = new double[scans.size()];
      double[] XItime = new double[scans.size()];

      float[] masses = scans.get(0).getMasses();
      float[] intensities = scans.get(0).getIntensities();

      int index = bs.searchIndex(masses, vmin, 0, masses.length);
      XIintensities[0] = summary(masses, intensities, index, vmax);
      XItime[0] = scans.get(0).getRetentionTime();
      // float relativePos = index/values.length;
      for (int k = 1; k < scans.size(); k++) {
         masses = scans.get(k).getMasses();
         intensities = scans.get(k).getIntensities();
         // int imin = (int)Math.max(0,
         // relativePos*values.length-(values.length/10));
         // int imax =
         // (int)Math.min(relativePos*values.length+(values.length/10),
         // values.length);
         // index = bs.search(values, v, imin, imax);
         index = bs.searchIndex(masses, vmin, 0, masses.length);
         XIintensities[k] = summary(masses, intensities, index, vmax);
         XItime[k] = scans.get(k).getRetentionTime();
         // relativePos = index / values.length;
      }
      logger.info("search in :: " + (System.currentTimeMillis() - start) + " ms");
      logger.info("average iterations :: " + (bs.getIterations() / (float) scans.size()));
      logger.info("nb of failed prediction" + (bs.getFails()));

      // WARN : setting rawFilename of Chromato to null is not recommanded : becareful to set a real rawFilename
      Chromatogram chromatogram = new Chromatogram(null);
      chromatogram.time = XItime;
      chromatogram.intensities = XIintensities;
      chromatogram.minMz = (double) vmin;
      chromatogram.maxMz = (double) vmax;

      return chromatogram;
   }

   static double summary(float[] masses, float[] intensities, int index, float vmax) {
      double result = 0.0;
      int index2 = (index == 0) ? 1 : index;
      while ((index2 < (masses.length - 1)) && (masses[index2] < vmax)) {
         if ((intensities[index2] > intensities[index2 - 1]) && (intensities[index2] >= intensities[index2 + 1])) {
            result += intensities[index2++];
         }
         index2++;
      }
      return result;
   }

}
