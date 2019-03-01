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
