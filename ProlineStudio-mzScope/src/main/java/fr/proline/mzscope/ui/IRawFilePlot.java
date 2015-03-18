/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.ui;

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import java.awt.Color;

/**
 *
 * @author CB205360
 */
public interface IRawFilePlot {
   
   /**
    * display the chromatogram and return the plot color
    * @param chromato
    * @return 
    */ 
   public Color displayChromatogram(Chromatogram chromato);
   /**
    * add the chromatogram and return the plot color
    * @param chromato
    * @return 
    */ 
   public Color addChromatogram(Chromatogram chromato);
   
   void extractChromatogram(double minMz, double maxMz);

   void displayFeature(Feature f);
   void displayScan(int index);

   IRawFile getCurrentRawfile();

   Chromatogram getCurrentChromatogram();
   
   public void extractChromatogramWithFeature(double minMz, double maxMz, double elutionTime, double firstScanTime, double lastScanTime);
}
