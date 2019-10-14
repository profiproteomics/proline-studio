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
public abstract class AbstractLcMsMapSpectrum implements LcMsMapSpectrum {

    private double minX = 0;
    private double maxX = 0;
    private int sliceCount = 0;
    private double sliceSize = 0;

    /**
     *
     */
    public AbstractLcMsMapSpectrum(double minX, double maxX, double sliceSize) {
	super();
	this.minX = minX;
	this.maxX = maxX;
	double widthMz = maxX - minX;
	this.sliceCount = (int) Math.ceil(widthMz / sliceSize);
	this.sliceSize = widthMz / this.sliceCount;
    }

    /**
     * @return the minX
     */
    @Override
    public double getMinX() {
	return this.minX;
    }

    /**
     * @return the maxX
     */
    @Override
    public double getMaxX() {
	return this.maxX;
    }

    /**
     * @return the sliceCount
     */
    @Override
    public int getSliceCount() {
	return this.sliceCount;
    }

    /**
     * @return the sliceSize
     */
    @Override
    public double getSliceSize() {
	return this.sliceSize;
    }

}