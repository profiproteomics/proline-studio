package fr.proline.studio.utils;

import java.awt.Color;
import org.jdesktop.swingx.util.PaintUtils;

/**
 *
 * @author JM235353
 */
public class CyclicColorPalette {
    private static Color[] palette = { Color.red, Color.blue, Color.green, Color.yellow, Color.pink, Color.cyan };

    public static Color getColor(int index) {
        int paletteSize = palette.length*2;
        index = index % paletteSize;
        if (index<palette.length) {
            return palette[index];
        }
        return PaintUtils.setSaturation(palette[index-palette.length], .7f);
    }
}
