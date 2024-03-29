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
package fr.proline.studio.dam.tasks.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public class AdjacencyMatrixData {
    
    private ArrayList<LightProteinMatch> m_proteins = null;
    private ArrayList<LightPeptideMatch> m_peptides = null;

    private HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> m_proteinToPeptideMap = null;
    private HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = null;
    
    private HashMap<LightProteinMatch, ArrayList<LightProteinMatch>> m_equivalentProteins = null;
    HashMap<LightProteinMatch, LightProteinMatch> m_equivalentToMainProtein = null;
    
    public AdjacencyMatrixData() {
    }
    
    public void setData(ArrayList<LightProteinMatch> proteins, ArrayList<LightPeptideMatch> peptides, HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> proteinToPeptideMap, HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap, HashMap<LightProteinMatch, ArrayList<LightProteinMatch>> equivalentProteins, HashMap<LightProteinMatch, LightProteinMatch> equivalentToMainProtein) {
        m_proteins = proteins;
        m_peptides = peptides;
        m_proteinToPeptideMap = proteinToPeptideMap;
        m_peptideToProteinMap = peptideToProteinMap;
        m_equivalentProteins = equivalentProteins;
        m_equivalentToMainProtein = equivalentToMainProtein;
    }
    
    public ArrayList<LightProteinMatch> getProtineList() {
        return m_proteins;
    }

    public ArrayList<LightPeptideMatch> getPeptideList() {
        return m_peptides;
    }

    public HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> getProteinToPeptideMap() {
        return m_proteinToPeptideMap;
    }

    public HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> getPeptideToProteinMap() {
        return m_peptideToProteinMap;
    }
    
    public HashMap<LightProteinMatch, ArrayList<LightProteinMatch>> getEquivalentProteins() {
        return m_equivalentProteins;
    }
    
    public HashMap<LightProteinMatch, LightProteinMatch> getEquivalentToMainProtein() {
        return m_equivalentToMainProtein;
    }
}
