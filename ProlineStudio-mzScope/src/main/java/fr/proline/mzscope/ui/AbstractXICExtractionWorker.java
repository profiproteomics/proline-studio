/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
abstract class AbstractXICExtractionWorker extends SwingWorker<Chromatogram, Void> {

   private static Logger logger = LoggerFactory.getLogger(AbstractXICExtractionWorker.class);
   
   private IRawFile rawFile;
   private double minMz;
   private double maxMz;

   public AbstractXICExtractionWorker(IRawFile rawFile, double min, double max) {
      this.rawFile = rawFile;
      this.minMz = min;
      this.maxMz = max;
   }

   @Override
   protected Chromatogram doInBackground() throws Exception {
      return rawFile.getXIC(minMz, maxMz);
   }

   @Override
   abstract protected void done();

}
