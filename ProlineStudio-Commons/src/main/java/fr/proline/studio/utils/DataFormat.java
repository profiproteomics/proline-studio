package fr.proline.studio.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * Useful methods to format data to String
 * 
 * @author JM235353
 */
public class DataFormat {
    
    private final static DecimalFormat m_decimalFormat = new DecimalFormat("###.##");
    
    static {
        DecimalFormatSymbols dfs = m_decimalFormat.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        m_decimalFormat.setDecimalFormatSymbols(dfs);
    }
    
    
     
    public static String format(Float f, int nbFractionDigits) {
        if (f == null) {
            return null;
        }
        return format(f.floatValue(), nbFractionDigits);
    }
    
    public static String format(float f, int nbFractionDigits) {
        
        m_decimalFormat.setMaximumFractionDigits(nbFractionDigits);
        m_decimalFormat.setMinimumFractionDigits(nbFractionDigits);
        
        return m_decimalFormat.format((double) f);
    }
    
        public static String format(Double d, int nbFractionDigits) {
        if (d == null) {
            return null;
        }
        return format(d.doubleValue(), nbFractionDigits);
    }
    
    public static String format(double d, int nbFractionDigits) {

        m_decimalFormat.setMaximumFractionDigits(nbFractionDigits);
        m_decimalFormat.setMinimumFractionDigits(nbFractionDigits);
        
        return m_decimalFormat.format((double) d);
    }
    
    public static String format(Integer i) {
        if (i == null) {
            return null;
        }
        return String.valueOf(i.intValue());
    }
    
    public static String format(int i) {
        return String.valueOf(i);
    }
    
    
    public static String formatWithGroupingSep(Float f, int nbFractionDigits) {
        if (f == null) {
            return null;
        }
        
        DecimalFormat decimalFormat =  new DecimalFormat("###,###.##");
        DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        dfs.setGroupingSeparator(' ');
        decimalFormat.setDecimalFormatSymbols(dfs);
        decimalFormat.setMaximumFractionDigits(nbFractionDigits);
        decimalFormat.setMinimumFractionDigits(nbFractionDigits);
        
        return decimalFormat.format((double) f);
    }
    
    
}
