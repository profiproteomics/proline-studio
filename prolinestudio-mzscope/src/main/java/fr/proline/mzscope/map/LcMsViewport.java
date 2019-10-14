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

import java.awt.geom.Point2D;

/**
 *
 * @author JeT define a window (viewport) in an LcMsMap min & max Retention time - min & max m/z
 */
public class LcMsViewport {

    public double minMz = 0; // min m/z
    public double maxMz = 0; // max m/z
    public double minRt = 0; // min retention time
    public double maxRt = 0; // max retention time

    /**
     * Default constructor
     */
    public LcMsViewport() {
	super();
	this.minMz = 0;
	this.maxMz = 0;
	this.minRt = 0;
	this.maxRt = 0;
    }

    /**
     * Copy constructor
     */
    public LcMsViewport(LcMsViewport viewport) {
	super();
	if (viewport != null) {
	    this.minMz = viewport.minMz;
	    this.maxMz = viewport.maxMz;
	    this.minRt = viewport.minRt;
	    this.maxRt = viewport.maxRt;
	}
    }

    /**
     * Quick constructor. Take care of arguments order ! two firsts arguments: min & max on Mz (Y axis) two
     * lasts arguments: min & max on Rt (X axis)
     */
    public LcMsViewport(double minMzDisp, double maxMzDisp, double minRtDisp, double maxRtDisp) {
	super();
	this.minMz = minMzDisp;
	this.maxMz = maxMzDisp;
	this.minRt = minRtDisp;
	this.maxRt = maxRtDisp;
    }

    /**
     * convert values to pixels using current viewport
     *
     * @param mz
     * @param rt
     * @return
     */
    public Point2D value2pixel(double mz, double rt, int imageWidth, int imageHeight) {
	double x = ((rt - this.minRt) / (this.maxRt - this.minRt)) * imageWidth;
	double y = ((mz - this.minMz) / (this.maxMz - this.minMz)) * imageHeight;
	return new Point2D.Double(x, y);
    }

    /**
     * convert pixel coordinates to values using current viewport
     *
     * @param mz
     * @param rt
     * @return
     */
    public Point2D pixel2value(double x, double y, int imageWidth, int imageHeight) {
	double rt = (((this.maxRt - this.minRt) * x) / imageWidth) + this.minRt;
	double mz = (((this.maxMz - this.minMz) * y) / imageHeight) + this.minMz;
	return new Point2D.Double(mz, rt);
    }

    /**
     * Create a new Viewport translated by given values
     *
     * @return
     */
    public LcMsViewport translate(double tMz, double tRt) {
	return new LcMsViewport(this.minMz + tMz, this.maxMz + tMz, this.minRt + tRt, this.maxRt + tRt);
    }

    /**
     * Create a new Viewport scaled by given values around a center
     *
     * @return
     */
    public LcMsViewport centerScale(double cMz, double cRt, double scaleMz, double scaleRt) {
	return new LcMsViewport(((this.minMz - cMz) * scaleMz) + cMz, ((this.maxMz - cMz) * scaleMz) + cMz,
		((this.minRt - cRt) * scaleRt) + cRt, ((this.maxRt - cRt) * scaleRt) + cRt);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "MassViewport [minMzDisp=" + this.minMz + ", maxMzDisp=" + this.maxMz + ", minRtDisp="
		+ this.minRt + ", maxRtDisp=" + this.maxRt + "]";
    }

}
