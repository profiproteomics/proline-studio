package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
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

    private int action;
    
    private final static int LOAD_PEPTIDE_INSTANCE_FOR_PEPTIDE_MATCH   = 0;
    private final static int LOAD_PEPTIDE_INSTANCES_FOR_RSM   = 1;
    
    private Integer projectId = null;
    private ProteinMatch proteinMatch = null;
    private ArrayList<ProteinMatch> proteinMatchArray = null;
    private ArrayList<ResultSummary> rsmList = null;
    
    private ResultSummary rsm = null;

    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, Integer projectId, ProteinMatch proteinMatch, ArrayList<ResultSummary> rsmList) {
        super(callback, new TaskInfo("Load Peptide Sets for Protein Match "+proteinMatch.getAccession(), TASK_LIST_INFO));
        this.projectId = projectId;
        this.proteinMatch = proteinMatch;
        this.proteinMatchArray = null;
        this.rsmList = rsmList;
        action = LOAD_PEPTIDE_INSTANCE_FOR_PEPTIDE_MATCH;
        
    }
    
    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, Integer projectId, ArrayList<ProteinMatch> proteinMatchArray, ArrayList<ResultSummary> rsmList) {
        super(callback, new TaskInfo("Load Peptide Sets for multiple Protein Matches", TASK_LIST_INFO));
        this.projectId = projectId;
        this.proteinMatch = null;
        this.proteinMatchArray = proteinMatchArray;
        this.rsmList = rsmList;
        action = LOAD_PEPTIDE_INSTANCE_FOR_PEPTIDE_MATCH;
    }

    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, Integer projectId, ResultSummary rsm) {
        super(callback, new TaskInfo("Load Peptides for Identification Summary "+rsm.getId(), TASK_LIST_INFO));
        this.projectId = projectId;
        this.rsm = rsm;
        action = LOAD_PEPTIDE_INSTANCES_FOR_RSM;
    }
    

    @Override
    public boolean needToFetch() {
        switch(action) {
            case LOAD_PEPTIDE_INSTANCE_FOR_PEPTIDE_MATCH: {
                int size = rsmList.size();
                for (int i = 0; i < size; i++) {
                    ResultSummary rsm = rsmList.get(i);
                    ProteinMatch pm = (proteinMatchArray != null) ? proteinMatchArray.get(i) : proteinMatch;
                    if (needToFetch(pm, rsm)) {
                        return true;
                    }
                }
                return false;
            }
            case LOAD_PEPTIDE_INSTANCES_FOR_RSM: {
                return (rsm.getTransientData().getPeptideInstanceArray() == null);
            }
                
                
        }

        return false; // should not be called
    }
    
    private boolean needToFetch(ProteinMatch proteinMatch, ResultSummary rsm) {
        if (proteinMatch == null) {
            return false;
        }
        PeptideSet peptideSet = proteinMatch.getTransientData().getPeptideSet(rsm.getId());
        if (peptideSet == null) {
            return true;
        }
        return ( peptideSet.getTransientPeptideInstances() == null);
    }
    

    @Override
    public boolean fetchData() {
        switch (action) {
            case LOAD_PEPTIDE_INSTANCE_FOR_PEPTIDE_MATCH: {
                return fetchDataForPeptideMatch();
            }
            case LOAD_PEPTIDE_INSTANCES_FOR_RSM: {
                return fetchDataForRsm();
            }
        }
        return false;
    }
    

    public boolean fetchDataForRsm() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();

            ArrayList<PeptideInstance> peptideInstanceList = new ArrayList<>();

            Query peptideInstancesQuery = entityManagerMSI.createQuery("SELECT pi, pm, p FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pi.resultSummary.id=:rsmId AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id ORDER BY pm.score DESC");
            peptideInstancesQuery.setParameter("rsmId", rsm.getId());

            List l = peptideInstancesQuery.getResultList();
            Iterator<Object[]> itPeptidesQuery = l.iterator();
            while (itPeptidesQuery.hasNext()) {
                Object[] resCur = itPeptidesQuery.next();
                PeptideInstance pi = (PeptideInstance) resCur[0];
                PeptideMatch pm = (PeptideMatch) resCur[1];
                Peptide p = (Peptide) resCur[2];


                pi.getTransientData().setBestPeptideMatch(pm);

                pm.getTransientData().setPeptide(p);

                peptideInstanceList.add(pi);
            }

            int nbPeptides = peptideInstanceList.size();
            PeptideInstance[] peptideInstances = peptideInstanceList.toArray(new PeptideInstance[nbPeptides]);
            rsm.getTransientData().setPeptideInstanceArray(peptideInstances);

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + " failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        return true;
    }
    

    public boolean fetchDataForPeptideMatch() {

        HashMap<Integer, Peptide> peptideMap = new HashMap<>();
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();

            int size = rsmList.size();
            for (int i = 0; i < size; i++) {
                ResultSummary rsm = rsmList.get(i);
                ProteinMatch pm = (proteinMatchArray != null) ? proteinMatchArray.get(i) : proteinMatch;
                if (!needToFetch(pm, rsm)) {
                    continue;
                }

                fetchPeptideData(entityManagerMSI, rsm, pm, peptideMap);
            }
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }
        EntityManager entityManagerPS = null;
        try {
        
            DataStoreConnectorFactory.getInstance();
            DataStoreConnectorFactory.getInstance().getPsDbConnector();
            DataStoreConnectorFactory.getInstance().getPsDbConnector().getEntityManagerFactory();
            
        entityManagerPS = DataStoreConnectorFactory.getInstance().getPsDbConnector().getEntityManagerFactory().createEntityManager();  
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (!peptideMap.isEmpty()) {
            try {

                entityManagerPS.getTransaction().begin();

                fetchPtmData(entityManagerPS, peptideMap);

                entityManagerPS.getTransaction().commit();
            } catch (Exception e) {
                logger.error(getClass().getSimpleName() + " failed", e);
                return false;
            } finally {
                entityManagerPS.close();
            }
        }
        
        return true;
    }
    
    private void fetchPeptideData(EntityManager entityManagerMSI, ResultSummary rsm, ProteinMatch proteinMatch, HashMap<Integer, Peptide> peptideMap) {

        // Retrieve peptideSet of a proteinMatch
        PeptideSet peptideSet = proteinMatch.getTransientData().getPeptideSet(rsm.getId());
        if (peptideSet == null) {
            TypedQuery<PeptideSet> peptideSetQuery = entityManagerMSI.createQuery("SELECT ps FROM PeptideSet ps, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId=:proteinMatchId AND ps_to_pm.id.peptideSetId=ps.id AND ps_to_pm.resultSummary.id=:rsmId", PeptideSet.class);
            peptideSetQuery.setParameter("proteinMatchId", proteinMatch.getId());
            peptideSetQuery.setParameter("rsmId", rsm.getId());
            peptideSet = peptideSetQuery.getSingleResult();
            proteinMatch.getTransientData().setPeptideSet(rsm.getId(), peptideSet);
        }

        // Retrieve the list of PeptideInstance, PeptideMatch, Peptide, MSQuery, Spectrum of a PeptideSet
        // MSQuery and Spectrum are loaded to force lazy fetch.
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pi, pm, p, sm, ms, sp  FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_to_pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.SequenceMatch as sm, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp WHERE ps_to_pi.peptideSet.id=:peptideSetId AND ps_to_pi.peptideInstance.id=pi.id AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id AND sm.id.proteinMatchId=:proteinMatchId AND sm.id.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ORDER BY pm.score DESC");

        peptidesQuery.setParameter("peptideSetId", peptideSet.getId());
        peptidesQuery.setParameter("proteinMatchId", proteinMatch.getId());

        ArrayList<PeptideInstance> peptideInstanceList = new ArrayList<>();
        List l = peptidesQuery.getResultList();
        Iterator<Object[]> itPeptidesQuery = l.iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            PeptideInstance pi = (PeptideInstance) resCur[0];
            PeptideMatch pm = (PeptideMatch) resCur[1];
            Peptide p = (Peptide) resCur[2];

            SequenceMatch sm = (SequenceMatch) resCur[3];
            MsQuery msq = (MsQuery) resCur[4];
            Spectrum sp = (Spectrum) resCur[5];

            pi.getTransientData().setBestPeptideMatch(pm);


            p.getTransientData().setSequenceMatch(sm);

            PeptideMatch.TransientData pmData = pm.getTransientData();
            pmData.setPeptide(p);
            pmData.setIsMsQuerySet(true);

            peptideInstanceList.add(pi);
        }

        int nbPeptides = peptideInstanceList.size();
        PeptideInstance[] peptideInstances = peptideInstanceList.toArray(new PeptideInstance[nbPeptides]);
        peptideSet.setTransientPeptideInstances(peptideInstances);

        
        for (int i = 0; i < nbPeptides; i++) {
            peptideMap.put(peptideInstances[i].getPeptideId(), peptideInstances[i].getTransientData().getBestPeptideMatch().getTransientData().getPeptide());
        }


        // Retrieve the list of Protein Sets of Peptides
        // typical ProteinMatch is loaded in the same time, to avoid lazy fetch 
        Query proteinGroupsQuery = entityManagerMSI.createQuery("SELECT ps, p, pm FROM fr.proline.core.orm.msi.ProteinMatch pm, fr.proline.core.orm.msi.ProteinSet ps, fr.proline.core.orm.msi.SequenceMatch sm, fr.proline.core.orm.msi.Peptide p WHERE ps.typicalProteinMatchId=pm.id AND pm.id=sm.id.proteinMatchId AND p.id=sm.id.peptideId AND p.id IN (:peptideIds) AND sm.resultSetId=:rsetId AND ps.resultSummary.id=:rsmId AND ps.isValidated=true");
        proteinGroupsQuery.setParameter("rsmId", rsm.getId());
        proteinGroupsQuery.setParameter("rsetId", rsm.getResultSet().getId());
        proteinGroupsQuery.setParameter("peptideIds", peptideMap.keySet());
        l = proteinGroupsQuery.getResultList();
        Iterator<Object[]> itProteinGroupsQuery = l.iterator();
        while (itProteinGroupsQuery.hasNext()) {
            Object[] resCur = itProteinGroupsQuery.next();
            ProteinSet ps = (ProteinSet) resCur[0];
            Peptide p = (Peptide) resCur[1];
            ProteinMatch pm = (ProteinMatch) resCur[2];
            ProteinSet.TransientData data = ps.getTransientData();
            if (data.getTypicalProteinMatch() == null) {
                data.setTypicalProteinMatch(pm);
            }

            ArrayList<ProteinSet> proteinSetArray = p.getTransientData().getProteinSetArray();
            if (proteinSetArray == null) {
                proteinSetArray = new ArrayList<ProteinSet>();
                p.getTransientData().setProteinSetArray(proteinSetArray);
            }
            proteinSetArray.add(ps);
        }

    }
    
    private void fetchPtmData(EntityManager entityManagerPS, HashMap<Integer, Peptide> peptideMap) {

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
