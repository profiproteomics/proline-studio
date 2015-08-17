package fr.proline.studio.utils;

import java.awt.FontMetrics;

/**
 * utils for String
 * @author MB243701
 */
public class StringUtils {
    
    /**
     * truncates the given text with nbCharac, starting from end
     * @param text
     * @param nbCharac 
     * @return  
     */
    public final static String truncate (String text, int nbCharac) {
        if (text == null) {
            return null;
        }
        int textSize = text.length() ;
        if (nbCharac > textSize) {
            return text;
        }
        return text.substring(textSize-nbCharac);
    }
    
    /* returns the pixel-lenght of a string */
    public static int lenghtOfString(String s, FontMetrics fm) {
       if (fm == null || s == null)
            return 0;
       else 
           return fm.stringWidth(s);
    }
    
    /**
     * return the action's name suffixed with JMS if needed
     * @param s
     * @param isJMSDefined
     * @return 
     */
    public static String getActionName(String s, boolean isJMSDefined) {
        return s+ (isJMSDefined?" (JMS)":"");
    }
}
