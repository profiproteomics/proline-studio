package fr.proline.studio.utils;

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
    
    /*
     * no longer used for the moment
     *
     *
     * public static ImageIcon getColoredImageIcon(int width, int height, int colorIndex, boolean registerImage) {
     *
     * String registerKey = null; if (registerImage) { if (m_sb == null) { m_sb = new StringBuilder(); }
     * m_sb.append(width).append('x').append(height).append('x').append(colorIndex); registerKey = m_sb.toString(); m_sb.setLength(0);
     *
     * ImageIcon imageIcon = getRegisteredIcon(registerKey); if (imageIcon != null) { return imageIcon; } }
     *
     * Color c = getColor(colorIndex);      *
     * BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); Graphics2D g = image.createGraphics(); g.setColor(c);
     * g.fillRect(1, 1, width-2, height-2); g.setColor(Color.black); g.drawRect(0, 0, width-1, height-1);
     *
     * ImageIcon imageIcon = new ImageIcon(image);
     *
     * if (registerImage) { registerIcon(registerKey, imageIcon); }
     *
     * return imageIcon; } private static StringBuilder m_sb = null;
     *
     * public static ImageIcon getCombinedImageIcon(Icon leftIcon, Icon rightIcon, int hGap, String registerKey) {
     *
     * if (registerKey != null) { ImageIcon imageIcon = getRegisteredIcon(registerKey); if (imageIcon != null) { return imageIcon; } }
     *
     * int width = leftIcon.getIconWidth() + rightIcon.getIconWidth() + hGap; int height = Math.max(leftIcon.getIconHeight(),
     * rightIcon.getIconHeight());
     *
     * BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); Graphics2D g = image.createGraphics(); g.setColor(new
     * Color(0, 0, 0, 0)); g.fillRect(0, 0, width - 1, height - 1);
     *
     *
     * leftIcon.paintIcon(null, g, 0, 0); rightIcon.paintIcon(null, g, leftIcon.getIconWidth() + hGap, 0);
     *
     * ImageIcon imageIcon = new ImageIcon(image);
     *
     * if (registerKey != null) { registerIcon(registerKey, imageIcon); }
     *
     * return imageIcon;
     *
     * }
     *
     * private static ImageIcon getRegisteredIcon(String registerKey) {
     *
     * if (m_imageIconMap != null) { return m_imageIconMap.get(registerKey);
     *
     * }
     * return null; }
     *
     * private static void registerIcon(String registerKey, ImageIcon i) { if (m_imageIconMap == null) { m_imageIconMap = new HashMap<>(); }
     * m_imageIconMap.put(registerKey, i); }
     *
     *
     */
}
