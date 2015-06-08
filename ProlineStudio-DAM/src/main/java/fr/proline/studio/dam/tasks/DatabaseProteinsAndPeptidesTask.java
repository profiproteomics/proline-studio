package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.TASK_LIST_INFO;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.m_logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseProteinsAndPeptidesTask extends AbstractDatabaseTask {
    
    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    
    public DatabaseProteinsAndPeptidesTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm) {
        super(callback, new TaskInfo("Load All Proteins and Peptides for "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
    
        m_projectId = projectId;
        m_rsm = rsm;
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
            }

            
            HashMap<Long, ArrayList<Long>> proteinToPeptideMap = new HashMap<>();
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
                ArrayList<Long> peptideArray = proteinToPeptideMap.get(proteinMatchId);
                if (peptideArray == null) {
                    peptideArray = new ArrayList<>();
                    proteinToPeptideMap.put(proteinMatchId, peptideArray);
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
    
}
