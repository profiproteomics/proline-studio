package fr.proline.studio.utils;

import java.awt.Color;

/**
 * Management of a default color palette for the application
 *
 * @author JM235353
 */
public class CyclicColorPalette {

    public static final Color[] DEFAULT_BASE_PALETTE = {
        new Color(231, 197, 31), //yellow
        new Color(231, 113, 58), //orange
        new Color(169, 35, 59), //red bordeaux
        new Color(106, 44, 95), //purple bordeaux
        new Color(104, 71, 160), //purple blue
        new Color(98, 126, 206), //blue
        new Color(82, 120, 123), //green blue 
        new Color(63, 121, 58), //green
        new Color(109, 153, 5) //green grace
    };

    public static final Color[] GROUP4_PALETTE = {
        new Color(229, 115, 115), //red 1 
        new Color(129, 212, 250), //blue 1 
        new Color(255, 213, 79),  //yellow 1 
        new Color(76, 175, 80),   //Greeen 1 
        new Color(183, 28, 28),   //red 2 
        new Color(3, 155, 229),   //blue 2 
        new Color(255, 179, 0),   //yellow 2 
        new Color(27, 94, 32),    //Greeen 2 
        new Color(255, 23, 68),   //red 3 
        new Color(1, 87, 155),    //blue 3 
        new Color(255, 234, 0),   //yellow 3
        new Color(0, 200, 83),    //Greeen 3 
    };

    public static final Color GRAY_BACKGROUND = new Color(239, 236, 234);
    public static final Color GRAY_BACKGROUND_DARKER = new Color(229, 226, 224);
    public static final Color GRAY_TEXT_LIGHT = new Color(142, 136, 131);
    public static final Color GRAY_TEXT_DARK = new Color(99, 95, 93);
    public static final Color GRAY_GRID = new Color(229, 226, 224);
    public static final Color GRAY_GRID_LOG = new Color(244, 240, 238);
    public static final Color BLUE_SELECTION_ZONE = new Color(0.2f, 0.2f, 1f, 0.5f);
    public static final Color GRAY_DARK = new Color(47, 43, 42);

    /**
     * get color from the default color palette if the colorIndex is too high,
     * the color returned will be of a different brightness than those in the
     * palette
     *
     * @param colorIndex
     * @return
     */
    public static Color getColor(int colorIndex) {
        return getColor(colorIndex, DEFAULT_BASE_PALETTE);
    }

    public static Color getColorBlue(int colorIndex) {
        int q = colorIndex % GROUP4_PALETTE.length;
        return GROUP4_PALETTE[q];
    }

    /**
     * get color from the specified color palette if the colorIndex is too high,
     * the color returned will be of a different brightness than those in the
     * palette
     *
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
     *
     * @return
     */
    public static Color[] getPalette() {
        int paletteSize = DEFAULT_BASE_PALETTE.length * 3;
        Color[] palette = new Color[paletteSize];
        for (int i = 0; i < paletteSize; i++) {
            palette[i] = getColor(i);
        }
        return palette;
    }

    /**
     * Make a color darker.
     *
     * @param color Color to make darker.
     * @param fraction Darkness fraction. betxeen 0-1
     * @return Darker color.
     */
    public static Color getDarkerColor(Color color, double fraction) {
        int red = (int) Math.round(color.getRed() * (1.0 - fraction));
        int green = (int) Math.round(color.getGreen() * (1.0 - fraction));
        int blue = (int) Math.round(color.getBlue() * (1.0 - fraction));

        if (red < 0) {
            red = 0;
        } else if (red > 255) {
            red = 255;
        }
        if (green < 0) {
            green = 0;
        } else if (green > 255) {
            green = 255;
        }
        if (blue < 0) {
            blue = 0;
        } else if (blue > 255) {
            blue = 255;
        }

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);
    }

    /**
     * get color from the default palette with the specified transparency (alpha
     * value)
     *
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
     *
     * @param colorIndex
     * @return
     */
    public static String getHTMLColor(int colorIndex) {
        Color c = getColor(colorIndex);
        return getHTMLColor(c);
    }

    /**
     * returns HTML color corresponding to the specified color
     *
     * @param color
     * @return
     */
    public static String getHTMLColor(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

}
