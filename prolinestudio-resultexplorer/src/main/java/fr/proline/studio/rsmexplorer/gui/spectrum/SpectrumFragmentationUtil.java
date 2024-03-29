/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public class SpectrumFragmentationUtil {
    
    private static HashMap<Character, Double> m_aaMassHashMap = null;

    public static double getMassFromAminoAcid(char aa) {
        if (m_aaMassHashMap == null) {
            m_aaMassHashMap = new HashMap<>();
            m_aaMassHashMap.put('A', (double) 71.03711);
            m_aaMassHashMap.put('C', (double) 103.00919);
            m_aaMassHashMap.put('D', (double) 115.02694);
            m_aaMassHashMap.put('E', (double) 129.04259);
            m_aaMassHashMap.put('F', (double) 147.06841);
            m_aaMassHashMap.put('G', (double) 57.02146);
            m_aaMassHashMap.put('H', (double) 137.05891);
            m_aaMassHashMap.put('I', (double) 113.08406);
            m_aaMassHashMap.put('K', (double) 128.09496);
            m_aaMassHashMap.put('L', (double) 113.08406);
            m_aaMassHashMap.put('M', (double) 131.04049);
            m_aaMassHashMap.put('N', (double) 114.04293);
            m_aaMassHashMap.put('P', (double) 97.05276);
            m_aaMassHashMap.put('Q', (double) 128.05858);
            m_aaMassHashMap.put('R', (double) 156.10111);
            m_aaMassHashMap.put('S', (double) 87.03203);
            m_aaMassHashMap.put('T', (double) 101.04768);
            m_aaMassHashMap.put('V', (double) 99.06841);
            m_aaMassHashMap.put('W', (double) 186.07931);
            m_aaMassHashMap.put('Y', (double) 163.06333);
            m_aaMassHashMap.put('?', (double) 100);
        }
        return m_aaMassHashMap.get(aa);
    }


}
