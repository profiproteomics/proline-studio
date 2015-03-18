package fr.proline.mzscope.mzml;

import fr.proline.mzscope.mzml.Scan;

public class IndexPredictor {

   public static void testLinearity(Scan s) {

   }

   public static void outputMasses(Scan s) {
      float[] masses = s.getMasses();
      for (int k = 0; k < masses.length; k++) {
         System.out.println(k + "\t" + masses[k]);
      }
   }
}
