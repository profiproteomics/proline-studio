/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import fr.proline.mzscope.mzdb.ThreadedMzdbRawFile;
import fr.proline.mzscope.mzml.MzMLRawFile;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class RawFileFactory {

   private static Logger logger = LoggerFactory.getLogger(RawFileFactory.class);

   public static IRawFile createRawFile(File file) {
      IRawFile rawFile = null;
      if (file.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
         rawFile = new ThreadedMzdbRawFile(file);
         logger.info("Rawfile {} created", file.getAbsolutePath());
      } else if (file.getAbsolutePath().endsWith(".mzML")) {
         rawFile = new MzMLRawFile(file);
         logger.info("Rawfile {} created", file.getAbsolutePath());
      }
      return rawFile;
   }

}
