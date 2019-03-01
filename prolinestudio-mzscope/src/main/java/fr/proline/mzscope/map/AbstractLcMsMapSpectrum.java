/**
 *
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