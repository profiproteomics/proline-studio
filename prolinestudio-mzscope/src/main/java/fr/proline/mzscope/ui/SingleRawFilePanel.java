package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IChromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
      JButton editFeatureBtn = new JButton();
      editFeatureBtn.setIcon(IconManager.getIcon(IconManager.IconType.SIGNAL));
      editFeatureBtn.setToolTipText("IChromatogram signal processing dialog");
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
      List<Signal> signals = new ArrayList<>();
      Iterable<IChromatogram> chromatograms = getAllChromatograms();
      for (IChromatogram chrom : chromatograms) {
        int minIdx = SpectrumUtils.getNearestPeakIndex(chrom.getTime(), min);
        int maxIdx = Math.min(SpectrumUtils.getNearestPeakIndex(chrom.getTime(), max)+1, chrom.getTime().length);
        Signal signal = new Signal(Arrays.copyOfRange(chrom.getTime(), minIdx, maxIdx), Arrays.copyOfRange(chrom.getIntensities(), minIdx, maxIdx));
        signals.add(signal);
      }
      JDialog dialog = new JDialog((JFrame)this.getTopLevelAncestor(), "Feature editor", true);
      dialog.setContentPane(SignalEditorBuilder.buildEditor(signals));
      dialog.pack();
      dialog.setVisible(true);
      logger.info("Edit feature within range "+min+", "+max);
   }

   @Override
   public void displayTIC() {
      final IRawFile rawFile = this.rawfile;
      if (rawFileLoading != null){
        rawFileLoading.setWaitingState(true);
      }
      logger.info("Display single TIC chromatogram");
      SwingWorker worker = new SwingWorker<IChromatogram, Void>() {
         @Override
         protected IChromatogram doInBackground() throws Exception {
            return rawFile.getTIC();
         }

         @Override
         protected void done() {
            try {
               displayChromatogram(get(), new Display(Display.Mode.REPLACE));
               setMsMsEventButtonEnabled(false);
            } catch (InterruptedException | ExecutionException e) {
               logger.error("Error while reading chromatogram");
            }finally{
                if (rawFileLoading != null){
                    rawFileLoading.setWaitingState(false);
                }
            }
         }
      };
      worker.execute();
   }

   @Override
   public void displayBPI() {
       if (rawFileLoading != null){
        rawFileLoading.setWaitingState(true);
       }
      final IRawFile rawFile = this.rawfile;
      logger.info("Display single base peak chromatogram");
      SwingWorker worker = new SwingWorker<IChromatogram, Void>() {
         @Override
         protected IChromatogram doInBackground() throws Exception {
            return rawFile.getBPI();
         }

         @Override
         protected void done() {
            try {
               displayChromatogram(get(), new Display(Display.Mode.REPLACE));
               setMsMsEventButtonEnabled(false);
            } catch (InterruptedException | ExecutionException e) {
               logger.error("Error while reading chromatogram");
            }finally{
                if (rawFileLoading != null){
                    rawFileLoading.setWaitingState(false);
                }
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
