package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.AccessDatabaseThread;
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

    private ProteinMatch proteinMatch = null;
    private ArrayList<ProteinMatch> proteinMatchArray = null;
    private ArrayList<ResultSummary> rsmList = null;

    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, ProteinMatch proteinMatch, ArrayList<ResultSummary> rsmList) {
        super(callback);
        this.proteinMatch = proteinMatch;
        this.proteinMatchArray = null;
        this.rsmList = rsmList;
    }
    
    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, ArrayList<ProteinMatch> proteinMatchArray, ArrayList<ResultSummary> rsmList) {
        super(callback);
        this.proteinMatch = null;
        this.proteinMatchArray = proteinMatchArray;
        this.rsmList = rsmList;
    }


    @Override
    public boolean needToFetch() {
        int size = rsmList.size();
        for (int i=0;i<size;i++) {
            ResultSummary rsm = rsmList.get(i);
            ProteinMatch pm = (proteinMatchArray!=null) ? proteinMatchArray.get(i) : proteinMatch;
            if (needToFetch(pm, rsm)) {
                return true;
            }
        }
        return false;
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

        HashMap<Integer, Peptide> peptideMap = new HashMap<Integer, Peptide>();
        EntityManager entityManagerMSI = DatabaseManager.getInstance().getMsiDbConnector( AccessDatabaseThread.getProjectIdTMP()).getEntityManagerFactory().createEntityManager();  //JPM.TODO : project id

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
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        EntityManager entityManagerPS = DatabaseManager.getInstance().getPsDbConnector().getEntityManagerFactory().createEntityManager();  

        try {

            entityManagerPS.getTransaction().begin();
            
            fetchPtmData(entityManagerPS, peptideMap);
            
            entityManagerPS.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerPS.close();
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

        ArrayList<PeptideInstance> peptideInstanceList = new ArrayList<PeptideInstance>();
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

            pi.setTransientBestPeptideMatch(pm);


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
            peptideMap.put(peptideInstances[i].getPeptideId(), peptideInstances[i].getTransientBestPeptideMatch().getTransientData().getPeptide());
        }


        // Retrieve the list of Protein Sets of Peptides
        // typical ProteinMatch is loaded in the same time, to avoir lazy fetch 
        Query proteinGroupsQuery = entityManagerMSI.createQuery("SELECT ps, p, pm FROM fr.proline.core.orm.msi.ProteinMatch pm, fr.proline.core.orm.msi.ProteinSet ps, fr.proline.core.orm.msi.SequenceMatch sm, fr.proline.core.orm.msi.Peptide p WHERE ps.typicalProteinMatchId=pm.id AND pm.id=sm.id.proteinMatchId AND p.id=sm.id.peptideId AND p.id IN (:peptideIds) AND sm.resultSetId=:rsetId AND ps.resultSummary.id=:rsmId");
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
                map = new HashMap<Integer, PeptidePtm>();
                p.getTransientData().setPeptidePtmMap(map);
            }
            map.put(ptm.getSeqPosition(), ptm);

        }


    }
}
