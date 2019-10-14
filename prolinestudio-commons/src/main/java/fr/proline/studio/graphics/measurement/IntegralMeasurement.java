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
package fr.proline.studio.graphics.measurement;

import fr.proline.studio.graphics.PlotBaseAbstract;
import fr.proline.studio.graphics.PlotLinear;

/**
 * Calculate Integral for a waveform (PlotLinear)
 * 
 * @author JM235353
 */
public class IntegralMeasurement extends DeltaXMeasurement {

    public IntegralMeasurement(PlotBaseAbstract plot) {
        super(plot);

        
        
        DeltaXMeasurement.AlgorithmMeasurement integralAlgorithm = new DeltaXMeasurement.AlgorithmMeasurement() {
            @Override
            public String getName() {
                return "Integral measurement";
            }

            @Override
            public String calculate(PlotBaseAbstract plot, double x1, double x2) {

                PlotLinear plotLinear = ((PlotLinear) plot);
                double integral = calculateIntegral(plotLinear.getDataX(), plotLinear.getDataY(), x1, x2);

                String res = plot.getBasePlotPanel().getXAxis().defaultFormat(integral);
                return res;
            }

        };

        setAlgorithm(integralAlgorithm);
    }
    
    @Override
    public void applyMeasurement(int x, int y) {
        super.applyMeasurement(x, y);

    }

    private double calculateIntegral(double[] dataX, double[] dataY, double xStartD, double xEndD) {

        // JPM : could be accelerated to search faster xStartD 
        
        double area = 0;
        int size = dataX == null ? 0 : dataX.length;
        if (size > 0) {

            double x0 = dataX[0];
            double y0 = dataY[0];
            boolean isNan0 = Double.isNaN(x0) || Double.isNaN(y0);

            for (int i = 1; i < size; i++) {
                double x1 = dataX[i];
                double y1 = dataY[i];

                boolean isNan1 = Double.isNaN(x1) || Double.isNaN(y1);

                if (!isNan0 & !isNan1) {
                    if (x1 < xStartD) {
                        // nothing to do
                    } else if (x0 > xEndD) {
                        // finished
                        break;
                    } else if (x0 < xStartD) {
                        // x0<xStartD & x1>=xStartD
                        if (x1 <= xEndD) {
                            // x0<xStartD & x1>=xStartD & x1<=xEndD
                            double yStartD = ((xStartD - x0) / (x1 - x0)) * (y1 - y0) + y0;
                            area += ((yStartD + y1) / 2) * (x1 - xStartD);
                        } else {
                            // x0<xStartD & x1>=xEndD
                            double yStartD = ((xStartD - x0) / (x1 - x0)) * (y1 - y0) + y0;
                            double yEndD = ((xEndD - x0) / (x1 - x0)) * (y1 - y0) + y0;
                            area += ((yStartD + yEndD) / 2) * (xEndD - xStartD);
                        }
                    } else if (x1 <= xEndD) {
                        // x0>=xStartD & x1<=xEndD
                        area += ((y0 + y1) / 2) * (x1 - x0);
                    } else {
                        // x0<=xEndD & x1>=xEndD
                        double yEndD = ((xEndD - x0) / (x1 - x0)) * (y1 - y0) + y0;
                        area += ((yEndD + y1) / 2) * (x1 - xEndD);
                    }

                }

                x0 = x1;
                y0 = y1;
                isNan0 = isNan1;

            }

        }
        return area;
    }
}
