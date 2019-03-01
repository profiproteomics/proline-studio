/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.ui.event;

import java.util.EventObject;

/**
 *
 * @author CB205360
 */
public class ExtractionEvent extends EventObject {

   public static int EXTRACTION_STARTED = 0;
   public static int EXTRACTION_DONE = 1;
   public static int EXTRACTION_CANCELLED = 2;

   private final int state;
   
   public ExtractionEvent(Object source, int state) {
      super(source);
      this.state = state;
   }

   public int getState() {
      return state;
   }

}
