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
package fr.proline.studio.dam.tasks.data;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author JM235353
 */
public class WeakPeptideReference {
    
    private int m_peptideIndex;
    private Set<Long> m_proteinSetIds = new HashSet<>();
    
    public WeakPeptideReference(int peptideIndex, long proteinSet1, long proteinSet2) {
        m_peptideIndex = peptideIndex;
        m_proteinSetIds.add(proteinSet1);
        m_proteinSetIds.add(proteinSet2);
    }
    
    public void add(long proteinSet1, long proteinSet2) {
        m_proteinSetIds.add(proteinSet1);
        m_proteinSetIds.add(proteinSet2);
    }
    
    public int getPeptideIndex() {
        return m_peptideIndex;
    }
    
    public boolean correspondToProteinSet(long proteinSetId) {
        return m_proteinSetIds.contains(proteinSetId);
    }
}
