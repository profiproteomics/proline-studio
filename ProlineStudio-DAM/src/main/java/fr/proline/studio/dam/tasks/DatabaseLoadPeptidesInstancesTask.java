package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Load Peptides corresponding to a ProteinMatch
 *
 * @author JM235353
 */
public class DatabaseLoadPeptidesInstancesTask extends AbstractDatabaseTask {

    private int m_action;
    
    private final static int LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH   = 0;
    private final static int LOAD_PEPTIDE_INSTANCES_FOR_RSM   = 1;
    
    private long m_projectId = -1;
    private DProteinMatch m_proteinMatch = null;
    private ArrayList<DProteinMatch> m_proteinMatchArray = null;
    private ArrayList<ResultSummary> m_rsmList = null;
    
    private ResultSummary m_rsm = null;

    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, long projectId, DProteinMatch proteinMatch, ArrayList<ResultSummary> rsmList) {
        super(callback, new TaskInfo("Load Peptide Sets for Protein Match "+proteinMatch.getAccession(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_proteinMatch = proteinMatch;
        m_proteinMatchArray = null;
        m_rsmList = rsmList;
        m_action = LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH;
        
    }
    
    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, long projectId, ArrayList<DProteinMatch> proteinMatchArray, ArrayList<ResultSummary> rsmList) {
        super(callback, new TaskInfo("Load Peptide Sets for multiple Protein Matches", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_proteinMatch = null;
        m_proteinMatchArray = proteinMatchArray;
        m_rsmList = rsmList;
        m_action = LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH;
    }

    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm) {
        super(callback, new TaskInfo("Load Peptides for Identification Summary "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsm = rsm;
        m_action = LOAD_PEPTIDE_INSTANCES_FOR_RSM;
    }

    
    
    @Override
    public boolean needToFetch() {
        switch(m_action) {
            case LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH: {
                int size = m_rsmList.size();
                for (int i = 0; i < size; i++) {
                    ResultSummary rsm = m_rsmList.get(i);
                    DProteinMatch pm = (m_proteinMatchArray != null) ? m_proteinMatchArray.get(i) : m_proteinMatch;
                    if (needToFetch(pm, rsm)) {
                        return true;
                    }
                }
                return false;
            }
            case LOAD_PEPTIDE_INSTANCES_FOR_RSM: {
                return (m_rsm.getTransientData().getPeptideInstanceArray() == null);
            }
                
                
        }

        return false; // should not be called
    }
    
    private boolean needToFetch(DProteinMatch proteinMatch, ResultSummary rsm) {
        if (proteinMatch == null) {
            return false;
        }
        PeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
        if (peptideSet == null) {
            return true;
        }
        return ( peptideSet.getTransientDPeptideInstances() == null);
    }
    

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH: {
                return fetchDataForPeptideMatch();
            }
            case LOAD_PEPTIDE_INSTANCES_FOR_RSM: {
                return fetchDataForRsm();
            }
        }
        return false;
    }
    

    public boolean fetchDataForRsm() {
        
         HashMap<Long, Peptide> peptideMap = new HashMap<>();
        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();

        try {

            
            entityManagerMSI.getTransaction().begin();

            ArrayList<PeptideInstance> peptideInstanceList = new ArrayList<>();
           
            
            Query peptideInstancesQuery = entityManagerMSI.createQuery("SELECT pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, p, pm.cdPrettyRank, pm.sdPrettyRank FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pi.resultSummary.id=:rsmId AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id ORDER BY pm.score DESC");
            peptideInstancesQuery.setParameter("rsmId", m_rsm.getId());

            List l = peptideInstancesQuery.getResultList();
            Iterator<Object[]> itPeptidesQuery = l.iterator();
            while (itPeptidesQuery.hasNext()) {
                Object[] resCur = itPeptidesQuery.next();
                PeptideInstance pi = (PeptideInstance) resCur[0];
                
                Long pmId = (Long) resCur[1];
                Integer pmRank = (Integer) resCur[2];
                Integer pmCharge = (Integer) resCur[3];
                Float pmDeltaMoz = (Float) resCur[4];
                Double pmExperimentalMoz = (Double) resCur[5];
                Integer pmMissedCleavage = (Integer) resCur[6];
                Float pmScore = (Float) resCur[7];
                Long pmResultSetId = (Long) resCur[8];
                Integer pmCdPrettyRank = (Integer) resCur[10];
                Integer pmSdPrettyRank = (Integer) resCur[11];
                DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);
                
                Peptide p = (Peptide) resCur[9];
                p.getTransientData().setPeptideReadablePtmStringLoaded();
                peptideMap.put(p.getId(), p);

                pi.getTransientData().setBestPeptideMatch(pm);

                pm.setPeptide(p);

                peptideInstanceList.add(pi);
            }

            int nbPeptides = peptideInstanceList.size();
            PeptideInstance[] peptideInstances = peptideInstanceList.toArray(new PeptideInstance[nbPeptides]);
            m_rsm.getTransientData().setPeptideInstanceArray(peptideInstances);

            
            // Retrieve PeptideReadablePtmString
            Long rsetId = m_rsm.getResultSet().getId();
            Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
            ptmStingQuery.setParameter("listId", peptideMap.keySet());
            ptmStingQuery.setParameter("rsetId", rsetId);

            List<Object[]> ptmStrings = ptmStingQuery.getResultList();
            Iterator<Object[]> it = ptmStrings.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long peptideId = (Long) res[0];
                PeptideReadablePtmString ptmString = (PeptideReadablePtmString) res[1];
                Peptide peptide = peptideMap.get(peptideId);
                peptide.getTransientData().setPeptideReadablePtmString(ptmString);
            }

            
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        

        
        
        return true;
    }
    

    public boolean fetchDataForPeptideMatch() {

        HashMap<Long, Peptide> peptideMap = new HashMap<>();
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();

            int size = m_rsmList.size();
            for (int i = 0; i < size; i++) {
                ResultSummary rsm = m_rsmList.get(i);
                DProteinMatch pm = (m_proteinMatchArray != null) ? m_proteinMatchArray.get(i) : m_proteinMatch;
                if (!needToFetch(pm, rsm)) {
                    continue;
                }

                fetchPeptideData(entityManagerMSI, rsm, pm, peptideMap);
            }
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
 
        
        if (!peptideMap.isEmpty()) {
            EntityManager entityManagerPS = DataStoreConnectorFactory.getInstance().getPsDbConnector().getEntityManagerFactory().createEntityManager();  
            try {

                entityManagerPS.getTransaction().begin();

                fetchPtmData(entityManagerPS, peptideMap);

                entityManagerPS.getTransaction().commit();
            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                entityManagerPS.getTransaction().rollback();
                return false;
            } finally {
                entityManagerPS.close();
            }
        }
        
        return true;
    }
    
    private void fetchPeptideData(EntityManager entityManagerMSI, ResultSummary rsm, DProteinMatch proteinMatch, HashMap<Long, Peptide> peptideMap) {

        // Retrieve peptideSet of a proteinMatch
        PeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
        if (peptideSet == null) {
            TypedQuery<PeptideSet> peptideSetQuery = entityManagerMSI.createQuery("SELECT ps FROM PeptideSet ps, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId=:proteinMatchId AND ps_to_pm.id.peptideSetId=ps.id AND ps_to_pm.resultSummary.id=:rsmId", PeptideSet.class);
            peptideSetQuery.setParameter("proteinMatchId", proteinMatch.getId());
            peptideSetQuery.setParameter("rsmId", rsm.getId());
            peptideSet = peptideSetQuery.getSingleResult();
            proteinMatch.setPeptideSet(rsm.getId(), peptideSet);
        }

        //JPM.TODO : speed up the peptidesQuery
        
        // Retrieve the list of PeptideInstance, PeptideMatch, Peptide, MSQuery, Spectrum of a PeptideSet
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, p, sm, ms.id, ms.initialId, sp, pm.cdPrettyRank, pm.sdPrettyRank FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_to_pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.SequenceMatch as sm, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp WHERE ps_to_pi.peptideSet.id=:peptideSetId AND ps_to_pi.peptideInstance.id=pi.id AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id AND sm.id.proteinMatchId=:proteinMatchId AND sm.id.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ORDER BY pm.score DESC");

        peptidesQuery.setParameter("peptideSetId", peptideSet.getId());
        peptidesQuery.setParameter("proteinMatchId", proteinMatch.getId());

        HashMap<Long, Peptide> peptideMapForPtm = new  HashMap<>();
        
        ArrayList<DPeptideInstance> peptideInstanceList = new ArrayList<>();
        List l = peptidesQuery.getResultList(); 
        Iterator<Object[]> itPeptidesQuery = l.iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            PeptideInstance pi = (PeptideInstance) resCur[0];
            DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
            
            Long pmId = (Long) resCur[1];
            Integer pmRank = (Integer) resCur[2];
            Integer pmCharge = (Integer) resCur[3];
            Float pmDeltaMoz = (Float) resCur[4];
            Double pmExperimentalMoz = (Double) resCur[5];
            Integer pmMissedCleavage = (Integer) resCur[6];
            Float pmScore = (Float) resCur[7];
            Long pmResultSetId = (Long) resCur[8];
            Integer pmCdPrettyRank = (Integer) resCur[14];
            Integer pmSdPrettyRank = (Integer) resCur[15];
            DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);

            Peptide p = (Peptide) resCur[9];
            peptideMapForPtm.put(p.getId(), p);

            SequenceMatch sm = (SequenceMatch) resCur[10];
            Long msqId = (Long) resCur[11];
            Integer msqInitialId = (Integer) resCur[12];
            
            Spectrum sp = (Spectrum) resCur[13];
            
            DMsQuery msq = new DMsQuery(pmId, msqId, msqInitialId, sp.getPrecursorIntensity());

            dpi.setBestPeptideMatch(pm);


            pm.setSequenceMatch(sm);
            //p.getTransientData().setSequenceMatch(sm);
            p.getTransientData().setPeptideReadablePtmStringLoaded();

            pm.setPeptide(p);
            pm.setMsQuery(msq);

            peptideInstanceList.add(dpi);
        }


        // Retrieve PeptideReadablePtmString
        Long rsetId = rsm.getResultSet().getId();
        Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
        ptmStingQuery.setParameter("listId", peptideMapForPtm.keySet());
        ptmStingQuery.setParameter("rsetId", rsetId);

        List<Object[]> ptmStrings = ptmStingQuery.getResultList();
        Iterator<Object[]> it = ptmStrings.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Long peptideId = (Long) res[0];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString) res[1];
            Peptide peptide = peptideMapForPtm.get(peptideId);
            peptide.getTransientData().setPeptideReadablePtmString(ptmString);
        }

        
        int nbPeptides = peptideInstanceList.size();
        DPeptideInstance[] peptideInstances = peptideInstanceList.toArray(new DPeptideInstance[nbPeptides]);
        peptideSet.setTransientDPeptideInstances(peptideInstances);

        
        for (int i = 0; i < nbPeptides; i++) {
            peptideMap.put(peptideInstances[i].getPeptideId(), ((DPeptideMatch)peptideInstances[i].getBestPeptideMatch()).getPeptide());
        }


        // Retrieve the list of Protein Sets of Peptides
        // typical ProteinMatch is loaded in the same time, to avoid lazy fetch 
        Query proteinGroupsQuery = entityManagerMSI.createQuery("SELECT ps.id, p, pm.id, pm.accession, pm.score, pm.peptideCount, pm.description FROM fr.proline.core.orm.msi.ProteinMatch pm, fr.proline.core.orm.msi.ProteinSet ps, fr.proline.core.orm.msi.SequenceMatch sm, fr.proline.core.orm.msi.Peptide p WHERE ps.typicalProteinMatchId=pm.id AND pm.id=sm.id.proteinMatchId AND p.id=sm.id.peptideId AND p.id IN (:peptideIds) AND sm.resultSetId=:rsetId AND ps.resultSummary.id=:rsmId AND ps.isValidated=true");
        proteinGroupsQuery.setParameter("rsmId", rsm.getId());
        proteinGroupsQuery.setParameter("rsetId", rsm.getResultSet().getId());
        proteinGroupsQuery.setParameter("peptideIds", peptideMap.keySet());
        l = proteinGroupsQuery.getResultList();
        Iterator<Object[]> itProteinGroupsQuery = l.iterator();
        while (itProteinGroupsQuery.hasNext()) {
            Object[] resCur = itProteinGroupsQuery.next();
            Long proteinSetId = (Long) resCur[0];
            Peptide p = (Peptide) resCur[1];
            
            
            
            Long proteinMatchId = (Long) resCur[2];
            String accession = (String) resCur[3];
            Float score = (Float) resCur[4];
            Integer peptideCount = (Integer) resCur[5];
            String description = (String) resCur[6];

            DProteinMatch pm = new DProteinMatch(proteinMatchId, accession, score, peptideCount, rsm.getResultSet().getId(), description);
            
            DProteinSet proteinSet = new DProteinSet(proteinSetId, proteinMatchId, rsm.getId());

            proteinSet.setTypicalProteinMatch(pm);

            ArrayList<DProteinSet> proteinSetArray = p.getTransientData().getProteinSetArray();
            if (proteinSetArray == null) {
                proteinSetArray = new ArrayList<>();
                p.getTransientData().setProteinSetArray(proteinSetArray);
            }
            proteinSetArray.add(proteinSet);
        }

    }
    
    protected static void fetchPtmData(EntityManager entityManagerPS, HashMap<Long, Peptide> peptideMap) {

        TypedQuery<PeptidePtm> ptmQuery = entityManagerPS.createQuery("SELECT ptm FROM fr.proline.core.orm.ps.PeptidePtm ptm WHERE ptm.peptide.id IN (:peptideIds)", PeptidePtm.class);
        ptmQuery.setParameter("peptideIds", peptideMap.keySet());
        List<PeptidePtm> ptmList = ptmQuery.getResultList();

        Iterator<PeptidePtm> it = ptmList.iterator();
        while (it.hasNext()) {
            PeptidePtm ptm = it.next();

            Peptide p = peptideMap.get(ptm.getPeptide().getId());
            HashMap<Integer, PeptidePtm> map = p.getTransientData().getPeptidePtmMap();
            if (map == null) {
                map = new HashMap<>();
                p.getTransientData().setPeptidePtmMap(map);
            }
            map.put(ptm.getSeqPosition(), ptm);

        }


    }
}
