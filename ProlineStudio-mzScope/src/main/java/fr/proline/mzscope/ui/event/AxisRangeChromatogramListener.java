/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.event;

/**
 *
 * @author MB243701
 */
public interface AxisRangeChromatogramListener {
    /**
     *  update the axis range from a plot
     * @param oldX
     * @param newX
     * @param oldY
     * @param newY 
     */
    public void updateAxisRange(double[] oldX,  double[] newX,  double[] oldY, double[] newY);
}
