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

/**
 *
 * @author JM235353
 */
public class LightPeptideMatch {
    private final long m_id;
    private final Float m_score;

    private String m_sequence;
    
    private LightProteinMatch[] m_proteinMatchArray = null;


    private Integer m_cdPrettyRank;

    public LightPeptideMatch(long id, Float score, Integer cdPrettyRank, String sequence) {
        m_id = id;
        m_score = score;
        m_proteinMatchArray = null;
        
        m_cdPrettyRank = cdPrettyRank;
        m_sequence = sequence;
    }
    
    public long getId() {
        return m_id;
    }
    
    public void setSequence(String sequence) {
    	m_sequence = sequence;
    }
    
    public String getSequence() {
        return m_sequence;
    }

    public Float getScore() {
        return m_score;
    }

    public Integer getCDPrettyRank() {
    	return m_cdPrettyRank;
    }

    public LightProteinMatch[] getProteinMatches() {
        return m_proteinMatchArray;
    }

    public void setProteinMatches(LightProteinMatch[] proteinMatchArray) {
        m_proteinMatchArray = proteinMatchArray;
    }

}
