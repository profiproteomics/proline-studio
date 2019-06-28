package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IChromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MsnExtractionRequest;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
abstract class AbstractMs1ExtractionWorker extends SwingWorker<IChromatogram, Void> {

   private static Logger logger = LoggerFactory.getLogger(AbstractMs1ExtractionWorker.class);
   
   private final IRawFile rawFile;
   private final MsnExtractionRequest parameters;
   
   public AbstractMs1ExtractionWorker(IRawFile rawFile, double min, double max) {
      this(rawFile, MsnExtractionRequest.builder().setMaxMz(min).setMinMz(max).build());
   }

   public AbstractMs1ExtractionWorker(IRawFile rawFile, MsnExtractionRequest params) {
      this.rawFile = rawFile;
      this.parameters = params;
   }

   @Override
   protected IChromatogram doInBackground() throws Exception {
      return rawFile.getXIC(parameters);
   }

   @Override
   abstract protected void done();

}
