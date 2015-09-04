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
    
    public AdjacencyMatrixData() {
    }
    
    public void setData(ArrayList<LightProteinMatch> proteins, ArrayList<LightPeptideMatch> peptides, HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> proteinToPeptideMap, HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap) {
        m_proteins = proteins;
        m_peptides = peptides;
        m_proteinToPeptideMap = proteinToPeptideMap;
        m_peptideToProteinMap = peptideToProteinMap;
    }
    
    public ArrayList<LightProteinMatch> get_ProtineList() {
        return m_proteins;
    }

    public ArrayList<LightPeptideMatch> get_PeptideList() {
        return m_peptides;
    }

    public HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> get_proteinToPeptideMap() {
        return m_proteinToPeptideMap;
    }

    public HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> get_peptideToProteinMap() {
        return m_peptideToProteinMap;
    }
    
}
