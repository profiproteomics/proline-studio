/* 
 * Copyright (C) 2019
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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public class Signal {

   final private static Logger logger = LoggerFactory.getLogger(Signal.class);

   private int m_signalType = 0;
   public final static int PROFILE = 0;
   public final static int CENTROID = 1;
   
   private double[] m_xSeries;
   private double[] m_ySeries;

   public Signal(double[] xSeries, double[] ySeries) {
      this.m_xSeries = xSeries;
      this.m_ySeries = ySeries;
   }
   
   public Signal(double[] xSeries, float[] ySeries) {
      this.m_xSeries = xSeries;
      this.m_ySeries = Signal.convertToDoubleArray(ySeries);
   }

   public void setSignalType(int signalType){
       if(signalType == PROFILE || signalType==CENTROID)
           m_signalType = signalType;
   }
   
   public int getSignalType(){
       return m_signalType;
   }
   
   public double[] getXSeries() {
      return m_xSeries;
   }

   public double[] getYSeries() {
      return m_ySeries;
   }
   
    private static double[] convertToDoubleArray(float[] source) {
        double[] dest = new double[source.length];
        for(int i=0; i<source.length ; i++) {
            dest[i] = source[i];
        }
        return dest;
    }
    
    public List<Tuple2> toScalaArrayTuple(boolean includeNullIntensity) {
      List<Tuple2> xyPairs = new ArrayList<>();
      for (int k = 0; k < m_xSeries.length; k++) {
        if(!includeNullIntensity && m_ySeries[k] <= 0.0 )
            continue;
        xyPairs.add(new Tuple2(m_xSeries[k], m_ySeries[k]));
      }
      return xyPairs;
   }
}
