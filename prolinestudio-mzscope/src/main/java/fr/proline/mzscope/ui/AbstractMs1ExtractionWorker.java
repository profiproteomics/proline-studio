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
