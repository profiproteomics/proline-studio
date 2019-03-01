/**
 *
 */
package fr.proline.mzscope.map.color;

import java.awt.Color;

/**
 * @author JeT
 *
 */
public class IntensityGrayPainter implements IntensityPainter {

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.color.IntensityPainter#getColor(double)
     */
    @Override
    public Color getColor(double intensity) {
	float v = (float) (Math.max(0, Math.min(1, intensity)));

	return new Color(v, v, v);
    }

}
