/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.util.CyclicColorPalette;
import java.awt.Color;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class SingleRawFilePanel extends AbstractRawFilePanel {

   final private static Logger logger = LoggerFactory.getLogger(SingleRawFilePanel.class);

   private IRawFile rawfile;

   public SingleRawFilePanel(IRawFile rawfile) {
      super();
      this.rawfile = rawfile;
      displayTIC();
   }

   @Override
   public IRawFile getCurrentRawfile() {
      return rawfile;
   }

   @Override
   public void displayTIC() {
      final IRawFile rawFile = this.rawfile;
      logger.info("Display single TIC chromatogram");
      SwingWorker worker = new SwingWorker<Chromatogram, Void>() {
         @Override
         protected Chromatogram doInBackground() throws Exception {
            return rawFile.getTIC();
         }

         @Override
         protected void done() {
            try {
               displayChromatogram(get());
               setMsMsEventButtonEnabled(false);
            } catch (InterruptedException | ExecutionException e) {
               logger.error("Error while reading chromatogram");
            }
         }
      };
      worker.execute();
   }

   @Override
   public void displayBPI() {
      final IRawFile rawFile = this.rawfile;
      logger.info("Display single BPI chromatogram");
      SwingWorker worker = new SwingWorker<Chromatogram, Void>() {
         @Override
         protected Chromatogram doInBackground() throws Exception {
            return rawFile.getBPI();
         }

         @Override
         protected void done() {
            try {
               displayChromatogram(get());
               setMsMsEventButtonEnabled(false);
            } catch (InterruptedException | ExecutionException e) {
               logger.error("Error while reading chromatogram");
            }
         }
      };
      worker.execute();
   }

    @Override
    public Color getPlotColor(IRawFile rawFile) {
        return CyclicColorPalette.getColor(1);
    }
}
