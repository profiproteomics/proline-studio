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
package fr.proline.mzscope.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class Signal {

   final private static Logger logger = LoggerFactory.getLogger(Signal.class);

   private double[] m_xSeries;
   private double[] m_ySeries;

   public Signal(double[] m_xSeries, double[] m_ySeries) {
      this.m_xSeries = m_xSeries;
      this.m_ySeries = m_ySeries;
   }

   public double[] getXSeries() {
      return m_xSeries;
   }

   public double[] getYSeries() {
      return m_ySeries;
   }
   
   
}
