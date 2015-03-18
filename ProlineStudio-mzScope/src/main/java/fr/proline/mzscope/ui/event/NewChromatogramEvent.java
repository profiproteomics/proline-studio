/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.ui.event;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;

/**
 *
 * @author CB205360
 */
public class NewChromatogramEvent {
   
   public final Object source;
   public final Chromatogram chromatogram;
   public final IRawFile rawFile;

   public NewChromatogramEvent(Object source, Chromatogram chromatogram, IRawFile rawFile) {
      this.chromatogram = chromatogram;
      this.rawFile = rawFile;
      this.source = source;
   }
   
}
