package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.TASK_LIST_INFO;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.m_logger;
import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Loading of Protein Matches and corresponding Peptides for the Adjacency Matrix
 * @author JM235353
 */
public class DatabaseProteinsAndPeptidesTask extends AbstractDatabaseTask {
    
     private static final int LOAD_ALL_PROTEINS_AND_PEPTIDES = 0;
    private static final int LOAD_PROTEINS_AND_PEPTIDES_FROM_IDS = 1;
    
    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    private AdjacencyMatrixData m_adjacencyMatrixData = null;
    
    ArrayList<Long> m_proteinMatchIdArray;
    ArrayList<Long> m_peptideMatchIdArray;
    HashMap<Long, DProteinMatch> m_proteinMatchMap;
    HashMap<Long, DPeptideMatch> m_peptideMatchMap;
    
    private final int m_action; 
    
    public DatabaseProteinsAndPeptidesTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, AdjacencyMatrixData adjacencyMatrixData ) {
        super(callback, new TaskInfo("Load All Proteins and Peptides for "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
    
        m_projectId = projectId;
        m_rsm = rsm;
        
        m_adjacencyMatrixData = adjacencyMatrixData;
        
        m_action = LOAD_ALL_PROTEINS_AND_PEPTIDES;
    }
    
    public DatabaseProteinsAndPeptidesTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, ArrayList<Long> proteinMatchIdArray, ArrayList<Long> peptideMatchIdArray, HashMap<Long, DProteinMatch> proteinMatchMap,  HashMap<Long, DPeptideMatch> peptideMatchMap) {
        super(callback, new TaskInfo("Load Proteins and Peptides for an Adjacency Matrix", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));

        m_projectId = projectId;
        m_rsm = rsm;
        
        m_proteinMatchIdArray = proteinMatchIdArray;
        m_peptideMatchIdArray = peptideMatchIdArray;
        m_proteinMatchMap = proteinMatchMap;
        m_peptideMatchMap = peptideMatchMap;

        m_action = LOAD_PROTEINS_AND_PEPTIDES_FROM_IDS;
    }
    
    @Override
    public boolean needToFetch() {
        return true;
    }
    
    @Override
    public boolean fetchData() {
        
        if (m_action == LOAD_ALL_PROTEINS_AND_PEPTIDES) {
            if (needToFetch()) {
                // first data are fetched
                return fetchAllProteinsAndPeptides();
            }
        } else if (m_action == LOAD_PROTEINS_AND_PEPTIDES_FROM_IDS) {
            if (needToFetch()) {
                // first data are fetched
                return fetchProteinsAndPeptidesFromIds();
            }
        }
        
        return true; // should not happen
    }
    
    private boolean fetchProteinsAndPeptidesFromIds() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Load Protein Matches
            TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(protm.id, protm.accession, protm.score, protm.peptideCount, protm.resultSet.id, protm.description, protm.serializedProperties)  FROM ProteinMatch protm WHERE protm.id IN (:listId) ", DProteinMatch.class);
            proteinMatchQuery.setParameter("listId", m_proteinMatchIdArray);
            List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();
            
            for (DProteinMatch pm : proteinMatchList) {
                m_proteinMatchMap.put(pm.getId(), pm);
            }
            
            // Load PeptideSet
            Query peptideSetQuery = entityManagerMSI.createQuery("SELECT ps_to_pm.id.proteinMatchId, ps.id, ps.score, ps.sequenceCount, ps.peptideCount, ps.peptideMatchCount, ps.resultSummaryId FROM PeptideSet ps, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId IN (:listId) AND ps_to_pm.id.peptideSetId=ps.id AND ps_to_pm.resultSummary.id=:rsmId");
            peptideSetQuery.setParameter("listId", m_proteinMatchIdArray);
            peptideSetQuery.setParameter("rsmId", m_rsm.getId());
            List<Object[]> peptideSets = peptideSetQuery.getResultList();
            Iterator<Object[]> itPSet = peptideSets.iterator();
            while (itPSet.hasNext()) {
                Object[] res = itPSet.next();
                Long proteinMatchId = (Long) res[0];
                Long psId = (Long) res[1];
                Float psScore = (Float) res[2];
                Integer psSequenceCount = (Integer) res[3];
                Integer psPeptideCount = (Integer) res[4];
                Integer psPeptideMatchCount = (Integer) res[5];
                Long psResultSummaryId = (Long) res[6];
                m_proteinMatchMap.get(proteinMatchId).setPeptideSet(m_rsm.getId(), new DPeptideSet(psId, psScore, psSequenceCount, psPeptideCount, psPeptideMatchCount, psResultSummaryId));
            }

            
            // Load Peptide Matches
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy, pm.serializedProperties) FROM fr.proline.core.orm.msi.PeptideMatch pm WHERE pm.id IN (:listId) ", DPeptideMatch.class);
            peptideMatchQuery.setParameter("listId", m_peptideMatchIdArray);
            List<DPeptideMatch> peptideMatchList = peptideMatchQuery.getResultList();
            
            for (int i=0;i<peptideMatchList.size();i++) {
                DPeptideMatch p = peptideMatchList.get(i);
                m_peptideMatchMap.put(p.getId(), p);
            }
            
            
            // read peptide for peptideMatch
            Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p FROM PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id");
            peptideQuery.setParameter("listId", m_peptideMatchIdArray);

            List<Object[]> peptides = peptideQuery.getResultList();
            Iterator<Object[]> it = peptides.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long peptideMatchId = (Long) res[0];
                Peptide peptide = (Peptide) res[1];
                m_peptideMatchMap.get(peptideMatchId).setPeptide(peptide);
            }
  
            HashMap<Long, Peptide> tmpMap = new HashMap<>();
            Iterator<DProteinMatch> itProt = proteinMatchList.iterator();
            while (itProt.hasNext()) {
                DatabaseLoadPeptidesInstancesTask.fetchPeptideDataForProteinMatch(entityManagerMSI, m_rsm, itProt.next(), tmpMap);
                tmpMap.clear();
            }
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }
    
    private boolean fetchAllProteinsAndPeptides() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            
            HashMap<Long, LightProteinMatch> proteinMatchMap = new HashMap<>();
            HashMap<Long, LightPeptideMatch> peptideMatchMap = new HashMap<>();
            HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> proteinToPeptideMap = new HashMap<>();
            HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap = new HashMap<>();
            
            ArrayList<LightProteinMatch> allProteins = new ArrayList<>();
            ArrayList<LightPeptideMatch> allPeptides = new ArrayList<>();
            
            
            Query proteinMatchQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.accession, pm.score, ps.id, ps.representativeProteinMatchId FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND ps_to_pm.id.proteinMatchId=pm.id AND ps_to_pm.id.proteinSetId=ps.id AND ps.isValidated=true");
            proteinMatchQuery.setParameter("rsmId", m_rsm.getId());
            
            List<Object[]> proteinMatchList = proteinMatchQuery.getResultList();
            ArrayList<Long> proteinMatchIdList = new ArrayList<>(proteinMatchList.size());
            Iterator<Object[]> it = proteinMatchList.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long proteinMatchId = (Long) res[0];
                proteinMatchIdList.add(proteinMatchId);
                String accession = (String) res[1];
                Float score = (Float) res[2];
                Long proteinSetId = (Long) res[3];
                Long representativeProteinMatchId = (Long) res[4];
                LightProteinMatch proteinMatch = new LightProteinMatch(proteinMatchId, accession, score, proteinSetId, representativeProteinMatchId);
                proteinMatchMap.put(proteinMatchId, proteinMatch);
            }

            
            HashMap<Long, ArrayList<Long>> proteinToPeptideIdMap = new HashMap<>();
            HashSet<Long> peptidesSet = new HashSet<>();
            
            final int SLICE_SIZE = 1000;
            SubTaskManager subTaskManager = new SubTaskManager(1);
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(0, proteinMatchIdList.size(), SLICE_SIZE);
            while (subTask != null) {

                Query proteinMatch2PepMatchQuery = entityManagerMSI.createQuery("SELECT pm.id, pepm.id FROM ProteinMatch pm, PeptideSetProteinMatchMap pepset_to_pm, PeptideMatch pepm, PeptideSetPeptideInstanceItem ps_to_pi, PeptideInstance pi WHERE pm.id IN (:pmlist) AND pepset_to_pm.resultSummary.id=:rsmId   AND pepset_to_pm.id.proteinMatchId=pm.id AND pepset_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pi.id.peptideInstanceId = pi.id AND pi.bestPeptideMatchId=pepm.id ORDER BY pepm.id");
                proteinMatch2PepMatchQuery.setParameter("pmlist", subTask.getSubList(proteinMatchIdList));
                proteinMatch2PepMatchQuery.setParameter("rsmId", m_rsm.getId());
                List<Object[]> resList = proteinMatch2PepMatchQuery.getResultList();
                it = resList.iterator();
                while (it.hasNext()) {
                    Object[] res = it.next();
                    Long proteinMatchId = (Long) res[0];
                    Long peptideMatchId = (Long) res[1];
                    ArrayList<Long> peptideArray = proteinToPeptideIdMap.get(proteinMatchId);
                    if (peptideArray == null) {
                        peptideArray = new ArrayList<>();
                        proteinToPeptideIdMap.put(proteinMatchId, peptideArray);
                    }
                    peptideArray.add(peptideMatchId);
                    peptidesSet.add(peptideMatchId);
                }

                subTask = subTaskManager.getNextSubTask();
            }
            
            ArrayList peptides = new ArrayList(peptidesSet);
            subTaskManager = new SubTaskManager(1);
            subTask = subTaskManager.sliceATaskAndGetFirst(0, peptides.size(), SLICE_SIZE);
            while (subTask != null) {
                Query peptideMatchQuery = entityManagerMSI.createQuery("SELECT pepm.id, pepm.score, pepm.cdPrettyRank, pep.sequence FROM PeptideMatch pepm, fr.proline.core.orm.msi.Peptide pep WHERE pepm.id IN (:pepList) AND pepm.peptideId=pep.id");
                peptideMatchQuery.setParameter("pepList", subTask.getSubList(peptides));
                List<Object[]> resList = peptideMatchQuery.getResultList();
                it = resList.iterator();
                while (it.hasNext()) {
                    Object[] res = it.next();
                    Long peptideMatchId = (Long) res[0];
                    Float score = (Float) res[1];
                    Integer cdPrettyRank = (Integer) res[2];
                    String sequence = (String) res[3];

                    LightPeptideMatch peptideMatch = new LightPeptideMatch(peptideMatchId, score, cdPrettyRank, sequence);

                    peptideMatchMap.put(peptideMatchId, peptideMatch);
                    allPeptides.add(peptideMatch);
                }

                subTask = subTaskManager.getNextSubTask();
            }
            
            
            // Look for typical proteins of protein Set
            HashMap<Long, LightProteinMatch> proteinSetIdToTypical = new HashMap<>();
            Iterator<Long> itProteinId = proteinToPeptideIdMap.keySet().iterator();
            while (itProteinId.hasNext()) {
                Long proteinId = itProteinId.next();
                LightProteinMatch protein = proteinMatchMap.get(proteinId);
                long proteinSetId = protein.getProteinSetId();
                long typicalProteinId = protein.getRepresentativeProteinMatchId();
                if (typicalProteinId == proteinId) {
                    // we have found the typical protein
                    proteinSetIdToTypical.put(proteinSetId, protein);
                    allProteins.add(protein);
                }
            }
            
            // equivalent proteins (proteins with the same peptides)
            HashMap<LightProteinMatch, ArrayList<LightProteinMatch>> equivalentProteins = new HashMap<>();
            
            //Equivalent to Main Protein
            HashMap<LightProteinMatch, LightProteinMatch> equivalentToMainProtein = new HashMap<>();
            
            // look for a protein for each samset and subset
            HashMap<Long, ArrayList<LightProteinMatch>> proteinSetIdToSubsetsProteins = new HashMap<>();
            itProteinId = proteinToPeptideIdMap.keySet().iterator();
            while (itProteinId.hasNext()) {
                Long proteinId = itProteinId.next();
                LightProteinMatch protein = proteinMatchMap.get(proteinId);
                long proteinSetId = protein.getProteinSetId();
                
                LightProteinMatch typicalProtein = proteinSetIdToTypical.get(proteinSetId);
                ArrayList<Long> typicalPeptideIdList = proteinToPeptideIdMap.get(typicalProtein.getId());
                int nbPeptidesTypical = typicalPeptideIdList.size();
                
                ArrayList<Long> peptideIdList = proteinToPeptideIdMap.get(protein.getId());
                int nbPeptides = peptideIdList.size();
                if (nbPeptides == nbPeptidesTypical) {
                    // Protein of the sameset
                    if (!typicalProtein.equals(protein)) {
                        // we put it in the equivalent protein map if it is not itself
                        ArrayList<LightProteinMatch> equivalentProteinsArray = equivalentProteins.get(typicalProtein);
                        if (equivalentProteinsArray == null) {
                            equivalentProteinsArray = new ArrayList<>();
                            equivalentProteins.put(typicalProtein, equivalentProteinsArray);
                        }
                        equivalentProteinsArray.add(protein);
                        equivalentToMainProtein.put(protein, typicalProtein);
                    }
                    continue;
                }
                
                // we have a protein of the subset
                 ArrayList<LightProteinMatch> subsetMainProteins = proteinSetIdToSubsetsProteins.get(proteinSetId);
                 if (subsetMainProteins == null) {
                     subsetMainProteins = new ArrayList<>();
                     subsetMainProteins.add(protein);
                     allProteins.add(protein);
                     proteinSetIdToSubsetsProteins.put(proteinSetId, subsetMainProteins);
                 } else {

                     boolean correspondingProtein = false;
                     Iterator<LightProteinMatch> subsetMainProteinsIt = subsetMainProteins.iterator();
                     LightProteinMatch proteinCur =  null;
                     while (subsetMainProteinsIt.hasNext()) {
                         proteinCur = subsetMainProteinsIt.next();
                         ArrayList<Long> petidesListCur = proteinToPeptideIdMap.get(proteinCur.getId());
                         int nbPeptidesCur = petidesListCur.size();
                         if (nbPeptides != nbPeptidesCur) {
                             continue;
                         }
                         boolean peptidesCorrespond = true;
                         for (int i=0;i<nbPeptides;i++) {
                             long id1 =  peptideIdList.get(i);
                             long idCur = petidesListCur.get(i);
                             if (id1 != idCur) {
                                 peptidesCorrespond = false;
                                 break;
                             }
                         }
                         if (peptidesCorrespond) {
                             // there is already a corresponding protein in subset
                             correspondingProtein = true;
                             break;
                         }
                     }
                     if (!correspondingProtein) {
                         subsetMainProteins.add(protein);
                         allProteins.add(protein);
                     } else {
                         // we put it in the equivalent protein map
                         ArrayList<LightProteinMatch> equivalentProteinsArray = equivalentProteins.get(proteinCur);
                         if (equivalentProteinsArray == null) {
                             equivalentProteinsArray = new ArrayList<>();
                             equivalentProteins.put(proteinCur, equivalentProteinsArray);
                         }
                         equivalentProteinsArray.add(protein);
                         equivalentToMainProtein.put(protein, proteinCur);
                     }
                 }
            }
            
            Iterator<LightProteinMatch> itProtein = allProteins.iterator();
            while (itProtein.hasNext()) {
                LightProteinMatch protein = itProtein.next();
                ArrayList<Long> peptideIdList = proteinToPeptideIdMap.get(protein.getId());
                
                ArrayList<LightPeptideMatch> peptideMatchList = new ArrayList<>(peptideIdList.size());
                for (Long peptideId : peptideIdList) {
                    LightPeptideMatch peptide = peptideMatchMap.get(peptideId);
                    peptideMatchList.add(peptide);
                    
                    ArrayList<LightProteinMatch> proteinList = peptideToProteinMap.get(peptide);
                    if (proteinList == null) {
                        proteinList = new ArrayList<>();
                        peptideToProteinMap.put(peptide, proteinList);
                    }
                    proteinList.add(protein);
                }
                
                proteinToPeptideMap.put(protein, peptideMatchList);
            }

            m_adjacencyMatrixData.setData(allProteins, allPeptides, proteinToPeptideMap, peptideToProteinMap, equivalentProteins, equivalentToMainProtein);
            
            entityManagerMSI.getTransaction().commit();

            
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
         return true;
    }
    
}
