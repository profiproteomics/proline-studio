package fr.proline.studio.utils;

import java.awt.Color;

/**
 * Management of a default color palette for the application
 *
 * @author JM235353
 */
public class CyclicColorPalette {

    public static final Color[] DEFAULT_BASE_PALETTE = {
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
    public static final Color GRAY_BACKGROUND = new Color(239, 236, 234);
    public static final Color GRAY_BACKGROUND_DARKER = new Color(229, 226, 224);
    public static final Color GRAY_TEXT_LIGHT = new Color(142, 136, 131);
    public static final Color GRAY_TEXT_DARK = new Color(99, 95, 93);
    public static final Color GRAY_GRID = new Color(229, 226, 224);
    public static final Color GRAY_GRID_LOG = new Color(244, 240, 238);
    public static final Color BLUE_SELECTION_ZONE = new Color(0.2f, 0.2f, 1f, 0.5f);

    /**
     * get color from the default color palette
     * if the colorIndex is too high, the color returned will be of a different brightness
     * than those in the palette
     * @param colorIndex
     * @return 
     */
    public static Color getColor(int colorIndex) {
        return getColor(colorIndex, DEFAULT_BASE_PALETTE);
    }
    
    
    /**
     * get color from the specified color palette
     * if the colorIndex is too high, the color returned will be of a different brightness
     * than those in the palette
     * @param colorIndex
     * @param palette
     * @return 
     */
    public static Color getColor(int colorIndex, Color[] palette) {
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
    
    /**
     * Return a palette created from the default palette with additional colors
     * with a modification of the brightness
     * @return 
     */
    public static Color[] getPalette() {
        int paletteSize = DEFAULT_BASE_PALETTE.length * 3;
        Color[] palette = new Color[paletteSize];
        for (int i=0;i<paletteSize;i++) {
            palette[i] = getColor(i);
        }
        return palette;
    }
    
    /**
     * get color from the default palette with the specified transparency (alpha value)
     * @param colorIndex
     * @param alpha
     * @return 
     */
    public static Color getColor(int colorIndex, int alpha) {
        Color c = getColor(colorIndex);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /**
     * get HTML color from the defautl palette
     * @param colorIndex
     * @return 
     */
    public static String getHTMLColor(int colorIndex) {
        Color c = getColor(colorIndex);
        return getHTMLColor(c);
    }
 
    /**
     * returns HTML color corresponding to the specified color
     * @param color
     * @return 
     */
    public static String getHTMLColor(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

}