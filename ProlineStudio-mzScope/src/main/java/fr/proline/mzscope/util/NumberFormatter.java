/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.util;

import java.text.DecimalFormat;
import org.jdesktop.swingx.renderer.StringValue;

/**
 * format number depending of a specified pattern
 * @author MB243701
 */
public class NumberFormatter implements StringValue {

   private DecimalFormat format;
   
   public NumberFormatter(String pattern) {
      this.format = new DecimalFormat(pattern);
   }
   
   @Override
   public String getString(Object value) {
      if (value == null) {
         return "";
      }
      try {
         return format.format(value);
      } catch (IllegalArgumentException e) {
         // didn't work, nothing we can do
      }
      return value.toString();
   }
}
