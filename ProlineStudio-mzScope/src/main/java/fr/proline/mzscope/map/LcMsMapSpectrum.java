/**
 *
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