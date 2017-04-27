package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinPTMSite;
import fr.proline.studio.dam.tasks.data.PTMSite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class PtmProteinSiteTableModelProcessing {

    //public static HashMap<String, String> TEST_MODIFICATIONS = new   HashMap<>();
    
    public static String calculateData(AbstractTableModel model, ArrayList<String> modificationsArray, ArrayList<Character> residuesArray, HashMap<Character, Integer> residuesMap, ArrayList<DProteinPTMSite> proteinPTMSiteArray, ArrayList<DProteinPTMSite> proteinPTMSiteNoRedundantArray, HashMap<String, Integer> modificationsMap) {

        //TEST_MODIFICATIONS.clear();
        
        proteinPTMSiteNoRedundantArray.clear();

        int nbRows = model.getRowCount();

        /* if (proteinPTMSiteArray.isEmpty()) {
            // specific case, we mut fill the Array of PTMSite
            for (int i = 0; i < nbRows; i++) {
                proteinPTMSiteArray.add((DProteinPTMSite) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_HIDDEN_PROTEIN_PTM));
            }
        }*/

        // List of different modifications and residues
        TreeSet<String> modificationTreeSet = new TreeSet<>();
        TreeSet<Character> residueTreeSet = new TreeSet<>();

        for (int i = 0; i < nbRows; i++) {
            String modification = (String) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_MODIFICATION);
            modificationTreeSet.add(modification);

            Character residue = (Character) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_RESIDUE_AA);
            if (residue != null) {
                residueTreeSet.add(residue);
            }
        }

        modificationsArray.addAll(modificationTreeSet);

        for (int i = 0; i < modificationsArray.size(); i++) {
            modificationsMap.put(modificationsArray.get(i), i);
        }

        // List of different residues
        residuesArray.addAll(residueTreeSet);
        for (int i = 0; i < residuesArray.size(); i++) {
            residuesMap.put(residuesArray.get(i), i);
        }

        // Count for each type of modification, the number of modifications
        HashMap<String, Integer> globalDistinctModificationsMap = groupProteinMatch(model, proteinPTMSiteArray, proteinPTMSiteNoRedundantArray);

        StringBuilder sb = new StringBuilder();
        Iterator<String> it = globalDistinctModificationsMap.keySet().iterator();
        while (it.hasNext()) {
            String modification = it.next();
            Integer nbModifications = globalDistinctModificationsMap.get(modification);
            sb.append(modification).append(":").append(nbModifications);
            if (it.hasNext()) {
                sb.append("   ");
            }
        }

        
        //Set<String> testSet = TEST_MODIFICATIONS.keySet();
        //ArrayList<String> testModifArray = new ArrayList<>();
        //testModifArray.addAll(testSet);
        
        /*Collections.sort(testModifArray);
        
        for (String test : testModifArray) {
            if (test.contains("6PGD")) {
                System.out.println(test);
            }
        }*/
        
        return sb.toString();

    }
    
    public static String calculateDataWORedundance(AbstractTableModel model, ArrayList<String> modificationsArray, ArrayList<Character> residuesArray, HashMap<Character, Integer> residuesMap, ArrayList<PTMSite> proteinPTMSiteArray,HashMap<String, Integer> modificationsMap) {

        //TEST_MODIFICATIONS.clear();
        
        int nbRows = model.getRowCount();

        /* if (proteinPTMSiteArray.isEmpty()) {
            // specific case, we mut fill the Array of PTMSite
            for (int i = 0; i < nbRows; i++) {
                proteinPTMSiteArray.add((DProteinPTMSite) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_HIDDEN_PROTEIN_PTM));
            }
        }*/

        // List of different modifications and residues
        TreeSet<String> modificationTreeSet = new TreeSet<>();
        TreeSet<Character> residueTreeSet = new TreeSet<>();

        for (int i = 0; i < nbRows; i++) {
            String modification = (String) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_MODIFICATION);
            modificationTreeSet.add(modification);

            Character residue = (Character) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_RESIDUE_AA);
            if (residue != null) {
                residueTreeSet.add(residue);
            }
        }

        modificationsArray.addAll(modificationTreeSet);

        for (int i = 0; i < modificationsArray.size(); i++) {
            modificationsMap.put(modificationsArray.get(i), i);
        }

        // List of different residues
        residuesArray.addAll(residueTreeSet);
        for (int i = 0; i < residuesArray.size(); i++) {
            residuesMap.put(residuesArray.get(i), i);
        }

        // Count for each type of modification, the number of modifications
        HashMap<String, Integer> globalDistinctModificationsMap = getModificationCount(model, proteinPTMSiteArray);

        StringBuilder sb = new StringBuilder();
        Iterator<String> it = globalDistinctModificationsMap.keySet().iterator();
        while (it.hasNext()) {
            String modification = it.next();
            Integer nbModifications = globalDistinctModificationsMap.get(modification);
            sb.append(modification).append(":").append(nbModifications);
            if (it.hasNext()) {
                sb.append("   ");
            }
        }

        
        //Set<String> testSet = TEST_MODIFICATIONS.keySet();
        //ArrayList<String> testModifArray = new ArrayList<>();
        //testModifArray.addAll(testSet);
        
        /*Collections.sort(testModifArray);
        
        for (String test : testModifArray) {
            if (test.contains("6PGD")) {
                System.out.println(test);
            }
        }*/
        
        return sb.toString();

    }

    private static HashMap<String, Integer> getModificationCount(AbstractTableModel model, ArrayList<PTMSite> proteinPTMSiteArray) {
           
        int rowCount = proteinPTMSiteArray.size();
        HashMap<String, Integer> globalDistinctModificationsMap = new HashMap<>();

        for (int i = 0; i < rowCount; i++) {
            String modification = (String) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_MODIFICATION);                
            Integer nb = globalDistinctModificationsMap.get(modification);
            if (nb == null) {
                globalDistinctModificationsMap.put(modification, 1);
            } else {
                globalDistinctModificationsMap.put(modification, nb + 1);
            }
        }
        return globalDistinctModificationsMap;        
    }
    
    
//    private static void ggroupPeptideMatch(DProteinMatch proteinMatch,AbstractTableModel model, ArrayList<PTMSite> proteinPTMSiteArray, HashMap<String, Integer> globalDistinctModificationsMap, int i1, int i2) {
//
//        for (int i = i1; i <= i2; i++) {
//            String modification = (String) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_MODIFICATION);                
//            Integer nb = globalDistinctModificationsMap.get(modification);
//            if (nb == null) {
//                globalDistinctModificationsMap.put(modification, 1);
//            } else {
//                globalDistinctModificationsMap.put(modification, nb + 1);
//            }
//        }
//    }
    
    private static HashMap<String, Integer> groupProteinMatch(AbstractTableModel model, ArrayList<DProteinPTMSite> proteinPTMSiteArray, ArrayList<DProteinPTMSite> proteinPTMSiteNoRedundantArray) {

        int rowCount = proteinPTMSiteArray.size();

        HashMap<String, Integer> globalDistinctModificationsMap = new HashMap<>();

        // group all the same protein Match
        int start = 0;
        DProteinMatch proteinMatchPrev = null;
        for (int i = 0; i < rowCount; i++) {
            DProteinPTMSite proteinPTMSite = proteinPTMSiteArray.get(i);
            DProteinMatch proteinMatch = proteinPTMSite.getPoteinMatch();
            if (proteinMatchPrev == null) {
                proteinMatchPrev = proteinMatch;
                continue;
            } else if (proteinMatchPrev.getId() == proteinMatch.getId()) {
                continue;
            } else {
                int stop = i - 1;
                groupPeptideMatch(proteinMatchPrev, model, proteinPTMSiteArray, proteinPTMSiteNoRedundantArray, globalDistinctModificationsMap, start, stop);

                proteinMatchPrev = proteinMatch;
                start = i;
            }
        }
        int stop = rowCount - 1;
        if (stop >= start) {
            groupPeptideMatch(proteinMatchPrev, model, proteinPTMSiteArray, proteinPTMSiteNoRedundantArray, globalDistinctModificationsMap, start, stop);
        }

        return globalDistinctModificationsMap;
    }

    private static void groupPeptideMatch(DProteinMatch proteinMatch,AbstractTableModel model, ArrayList<DProteinPTMSite> proteinPTMSiteArray, ArrayList<DProteinPTMSite> proteinPTMSiteNoRedundantArray, HashMap<String, Integer> globalDistinctModificationsMap, int i1, int i2) {

        // group all the same peptide match and create a map for each with modifications
        HashMap<DPeptideMatch, HashMap<String, String>> peptideMatchMap = new HashMap<>();
        HashMap<String, String> distinctModificationsMap = new HashMap<>();

        int start = i1;
        DPeptideMatch peptideMatchPrev = null;

        ArrayList<DPeptideMatch> peptideMatchArray = new ArrayList<>();  // all peptides of the current protein
        for (int i = i1; i <= i2; i++) {
            DProteinPTMSite proteinPTMSite = proteinPTMSiteArray.get(i);
            DPeptideMatch peptideMatch = proteinPTMSite.getPeptideMatch();

            if (peptideMatchPrev == null) {
                peptideMatchPrev = peptideMatch;
                peptideMatchArray.add(peptideMatch);
                continue;
            } else if (peptideMatchPrev.getId() == peptideMatch.getId()) {
                continue;
            } else {
                int stop = i - 1;
                peptideMatchFound(proteinMatch, model, peptideMatchMap, distinctModificationsMap, peptideMatchPrev, start, stop);

                peptideMatchPrev = peptideMatch;
                peptideMatchArray.add(peptideMatch);
                start = i;
            }

        }

        int stop = i2;
        if (stop >= start) {
            peptideMatchFound(proteinMatch, model, peptideMatchMap, distinctModificationsMap, peptideMatchPrev, start, stop);
        }

        // sort peptide match array of the current ProteinMatch according to the scores of peptide match
        if (m_peptideMatchComparator == null) {
            m_peptideMatchComparator = new Comparator<DPeptideMatch>() {
                @Override
                public int compare(DPeptideMatch pm1, DPeptideMatch pm2) {

                    float diff = pm1.getScore() - pm2.getScore();
                    if (diff < 0) {
                        return 1;
                    } else if (diff > 0) {
                        return -1;
                    }
                    return 0;
                }
            };
        }
        Collections.sort(peptideMatchArray, m_peptideMatchComparator);

        Iterator<String> it = distinctModificationsMap.keySet().iterator();
        while (it.hasNext()) {
            String modificationKey = it.next();
            String modification = distinctModificationsMap.get(modificationKey);
            /*if (modification.contains("Oxidation")) {
                System.out.println(modificationKey);
            }*/
            Integer nb = globalDistinctModificationsMap.get(modification);
            if (nb == null) {
                globalDistinctModificationsMap.put(modification, 1);
            } else {
                globalDistinctModificationsMap.put(modification, nb + 1);
            }
        }

        HashSet<DPeptideMatch> peptideMatchKeptSet = new HashSet<>();
        Iterator<DPeptideMatch> itPM = peptideMatchArray.iterator();
        while (itPM.hasNext()) {
            DPeptideMatch peptideMatch = itPM.next();
            HashMap<String, String> modificationsMap = peptideMatchMap.get(peptideMatch);
            Iterator<String> itModification = modificationsMap.keySet().iterator();
            boolean keepPeptideMatch = false;
            while (itModification.hasNext()) {
                String modificationKey = itModification.next();
                if (distinctModificationsMap.containsKey(modificationKey)) {
                    distinctModificationsMap.remove(modificationKey);
                    keepPeptideMatch = true;
                }
            }
            if (keepPeptideMatch) {
                peptideMatchKeptSet.add(peptideMatch);
            }
            if (distinctModificationsMap.isEmpty()) {
                break;
            }
        }

        for (int i = i1; i <= i2; i++) {
            DProteinPTMSite proteinPTMSite = proteinPTMSiteArray.get(i);
            DPeptideMatch peptideMatch = proteinPTMSite.getPeptideMatch();
            if (peptideMatchKeptSet.contains(peptideMatch)) {
                proteinPTMSiteNoRedundantArray.add(proteinPTMSite);
            }
        }

    }
    private static Comparator<DPeptideMatch> m_peptideMatchComparator = null;

    private static void peptideMatchFound(DProteinMatch proteinMatch, AbstractTableModel model, HashMap<DPeptideMatch, HashMap<String, String>> peptideMatchMap, HashMap<String, String> distinctModificationsMap, DPeptideMatch peptideMatch, int i1, int i2) {
        HashMap<String, String> modificiationMap = new HashMap<>();
        peptideMatchMap.put(peptideMatch, modificiationMap);
        for (int i = i1; i <= i2; i++) {
            Integer proteinLoc = (Integer) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_PROTEIN_LOC);
            String modification = (String) model.getValueAt(i, PtmProtenSiteTableModel.COLTYPE_MODIFICATION);
            String modificationKey = modification + proteinLoc;
            modificiationMap.put(modificationKey, modification);
            /*if (! distinctModificationsMap.containsKey(modificationKey)) {
                TEST_MODIFICATIONS.put(proteinMatch.getAccession()+" "+modificationKey, modification);
            }*/
            distinctModificationsMap.put(modificationKey, modification);
            
            
            
            //System.out.println(i+": "+proteinMatch.getAccession()+" "+peptideMatch.getPeptide().getSequence()+" "+modificationKey);
        }
    }
}
