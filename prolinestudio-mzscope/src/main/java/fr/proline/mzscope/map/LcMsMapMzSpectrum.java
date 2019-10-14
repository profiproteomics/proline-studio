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

import fr.profi.mzdb.model.SpectrumSlice;

/**
 * @author JeT
 *
 */
public class LcMsMapMzSpectrum extends AbstractLcMsMapSpectrum {
    private double[] intensities = null;
    private double maxIntensity = 0;

    /**
     * @param chunk
     * @param sliceSizeMz
     */
    public LcMsMapMzSpectrum(LcMsMapChunk chunk, double sliceSizeMz) {
	super(chunk.getViewport().minMz, chunk.getViewport().maxMz, sliceSizeMz);
	this.intensities = new double[this.getSliceCount()];
	for (SpectrumSlice slice : chunk.getSlices()) {
	    double rt = slice.getHeader().getElutionTime();
	    if ((rt < chunk.getViewport().minRt) || (rt > chunk.getViewport().maxRt)) {
		continue;
	    }
	    for (int dataIndex = 0; dataIndex < slice.getData().getIntensityList().length; dataIndex++) {
		double mz = slice.getData().getMzList()[dataIndex];
		double intensity = slice.getData().getIntensityList()[dataIndex];
		if ((mz < chunk.getViewport().minMz) || (mz > chunk.getViewport().maxMz)) {
		    continue;
		}
		int mzIndex = this.getMzIndex(mz);
		if ((mzIndex < 0) || (mzIndex >= this.intensities.length)) {
		    System.out.println("mz index " + mzIndex + " out of bounds " + this.intensities.length);
		} else {
		    this.intensities[mzIndex] += intensity;
		    this.maxIntensity = Math.max(this.maxIntensity, this.intensities[mzIndex]);
		}
	    }
	}
    }

    /**
     * @return the intensities
     */
    @Override
    public final double[] getIntensities() {
	return this.intensities;
    }

    /**
     * @return the intensity at a given index
     */
    @Override
    public final double getIntensity(final int rtIndex) throws IndexOutOfBoundsException {
	return this.intensities[rtIndex];
    }

    /**
     * @return the maxIntensity
     */
    @Override
    public final double getMaxIntensity() {
	return this.maxIntensity;
    }

    private int getMzIndex(double mz) {
	return (int) ((mz - this.getMinX()) / this.getSliceSize());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.LcMsMapSpectrum#getIndex(double)
     */
    @Override
    public int getIndex(double xValue) {
	return this.getMzIndex(xValue);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.LcMsMapSpectrum#getIntensity(double)
     */
    @Override
    public double getIntensity(final double mz) {
	return this.intensities[this.getMzIndex(mz)];
    }

}
