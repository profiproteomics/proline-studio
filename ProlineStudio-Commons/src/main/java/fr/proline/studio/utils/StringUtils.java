package fr.proline.studio.utils;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    public static List<String> splitString(String msg, int lineSize) {
        List<String> res = new ArrayList<>();
        Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);
	while(m.find()) {
                System.out.println(m.group().trim());   // Debug
                res.add(m.group());
        }
        return res;
    }

    public static String formatString(String msg, int lineSize) {
        Pattern p = Pattern.compile(".{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);
        StringBuilder builder = new StringBuilder();
        while(m.find()) {
            if (builder.length() != 0) builder.append("\n");
            builder.append(m.group());
        }
        return builder.toString();
    }
        

}
