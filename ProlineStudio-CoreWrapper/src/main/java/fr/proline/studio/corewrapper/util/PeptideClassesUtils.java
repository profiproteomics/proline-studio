/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.corewrapper.util;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

/**
 *
 * @author VD225637
 */
public class PeptideClassesUtils {
    
    public static final double PROTON_MASS =1.007825; //1.007276466812;
    public static final double PEPTIDE_ISOTOPE_SHIFT=1.00335;
    final static double EXP_CSTE = StrictMath.pow(10, 6);
            
    public static Float getPPMFor(DPeptideMatch pepMatch, Peptide peptide) {
        double deltaMoz = (double) pepMatch.getDeltaMoz();
        double calculatedMass = peptide.getCalculatedMass() ;
        double charge = (double) pepMatch.getCharge(); 
        float ppm = (float) ((deltaMoz*EXP_CSTE) / ( (calculatedMass+(PEPTIDE_ISOTOPE_SHIFT * pepMatch.getIsotopeOffset()) + (charge * PROTON_MASS))/charge));
                
            if ((calculatedMass >= -1e-10) && (calculatedMass <= 1e-10)) {
                //calculatedMass == 0; // it was a bug, does no longer exist, but 0 values can exist in database.
                ppm = 0;
            }
        return ppm;
    }
}
