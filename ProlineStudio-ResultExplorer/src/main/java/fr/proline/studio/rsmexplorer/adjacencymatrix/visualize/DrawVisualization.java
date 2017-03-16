package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import java.util.ArrayList;
import java.util.HashMap;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;

public class DrawVisualization {

    private AdjacencyMatrixData m_adjacencyMatrixData = null;


    private ArrayList<Component> m_cList;
    private ConnectedComponents m_cObject;

    public void setData(AdjacencyMatrixData adjacencyMatrixData, boolean keepSameSet) {
        m_adjacencyMatrixData = adjacencyMatrixData;

        m_cObject = new ConnectedComponents(getProtineList(), getPeptideList(), getProteinToPeptideMap(), getPeptideToProteinMap(), getEquivalentProteins());
        m_cList = m_cObject.getConnectedComponents(keepSameSet);
    }

    public int getLargestComponent() {
        int maxIndex = m_cObject.getLargestComponent();
        return maxIndex;
    }

    public ArrayList<Component> getComponentList() {
        return m_cList;
    }

    private ArrayList<LightProteinMatch> getProtineList() {
        return m_adjacencyMatrixData.getProtineList();
    }

    private ArrayList<LightPeptideMatch> getPeptideList() {
        return m_adjacencyMatrixData.getPeptideList();
    }

    public HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> getProteinToPeptideMap() {
        return m_adjacencyMatrixData.getProteinToPeptideMap();
    }

    public HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> getPeptideToProteinMap() {
        return m_adjacencyMatrixData.getPeptideToProteinMap();
    }
    
    public HashMap<LightProteinMatch, ArrayList<LightProteinMatch>> getEquivalentProteins() {
        return m_adjacencyMatrixData.getEquivalentProteins();
    }

    public HashMap<LightProteinMatch, LightProteinMatch> getEquivalentToMainProtein() {
        return m_adjacencyMatrixData.getEquivalentToMainProtein();
    }
}
