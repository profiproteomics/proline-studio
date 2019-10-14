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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import fr.proline.mzscope.model.IChromatogram;

public class Main {

   public static String MS_LEVEL_CV_PARAM = "MS:1000511";

   /**
    * @param args
    */
   public static void main(String[] args) {

      String filepath = args[0];
      long start = System.currentTimeMillis();

      List<Scan> scans = mzMLReader.read(filepath);
      System.out.println(scans.size() + " scans read in :: " + (System.currentTimeMillis() - start) + " ms");

      start = System.currentTimeMillis();
      IChromatogram chromato = null;
      int maxIter = 100;
      for (int k = 0; k < maxIter; k++) {
         float v = (float) Math.random() * (1200.0f) + 400.0f;
         float delta = v * 10.0f / 1e6f;
         chromato = XICExtractor.extract(scans, v - delta, v + delta);
         // assert(chromato.length == scans.size());
      }

      // chromato = XICExtractor.extract(scans, 557.31f, 557.32f);
      chromato = XICExtractor.extract(scans, 435.71f, 435.85f);
      System.out.println(maxIter + " ions chromatogram extracted in :: " + (System.currentTimeMillis() - start) + " ms");

      System.out.println("mzML IChromatogram length " + chromato.getIntensities().length);
      JFrame frame = new JFrame("mzML IChromatogram Viewer");
      frame.setSize(800, 600);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

      try {
         BufferedWriter output = new BufferedWriter(new FileWriter("extracted_xic.tab"));
         output.write("index, rt, intensity \n");

         for (int count = 0; count < chromato.getIntensities().length; count++) {
            output.write(count + "," + chromato.getTime()[count] + "," + chromato.getIntensities()[count] + "\n");
         }
         output.close();

      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
