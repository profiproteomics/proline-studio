package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Ms1ExtractionRequest;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
abstract class AbstractMs1ExtractionWorker extends SwingWorker<Chromatogram, Void> {

   private static Logger logger = LoggerFactory.getLogger(AbstractMs1ExtractionWorker.class);
   
   private final IRawFile rawFile;
   private final Ms1ExtractionRequest parameters;
   
   public AbstractMs1ExtractionWorker(IRawFile rawFile, double min, double max) {
      this(rawFile, Ms1ExtractionRequest.builder().setMaxMz(min).setMinMz(max).build());
   }

   public AbstractMs1ExtractionWorker(IRawFile rawFile, Ms1ExtractionRequest params) {
      this.rawFile = rawFile;
      this.parameters = params;
   }

   @Override
   protected Chromatogram doInBackground() throws Exception {
      return rawFile.getXIC(parameters);
   }

   @Override
   abstract protected void done();

}
