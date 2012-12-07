package fr.proline.studio.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jdesktop.swingx.util.PaintUtils;

/**
 *
 * @author JM235353
 */
public class CyclicColorPalette {
    private static Color[] palette = { Color.red, Color.blue, Color.green, Color.yellow, Color.pink, Color.cyan };

    private static HashMap<String, ImageIcon> imageIconMap = null;
    
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
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append(width).append('x').append(height).append('x').append(colorIndex);
            registerKey = sb.toString();
            sb.setLength(0);
        
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
    private static StringBuilder sb = null;
    
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

        if (imageIconMap != null) {
            return imageIconMap.get(registerKey);

        }
        return null;
    }

    private static void registerIcon(String registerKey, ImageIcon i) {
        if (imageIconMap == null) {
            imageIconMap = new HashMap<String, ImageIcon>();
        }
        imageIconMap.put(registerKey, i);
    }

    
}
