package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.utils.MzScopeConstants.DisplayMode;
import fr.proline.mzscope.utils.ScanUtils;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JToolBar;
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

   public SingleRawFilePanel(IRawFile rawfile, boolean displayDefaultChrom) {
      super();
      this.rawfile = rawfile;
      updateToolbar();
      if (displayDefaultChrom)
         displayTIC();
   }

   public SingleRawFilePanel(IRawFile rawfile) {
      this(rawfile, true);
   }

   @Override
   public IRawFile getCurrentRawfile() {
      return rawfile;
   }

   protected JToolBar updateToolbar() {
      chromatogramToolbar.addSeparator();
      JButton editFeatureBtn = new JButton("Feat");
      editFeatureBtn.setToolTipText("Edit Feature Chromatogram");
      editFeatureBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            editFeature();
         }
      });

      chromatogramToolbar.add(editFeatureBtn);
      return chromatogramToolbar;
   }

   private void editFeature() {
      double min = chromatogramPanel.getChromatogramPlotPanel().getXAxis().getMinValue();
      double max = chromatogramPanel.getChromatogramPlotPanel().getXAxis().getMaxValue();
      Chromatogram chrom = getCurrentChromatogram();
      int minIdx = ScanUtils.getNearestPeakIndex(chrom.time, min);
      int maxIdx = Math.min(ScanUtils.getNearestPeakIndex(chrom.time, max)+1, chrom.time.length);
      Signal signal = new Signal(Arrays.copyOfRange(chrom.time, minIdx, maxIdx), Arrays.copyOfRange(chrom.intensities, minIdx, maxIdx));
      JDialog dialog = new JDialog((JFrame)this.getTopLevelAncestor(), "Feature editor", true);
      dialog.setContentPane(new SignalEditorPanel(signal));
      dialog.pack();
      dialog.setVisible(true);
      logger.info("Edit feature within range "+min+", "+max);
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
               displayChromatogram(get(), DisplayMode.REPLACE);
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
      logger.info("Display single base peak chromatogram");
      SwingWorker worker = new SwingWorker<Chromatogram, Void>() {
         @Override
         protected Chromatogram doInBackground() throws Exception {
            return rawFile.getBPI();
         }

         @Override
         protected void done() {
            try {
               displayChromatogram(get(), DisplayMode.REPLACE);
               setMsMsEventButtonEnabled(false);
            } catch (InterruptedException | ExecutionException e) {
               logger.error("Error while reading chromatogram");
            }
         }
      };
      worker.execute();
   }

    @Override
    public Color getPlotColor(String rawFilename) {
        return CyclicColorPalette.getColor(1);
    }
}
