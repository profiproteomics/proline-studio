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
package fr.proline.mzscope.map;

/**
 * @author JeT
 *
 */
public interface LcMsMapSpectrum {

    /**
     * @return the intensities
     */
    double[] getIntensities();

    /**
     * @return the intensity at a given m/z
     */
    double getIntensity(double mz);

    /**
     * @return the intensity at a given index
     */
    double getIntensity(int mzIndex) throws IndexOutOfBoundsException;

    /**
     * @return the maxIntensity
     */
    double getMaxIntensity();

    int getIndex(double xValue);

    /**
     * @return
     */
    double getMinX();

    /**
     * @return
     */
    double getMaxX();

    /**
     * @return
     */
    int getSliceCount();

    /**
     * @return
     */
    double getSliceSize();
}