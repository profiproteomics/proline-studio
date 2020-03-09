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
    
    /**
     * 
     * @param petideIonMoz  
     * @param petideIonCharge
     * @param isotopRank  from 0
     * @return 
     */
    public static double getIsotopMoz(double petideIonMoz, int petideIonCharge, int isotopRank) {
        return petideIonMoz + (PROTON_MASS / petideIonCharge)*(isotopRank);
    }

}
