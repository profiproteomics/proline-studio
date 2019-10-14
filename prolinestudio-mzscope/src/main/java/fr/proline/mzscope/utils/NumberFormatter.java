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
package fr.proline.mzscope.utils;

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
