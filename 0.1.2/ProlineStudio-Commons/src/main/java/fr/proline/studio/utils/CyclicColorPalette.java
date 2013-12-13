package fr.proline.studio.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * To create and combine icons from a color palette
 * @author JM235353
 */
public class CyclicColorPalette {
    private static Color[] palette = { Color.red, Color.blue, Color.green, Color.yellow, Color.pink, Color.cyan };

    private static HashMap<String, ImageIcon> m_imageIconMap = null;
    
    public static Color getColor(int colorIndex) {
        int paletteSize = palette.length*2;
        colorIndex = colorIndex % paletteSize;
        if (colorIndex<palette.length) {
            return palette[colorIndex];
        }
        return PaintUtils.setSaturation(palette[colorIndex-palette.length], .7f);
    }
    
    public static ImageIcon getColoredImageIcon(int width, int height, int colorIndex, boolean registerImage) {
        
        String registerKey = null;
        if (registerImage) {
            if (m_sb == null) {
                m_sb = new StringBuilder();
            }
            m_sb.append(width).append('x').append(height).append('x').append(colorIndex);
            registerKey = m_sb.toString();
            m_sb.setLength(0);
        
            ImageIcon imageIcon = getRegisteredIcon(registerKey);
            if (imageIcon != null) {
                return imageIcon;
            }
        }
        
        Color c = getColor(colorIndex); 
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
	g.setColor(c);
	g.fillRect(1, 1, width-2, height-2);
        g.setColor(Color.black);
        g.drawRect(0, 0, width-1, height-1);
        
        ImageIcon imageIcon = new ImageIcon(image);
        
        if (registerImage) {
            registerIcon(registerKey, imageIcon);
        }
        
        return imageIcon;
    }
    private static StringBuilder m_sb = null;
    
    public static ImageIcon getCombinedImageIcon(Icon leftIcon, Icon rightIcon, int hGap, String registerKey) {
        
        if (registerKey != null) {
            ImageIcon imageIcon = getRegisteredIcon(registerKey);
            if (imageIcon != null) {
                return imageIcon;
            }
        }
        
        int width = leftIcon.getIconWidth() + rightIcon.getIconWidth() + hGap;
        int height = Math.max(leftIcon.getIconHeight(), rightIcon.getIconHeight());

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width - 1, height - 1);


        leftIcon.paintIcon(null, g, 0, 0);
        rightIcon.paintIcon(null, g, leftIcon.getIconWidth() + hGap, 0);

        ImageIcon imageIcon = new ImageIcon(image);

        if (registerKey != null) {
            registerIcon(registerKey, imageIcon);
        }
        
        return imageIcon;
       
    }
    
    private static ImageIcon getRegisteredIcon(String registerKey) {

        if (m_imageIconMap != null) {
            return m_imageIconMap.get(registerKey);

        }
        return null;
    }

    private static void registerIcon(String registerKey, ImageIcon i) {
        if (m_imageIconMap == null) {
            m_imageIconMap = new HashMap<>();
        }
        m_imageIconMap.put(registerKey, i);
    }

    
}
