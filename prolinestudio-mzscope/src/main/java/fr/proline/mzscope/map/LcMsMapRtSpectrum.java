/**
 *
 */
package fr.proline.mzscope.map;

import fr.profi.mzdb.model.SpectrumSlice;

/**
 * @author JeT
 *
 */
public class LcMsMapRtSpectrum extends AbstractLcMsMapSpectrum {
    private double[] intensities = null;
    private double maxIntensity = 0;

    /**
     * @param chunk
     * @param sliceSizeRt
     */
    public LcMsMapRtSpectrum(LcMsMapChunk chunk, double sliceSizeRt) {
	super(chunk.getViewport().minRt, chunk.getViewport().maxRt, sliceSizeRt);
	double widthRt = chunk.getViewport().maxRt - chunk.getViewport().minRt;
	this.intensities = new double[this.getSliceCount()];
	for (SpectrumSlice slice : chunk.getSlices()) {
	    double rt = slice.getHeader().getElutionTime();
	    if ((rt < chunk.getViewport().minRt) || (rt > chunk.getViewport().maxRt)) {
		continue;
	    }
	    int rtIndex = this.getRtIndex(rt);
	    if ((rtIndex < 0) || (rtIndex >= this.intensities.length)) {
		System.out.println("rt index " + rtIndex + " out of bounds " + this.intensities.length);
	    } else {
		for (int dataIndex = 0; dataIndex < slice.getData().getIntensityList().length; dataIndex++) {
		    double mz = slice.getData().getMzList()[dataIndex];
		    double intensity = slice.getData().getIntensityList()[dataIndex];
		    if ((mz < chunk.getViewport().minMz) || (mz > chunk.getViewport().maxMz)) {
			continue;
		    }
		    this.intensities[rtIndex] += intensity;
		    this.maxIntensity = Math.max(this.maxIntensity, this.intensities[rtIndex]);
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

    /**
     * @param rt
     *            elution time
     * @return
     */
    private int getRtIndex(double rt) {
	return (int) ((rt - this.getMinX()) / this.getSliceSize());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.LcMsMapSpectrum#getIndex(double)
     */
    @Override
    public int getIndex(double xValue) {
	return this.getRtIndex(xValue);
    }

    /**
     * @return the intensity at a given elution time
     */
    @Override
    public double getIntensity(final double rt) {
	return this.intensities[this.getRtIndex(rt)];
    }

}
