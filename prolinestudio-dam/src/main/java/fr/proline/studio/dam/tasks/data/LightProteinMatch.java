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

public class LightProteinMatch {

    private long m_id;
    private String m_accession;
    private final Float m_score;

    private long m_proteinSetId;
    
    private long m_representativeProteinMatchId; // typical Protein of the Protein Set which is in the sameset
    
    private long m_identicalProteinInSet; // Identical protein corresponding to the same peptides (case of subset)
    
    

    
    public LightProteinMatch(long id, String accession, Float score, long proteinSetId, long representativeProteinMatchId) {
        m_id = id;
        m_accession = accession;
        m_score = score;
        m_proteinSetId = proteinSetId;
        m_representativeProteinMatchId = representativeProteinMatchId;
        m_identicalProteinInSet = -1;
    }

    public long getProteinSetId() {
        return m_proteinSetId;
    }

    public long getId() {
        return m_id;
    }

    public void setId(final long pId) {
        m_id = pId;
    }

    public String getAccession() {
        return m_accession;
    }

    public void setAccession(String accession) {
        m_accession = accession;
    }

    public Float getScore() {
        return m_score;
    }

    public long getRepresentativeProteinMatchId() {
        return m_representativeProteinMatchId;
    }
    
    public void setIdenticalProteinInSet(long id) {
        m_identicalProteinInSet = id;
    }
    
     public long getIdenticalProteinInSet() {
        return m_identicalProteinInSet;
    }
}
