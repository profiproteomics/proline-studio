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

        m_cObject = new ConnectedComponents(get_ProtineList(), get_PeptideList(), getProteinToPeptideMap(), getPeptideToProteinMap());
        m_cList = m_cObject.getConnectedComponents(keepSameSet);
    }

    public int getLargestComponent() {
        int maxIndex = m_cObject.getLargestComponent();
        return maxIndex;
    }

    public ArrayList<Component> get_ComponentList() {
        return m_cList;
    }

    public ArrayList<LightProteinMatch> get_ProtineList() {
        return m_adjacencyMatrixData.get_ProtineList();
    }

    public ArrayList<LightPeptideMatch> get_PeptideList() {
        return m_adjacencyMatrixData.get_PeptideList();
    }

    public HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> getProteinToPeptideMap() {
        return m_adjacencyMatrixData.get_proteinToPeptideMap();
    }

    public HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> getPeptideToProteinMap() {
        return m_adjacencyMatrixData.get_peptideToProteinMap();
    }

}
