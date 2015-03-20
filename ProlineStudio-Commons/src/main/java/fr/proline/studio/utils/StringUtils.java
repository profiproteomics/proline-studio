/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
}
