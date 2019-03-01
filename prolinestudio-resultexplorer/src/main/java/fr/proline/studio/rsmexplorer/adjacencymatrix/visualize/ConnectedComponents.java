/* Computes the Connected Components from the data.
 * Using Peptide->protein EdgeSet to find Connected Components
 */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import java.util.HashSet;

public class ConnectedComponents {

    private ArrayList<LightProteinMatch> m_proteins = new ArrayList<>();
    private ArrayList<LightPeptideMatch> m_peptides = new ArrayList<>();
    private HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> m_proteinToPeptideMap = new HashMap<>();
    private HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap = new HashMap<>();
    private HashMap<LightProteinMatch, ArrayList<LightProteinMatch>> m_equivalentProteins = null;
    
    
    private int m_maxIndex = 0, m_maxSize = 0;

    public ConnectedComponents(ArrayList<LightProteinMatch> proteins, ArrayList<LightPeptideMatch> peptides, HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> proteinToPeptideMap, HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap, HashMap<LightProteinMatch, ArrayList<LightProteinMatch>> equivalentProteins) {
        m_proteins = proteins;
        m_peptides = peptides;
        m_proteinToPeptideMap = proteinToPeptideMap;
        m_peptideToProteinMap = peptideToProteinMap;
        m_equivalentProteins = equivalentProteins;
    }

    public int getLargestComponent() {
        return m_maxIndex;
    }

    public ArrayList<Component> getConnectedComponents(boolean keepSameSet) {

        int proteinIndex = 0;
        int peptideIndex = 0;
        int componentIndex = 0;


        int proteinSize = m_proteins.size();
        int peptideSize = m_peptides.size();

        HashSet<LightProteinMatch> proteinFlagSet = new HashSet<>(proteinSize);
        HashSet<LightPeptideMatch> peptideFlagSet = new HashSet<>(peptideSize);
        
        Queue<LightProteinMatch> proteinQueue = new LinkedList<>();
        Queue<LightPeptideMatch> peptideQueue = new LinkedList<>();


        ArrayList<Component> componentSet = new ArrayList<>();
        
        LightProteinMatch proteinTemp = m_proteins.get(0);
        LightPeptideMatch peptideTemp = m_peptides.get(0);

        proteinQueue.add(proteinTemp);
        proteinFlagSet.add(proteinTemp);

        componentIndex = -1;

        while ((proteinIndex < proteinSize) && (peptideIndex < peptideSize)) {
            componentIndex++;
            Component componentNull = new Component();
            componentNull.m_equivalentProteins = m_equivalentProteins;
            componentSet.add(componentNull);

            while (!(proteinQueue.isEmpty() && peptideQueue.isEmpty())) {

                while (!proteinQueue.isEmpty()) {

                    proteinTemp = proteinQueue.remove();
                    ArrayList<LightPeptideMatch> peptideList = m_proteinToPeptideMap.get(proteinTemp);

                    for (LightPeptideMatch temp1 : peptideList) {
                        if (!peptideFlagSet.contains(temp1)) {
                            peptideQueue.add(temp1);
                            peptideFlagSet.add(temp1);
                        }
                    }

                    componentSet.get(componentIndex).getProteinArray(false).add(proteinTemp);

                }

                while (!peptideQueue.isEmpty()) {
                    peptideTemp = peptideQueue.remove();
                    ArrayList<LightProteinMatch> proteinList = m_peptideToProteinMap.get(peptideTemp);

                    for (LightProteinMatch temp2 : proteinList) {
                        if (!proteinFlagSet.contains(temp2)) {
                            proteinQueue.add(temp2);
                            proteinFlagSet.add(temp2);
                        }
                    }

                    componentSet.get(componentIndex).getPeptideArray().add(peptideTemp);

                }

            }

            int tempCheckSize = componentSet.get(componentIndex).getProteinSize(false)+componentSet.get(componentIndex).getPeptideSize();


            if (tempCheckSize > m_maxSize) {
                m_maxIndex = componentIndex;
                m_maxSize = tempCheckSize;
            }

            if (proteinIndex < proteinSize) {
                int i;
                for (i = proteinIndex; i < proteinSize; i++) {
                    LightProteinMatch proteinCur = m_proteins.get(i);
                    if (!proteinFlagSet.contains(proteinCur)) {
                        proteinQueue.add(proteinCur);
                        proteinFlagSet.add(proteinCur);
                        break;
                    }
                }

                proteinIndex = i;
            } else {
                int j;
                for (j = peptideIndex; j < peptideSize; j++) {
                    LightPeptideMatch peptideCur = m_peptides.get(j);
                    if (!peptideFlagSet.contains(peptideCur)) {
                        peptideQueue.add(peptideCur);
                        peptideFlagSet.add(peptideCur);
                        break;
                    }
                }

                peptideIndex = j;
            }

        }

        if (keepSameSet) {
            return componentSet;
        }
        
        return filterComponents(componentSet);
    }
    
    
    private ArrayList<Component> filterComponents(ArrayList<Component> cList) {
        ArrayList<Component> subCList = new ArrayList<>();
        for (Component temp : cList) {
            if (temp.getPeptideArray().size() > 1 && temp.getProteinArray(false).size() > 1) {
                if (!fullMatch(temp)) {
                    subCList.add(temp);
                }
            }
        }

        return subCList;
    }

    private boolean fullMatch(Component temp) {
        for (LightPeptideMatch peptTemp : temp.getPeptideArray()) {
            ArrayList<LightProteinMatch> protList = m_peptideToProteinMap.get(peptTemp);
            if (protList.size() != temp.getProteinArray(false).size()) {
                return false;
            }
        }
        return true;
    }
}
