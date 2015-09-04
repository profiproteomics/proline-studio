package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
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

/**
 * Loading of Protein Matches and corresponding Peptides for the Adjacency Matrix
 * @author JM235353
 */
public class DatabaseProteinsAndPeptidesTask extends AbstractDatabaseTask {
    
    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    
    private AdjacencyMatrixData m_adjacencyMatrixData = null;
    
    public DatabaseProteinsAndPeptidesTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, AdjacencyMatrixData adjacencyMatrixData ) {
        super(callback, new TaskInfo("Load All Proteins and Peptides for "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
    
        m_projectId = projectId;
        m_rsm = rsm;
        
        m_adjacencyMatrixData = adjacencyMatrixData;
    }
    
    @Override
    public boolean needToFetch() {
        return true;
    }
    
    @Override
    public boolean fetchData() {
        if (needToFetch()) {
            // first data are fetched
            return fetchAllProteinsAndPeptides();
        }
        return true; // should not happen
    }
    
    private boolean fetchAllProteinsAndPeptides() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
            
            Query proteinMatch2PepMatchQuery = entityManagerMSI.createQuery("SELECT pm.id, pepm.id FROM ProteinMatch pm, PeptideSetProteinMatchMap pepset_to_pm, PeptideMatch pepm, PeptideSetPeptideInstanceItem ps_to_pi, PeptideInstance pi WHERE pm.id IN (:pmlist) AND pepset_to_pm.resultSummary.id=:rsmId   AND pepset_to_pm.id.proteinMatchId=pm.id AND pepset_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pi.id.peptideInstanceId = pi.id AND pi.bestPeptideMatchId=pepm.id");
            proteinMatch2PepMatchQuery.setParameter("pmlist", proteinMathIdList);
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
            
            
            Query peptideMatchQuery = entityManagerMSI.createQuery("SELECT pepm.id, pepm.score, pepm.cdPrettyRank, pep.sequence FROM PeptideMatch pepm, Peptide pep WHERE pepm.id IN (:pepList) AND pepm.peptideId=pep.id");
            peptideMatchQuery.setParameter("pepList", new ArrayList<Long>(peptidesSet));
            resList = peptideMatchQuery.getResultList();
            it = resList.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long peptideMatchId = (Long) res[0];
                Float score = (Float) res[1];
                Integer cdPrettyRank = (Integer) res[2];
                String sequence = (String) res[3];
                
                LightPeptideMatch peptideMatch = new LightPeptideMatch(peptideMatchId,score, cdPrettyRank);
                
                peptideMatchMap.put(peptideMatchId, peptideMatch);
                allPeptides.add(peptideMatch);
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
