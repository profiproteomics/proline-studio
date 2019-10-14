/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
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
    
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");
    private final static DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat("#.##E0");
    
    static {
        DecimalFormatSymbols dfs = DECIMAL_FORMAT.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DECIMAL_FORMAT.setDecimalFormatSymbols(dfs);
        
        dfs = SCIENTIFIC_FORMAT.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        SCIENTIFIC_FORMAT.setDecimalFormatSymbols(dfs);
    }
    
    
     
    public static String format(Float f, int nbFractionDigits) {
        if (f == null) {
            return null;
        }
        return format(f.floatValue(), nbFractionDigits);
    }
    
    public static String format(float f, int nbFractionDigits) {
        
        DECIMAL_FORMAT.setMaximumFractionDigits(nbFractionDigits);
        DECIMAL_FORMAT.setMinimumFractionDigits(nbFractionDigits);
        
        return DECIMAL_FORMAT.format((double) f);
    }
    
    public static String format(Double d, int nbFractionDigits) {
        if (d == null) {
            return null;
        }
        return format(d.doubleValue(), nbFractionDigits);
    }
    
    public static String format(double d, int nbFractionDigits) {

        DECIMAL_FORMAT.setMaximumFractionDigits(nbFractionDigits);
        DECIMAL_FORMAT.setMinimumFractionDigits(nbFractionDigits);
        
        return DECIMAL_FORMAT.format((double) d);
    }
    
    public static String formatScientific(Float f, int nbFractionDigits) {
        if (f == null) {
            return null;
        }
        return formatScientific(f.floatValue(), nbFractionDigits);
    }
    
    public static String formatScientific(float f, int nbFractionDigits) {
        
        SCIENTIFIC_FORMAT.setMaximumFractionDigits(nbFractionDigits);
        SCIENTIFIC_FORMAT.setMinimumFractionDigits(nbFractionDigits);
        
        return SCIENTIFIC_FORMAT.format((double) f);
    }
    
    public static String formatScientific(Double d, int nbFractionDigits) {
        if (d == null) {
            return null;
        }
        return formatScientific(d.doubleValue(), nbFractionDigits);
    }
    
    public static String formatScientific(double d, int nbFractionDigits) {

        SCIENTIFIC_FORMAT.setMaximumFractionDigits(nbFractionDigits);
        SCIENTIFIC_FORMAT.setMinimumFractionDigits(nbFractionDigits);
        
        return SCIENTIFIC_FORMAT.format((double) d);
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
    
    
    public static String formatWithGroupingSep(Number f, int nbFractionDigits) {
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
        
        return decimalFormat.format(f.doubleValue());
    }
    
    
    
}
