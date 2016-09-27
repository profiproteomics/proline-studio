package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
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
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Load Protein Matches
            TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(protm.id, protm.accession, protm.score, protm.peptideCount, protm.resultSet.id, protm.description)  FROM ProteinMatch protm WHERE protm.id IN (:listId) ", DProteinMatch.class);
            proteinMatchQuery.setParameter("listId", m_proteinMatchIdArray);
            List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();
            
            for (int i=0;i<proteinMatchList.size();i++) {
                DProteinMatch pm = proteinMatchList.get(i);
                m_proteinMatchMap.put(pm.getId(), pm);
            }
            
            // Load PeptideSet
            Query peptideSetQuery = entityManagerMSI.createQuery("SELECT ps_to_pm.id.proteinMatchId, ps FROM PeptideSet ps, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId IN (:listId) AND ps_to_pm.id.peptideSetId=ps.id AND ps_to_pm.resultSummary.id=:rsmId");
            peptideSetQuery.setParameter("listId", m_proteinMatchIdArray);
            peptideSetQuery.setParameter("rsmId", m_rsm.getId());
            List<Object[]> peptideSets = peptideSetQuery.getResultList();
            Iterator<Object[]> itPSet = peptideSets.iterator();
            while (itPSet.hasNext()) {
                Object[] res = itPSet.next();
                Long proteinMatchId = (Long) res[0];
                PeptideSet peptideSet = (PeptideSet) res[1];
                m_proteinMatchMap.get(proteinMatchId).setPeptideSet(m_rsm.getId(), peptideSet);
            }

            
            // Load Peptide Matches
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy) FROM fr.proline.core.orm.msi.PeptideMatch pm WHERE pm.id IN (:listId) ", DPeptideMatch.class);
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
                DatabaseLoadPeptidesInstancesTask.fetchPeptideData(entityManagerMSI, m_rsm, itProt.next(), tmpMap);
                tmpMap.clear();
            }
            
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
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            
            HashMap<Long, LightProteinMatch> proteinMatchMap = new HashMap<>();
            HashMap<Long, LightPeptideMatch> peptideMatchMap = new HashMap<>();
            HashMap<LightProteinMatch, ArrayList<LightPeptideMatch>> proteinToPeptideMap = new HashMap<>();
            HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> peptideToProteinMap = new HashMap<>();
            
            ArrayList<LightProteinMatch> allProteins = new ArrayList<>();
            ArrayList<LightPeptideMatch> allPeptides = new ArrayList<>();
            
            
            Query proteinMatchQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.accession, pm.score FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND ps_to_pm.id.proteinMatchId=pm.id AND ps_to_pm.id.proteinSetId=ps.id AND ps.isValidated=true");
            proteinMatchQuery.setParameter("rsmId", m_rsm.getId());
            
            List<Object[]> proteinMatchList = proteinMatchQuery.getResultList();
            ArrayList<Long> proteinMathIdList = new ArrayList<>(proteinMatchList.size());
            
            Iterator<Object[]> it = proteinMatchList.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long proteinMatchId = (Long) res[0];
                proteinMathIdList.add(proteinMatchId);
                String accession = (String) res[1];
                Float score = (Float) res[2];
                LightProteinMatch proteinMatch = new LightProteinMatch(proteinMatchId, accession, score);
                proteinMatchMap.put(proteinMatchId, proteinMatch);
            }

            
            HashMap<Long, ArrayList<Long>> proteinToPeptideIdMap = new HashMap<>();
            HashSet<Long> peptidesSet = new HashSet<>();
            
            final int SLICE_SIZE = 1000;
            SubTaskManager subTaskManager = new SubTaskManager(1);
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(0, proteinMathIdList.size(), SLICE_SIZE);
            while (subTask != null) {

                Query proteinMatch2PepMatchQuery = entityManagerMSI.createQuery("SELECT pm.id, pepm.id FROM ProteinMatch pm, PeptideSetProteinMatchMap pepset_to_pm, PeptideMatch pepm, PeptideSetPeptideInstanceItem ps_to_pi, PeptideInstance pi WHERE pm.id IN (:pmlist) AND pepset_to_pm.resultSummary.id=:rsmId   AND pepset_to_pm.id.proteinMatchId=pm.id AND pepset_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pi.id.peptideInstanceId = pi.id AND pi.bestPeptideMatchId=pepm.id");
                proteinMatch2PepMatchQuery.setParameter("pmlist", subTask.getSubList(proteinMathIdList));
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
                Query peptideMatchQuery = entityManagerMSI.createQuery("SELECT pepm.id, pepm.score, pepm.cdPrettyRank, pep.sequence FROM PeptideMatch pepm, Peptide pep WHERE pepm.id IN (:pepList) AND pepm.peptideId=pep.id");
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
            
            Iterator<Long> itProteinId = proteinToPeptideIdMap.keySet().iterator();
            while (itProteinId.hasNext()) {
                Long proteinId = itProteinId.next();
                LightProteinMatch protein = proteinMatchMap.get(proteinId);
                allProteins.add(protein);
                ArrayList<Long> peptideIdList = proteinToPeptideIdMap.get(proteinId);
                
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
            
            m_adjacencyMatrixData.setData(allProteins, allPeptides, proteinToPeptideMap, peptideToProteinMap);
            
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
