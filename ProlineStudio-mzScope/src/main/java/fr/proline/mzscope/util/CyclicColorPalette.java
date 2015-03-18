/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.util;

import java.awt.Color;

/**
 * To create and combine icons from a color palette
 *
 * @author JM235353
 */
public class CyclicColorPalette {

    public static final Color[] palette = {
        new Color(231, 197, 31),
        new Color(231, 113, 58),
        new Color(169, 35, 59),
        new Color(106, 44, 95),
        new Color(104, 71, 160),
        new Color(98, 126, 206),
        new Color(82, 120, 123),
        new Color(63, 121, 58),
        new Color(109, 153, 5)
    };
    public static Color GRAY_BACKGROUND = new Color(239, 236, 234);
    public static Color GRAY_BACKGROUND_DARKER = new Color(229, 226, 224);
    public static Color GRAY_TEXT_LIGHT = new Color(142, 136, 131);
    public static Color GRAY_TEXT_DARK = new Color(99, 95, 93);
    public static Color GRAY_GRID = new Color(229, 226, 224);
    public static Color GRAY_GRID_LOG = new Color(244, 240, 238);
    public static Color BLUE_SELECTION_ZONE = new Color(0.2f, 0.2f, 1f, 0.5f);

    public static Color getColor(int colorIndex) {
        int paletteSize = palette.length * 3;
        colorIndex = colorIndex % paletteSize;
        if (colorIndex < palette.length) {
            return palette[colorIndex];
        }
        int q = colorIndex / palette.length;
        int sign = ((q % 2 == 0) ? +1 : -1);
        float[] hsb = Color.RGBtoHSB(palette[colorIndex - q * palette.length].getRed(),
                palette[colorIndex - q * palette.length].getGreen(),
                palette[colorIndex - q * palette.length].getBlue(), null);
        float brightness = hsb[2] + sign * 0.17f;
        brightness = Math.max(0.0f, brightness);
        brightness = Math.min(brightness, 1.0f);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], brightness));
    }
    
    public static Color getColor(int colorIndex, int alpha) {
        Color c = getColor(colorIndex);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static String getHTMLColor(int colorIndex) {
        Color c = getColor(colorIndex);
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}

