/**
 *
 */
package fr.proline.mzscope.map.color;

import java.awt.Color;

/**
 * Convert an intensity in color
 *
 * @author JeT
 */
public interface IntensityPainter {

    /**
     * @param intensity
     *            0..1 value
     * @return
     */
    Color getColor(double intensity);

}
