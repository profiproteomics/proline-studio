/*	Defines structure of each component
 * */
package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import java.util.ArrayList;

import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.dam.tasks.data.WeakPeptideReference;
import java.util.HashMap;
import java.util.Set;

public class Component {

    public ArrayList<LightProteinMatch> m_proteinMatchArray = new ArrayList<>();
    public ArrayList<LightPeptideMatch> m_peptideArray = new ArrayList<>();
    
    private HashMap<Integer, WeakPeptideReference> m_weakPeptidesMap = null;

    public Component() { 
    }

    public int getProteinSize() {
        return m_proteinMatchArray.size();
    }

    public int getPeptideSize() {
        return m_peptideArray.size();
    }
    
    public int[][] getPeptProtMatrix(HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap) {

        
        int peptideSize = m_peptideArray.size();
        int proteinSize = m_proteinMatchArray.size();
        
        HashMap<LightProteinMatch, Integer> proteinIndexMap = new HashMap<>(proteinSize);
        int index = 0;
        for (LightProteinMatch tempProt : m_proteinMatchArray) {
            proteinIndexMap.put(tempProt, index);
            index++;
        }
        
        int[][] tempMatch = new int[peptideSize][proteinSize]; // full of 0
        int i = 0;
        for (LightPeptideMatch tempPept : m_peptideArray) {
            ArrayList<LightProteinMatch> proteinList = peptideToProteinMap.get(tempPept);
            int j = 0;

            for(LightProteinMatch tempProt : proteinList) {
                tempMatch[i][proteinIndexMap.get(tempProt)] = 1;
            }

            i++;
        }

        //row - Pept , col - Prot
        return tempMatch;
    }
    
    public boolean isWeakPeptide(int peptideIndex, long proteinSetId) {
        WeakPeptideReference weakPeptideReference = m_weakPeptidesMap.get(peptideIndex);
        if (weakPeptideReference == null) {
            return false;
        }
        return weakPeptideReference.correspondToProteinSet(proteinSetId);
    }
    
    // JPM.TODO : weak peptides searched, it not ok for the moment
    public void searchWeakPeptides(DrawVisualization drawVisualization, double weakPeptideScoreThreshold) {

        if (m_weakPeptidesMap != null) {
            // search already done
            m_weakPeptidesMap.clear();
            //return; //JPM.TODO
        }
        m_weakPeptidesMap = new HashMap<>();
        
        HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap = drawVisualization.getPeptideToProteinMap();
        HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> proteinToPeptideMap = drawVisualization.getProteinToPeptideMap();
        
        
        int[][] pepProtMatrix = getPeptProtMatrix(peptideToProteinMap);
        
        // keep only one protein with the maximum number of peptides for each proteinSet
        int nbPeptides = pepProtMatrix.length;
        int nbProteins = pepProtMatrix[0].length;
        HashMap<Long, Integer> mapProtein = new HashMap<>();
        for (int i=0;i<nbProteins;i++) {
            LightProteinMatch proteinMatch = m_proteinMatchArray.get(i);
            
            Long proteinSetId = proteinMatch.getProteinSetId();
            Integer proteinMatchSelectedIndex = mapProtein.get(proteinSetId);
            if (proteinMatchSelectedIndex == null) {
                mapProtein.put(proteinSetId, i);
            } else {
                int nbPeptidesCur =  proteinToPeptideMap.get(proteinMatch).size();
                int nbPeptidesSelected = proteinToPeptideMap.get(m_proteinMatchArray.get(proteinMatchSelectedIndex)).size();
                if (nbPeptidesCur>nbPeptidesSelected) {
                    mapProtein.put(proteinSetId, i);
                }
            }
        }
        
        // compare Peptides of Proteins between them
        Set<Long> ids = mapProtein.keySet();
        Long[] proteinSetIds = ids.toArray(new Long[ids.size()]);
        for (int i=0;i<proteinSetIds.length-1;i++) { 
            for (int j=i+1;j<proteinSetIds.length;j++) {
                Long proteinSetId1 = proteinSetIds[i];
                Long proteinSetId2 = proteinSetIds[j];
                Integer pm1Index = mapProtein.get(proteinSetId1);
                Integer pm2Index = mapProtein.get(proteinSetId2);
                int nbCommonPeptides = 0;
                int commonPeptideIndex = -1;
                for (int k=0;k<nbPeptides;k++) {
                    if ((pepProtMatrix[k][pm1Index]>0) && (pepProtMatrix[k][pm2Index]>0)) {
                        nbCommonPeptides++;
                        commonPeptideIndex = k;
                        if (nbCommonPeptides>=2) {
                            break;
                        }
                    }
                }
                if (nbCommonPeptides == 1) {
                    // found a potential weak peptide between two Proteins of different Protein Set
                    LightPeptideMatch peptide = m_peptideArray.get(commonPeptideIndex);
                    if (peptide.getScore()<=weakPeptideScoreThreshold) {
                        // we have found a weakPeptide
                        WeakPeptideReference weakPeptide = m_weakPeptidesMap.get(commonPeptideIndex);
                        if (weakPeptide == null) {
                            weakPeptide = new WeakPeptideReference(commonPeptideIndex, proteinSetId1, proteinSetId2);
                            m_weakPeptidesMap.put(commonPeptideIndex, weakPeptide);
                        } else {
                            weakPeptide.add(proteinSetId1, proteinSetId2);
                        }
                    }
                }
                
            }
        }
        
    }

}
