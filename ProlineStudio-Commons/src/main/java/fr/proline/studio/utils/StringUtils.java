/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.utils;

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
}
