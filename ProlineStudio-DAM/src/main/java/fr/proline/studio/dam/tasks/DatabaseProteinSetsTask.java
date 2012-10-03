package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ProteinSet.TransientProteinSetData;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.*;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


/**
 * Load Protein Sets and their Typical ProteinMatch of a Result Summary
 * @author JM235353
 */
public class DatabaseProteinSetsTask extends AbstractDatabaseTask {
    
    private ResultSummary rsm = null;

    public DatabaseProteinSetsTask(AbstractDatabaseCallback callback, ResultSummary rsm) {
        super(callback);
        this.rsm = rsm;        
    }

    
    @Override
    public boolean needToFetch() {
        return (rsm.getTransientProteinSets() == null);
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {
            
            entityManagerMSI.getTransaction().begin();
            
            Integer rsmId = rsm.getId();

            long timeStart = System.currentTimeMillis();

            // Load Protein Sets
            TypedQuery<ProteinSet> proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps FROM ProteinSet ps WHERE ps.resultSummary.id=:rsmId ORDER BY ps.score DESC", ProteinSet.class);
            proteinSetsQuery.setParameter("rsmId", rsmId);
            List<ProteinSet> proteinSets = proteinSetsQuery.getResultList();
            
            ProteinSet[] proteinSetArray = proteinSets.toArray(new ProteinSet[proteinSets.size()]);
            rsm.setTransientProteinSets(proteinSetArray);
            
            
            long timeStop = System.currentTimeMillis();
            double delta = ((double) (timeStop-timeStart))/1000;
            System.out.println("Load Protein Sets : "+delta);
            timeStart = System.currentTimeMillis();
            
            
            // Load Typical Protein Match for each  Protein Set
            ArrayList<Integer> ProteinMatchIds = new ArrayList<Integer>(proteinSetArray.length);
            
            int nbProteinSets = proteinSetArray.length;
            for (int i=0;i<nbProteinSets;i++) {
                ProteinMatchIds.add(i, proteinSetArray[i].getProteinMatchId());
                proteinSetArray[i].getResultSummary(); // force fetch of lazy data
            }
            TypedQuery<ProteinMatch> typicalProteinQuery = entityManagerMSI.createQuery("SELECT pm FROM ProteinMatch pm WHERE pm.id IN (:listId)", ProteinMatch.class);
            typicalProteinQuery.setParameter("listId", ProteinMatchIds);
            List<ProteinMatch> typicalProteinMatches = typicalProteinQuery.getResultList();
            HashMap<Integer,ProteinMatch> typicalProteinMap = new HashMap<Integer,ProteinMatch>();
            Iterator<ProteinMatch> itTypical = typicalProteinMatches.iterator();
            while (itTypical.hasNext()) {
                ProteinMatch pmCur = itTypical.next();
                typicalProteinMap.put(pmCur.getId(), pmCur);
                pmCur.getPeptideCount();  // force fetch of lazy data
            }
            for (int i=0;i<nbProteinSets;i++) {
                ProteinSet proteinSetCur = proteinSetArray[i];
                ProteinSet.TransientProteinSetData proteinSetData = new ProteinSet.TransientProteinSetData();
                proteinSetCur.setTransientProteinSetData(proteinSetData);
                proteinSetData.setTypicalProteinMatch(typicalProteinMap.get(proteinSetCur.getProteinMatchId()));
            }

            
            
                    
            

            
            timeStop = System.currentTimeMillis();
            delta = ((double) (timeStop-timeStart))/1000;
            System.out.println("Load Typical Protein : "+delta);
            timeStart = System.currentTimeMillis();
            
            
            
            
            // Prepare Spectral count query
            HashMap<Integer,Integer> spectralCountMap = new HashMap<Integer,Integer>();

            
            String spectralCountQueryStringPart1 = "SELECT ps_to_pm.id.proteinMatchId, count(pi.peptideMatchCount) FROM PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId IN (:proteinMatchIds) AND ps_to_pm.resultSummary.id=:rsmId AND ps_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pm.resultSummary.id=ps_to_pi.resultSummary.id AND ps_to_pi.id.peptideInstanceId=pi.id";
            String spectralCountQueryString = spectralCountQueryStringPart1+" GROUP BY ps_to_pm.id.proteinMatchId";
            
            Query spectralCountQuery = entityManagerMSI.createQuery(spectralCountQueryString);
            spectralCountQuery.setParameter("proteinMatchIds", ProteinMatchIds); 
            spectralCountQuery.setParameter("rsmId", rsmId);
            
            List <Object[]> spectralCountRes = spectralCountQuery.getResultList();
            Iterator<Object[]> spectralCountResIt = spectralCountRes.iterator();
            while (spectralCountResIt.hasNext()) {
                Object[] cur = spectralCountResIt.next();
                Integer proteinMatchId = (Integer) cur[0];
                Integer spectralCount = ((Long) cur[1]).intValue();
                spectralCountMap.put(proteinMatchId, spectralCount);
            }
            for (int i=0;i<nbProteinSets;i++) {
                ProteinSet proteinSetCur = proteinSetArray[i];
                ProteinSet.TransientProteinSetData proteinSetData = proteinSetCur.getTransientProteinSetData();

                Integer spectralCount = spectralCountMap.get(proteinSetCur.getProteinMatchId());
                if (spectralCount != null) {  // should not happen
                    proteinSetData.setSpectralCount( spectralCount );
                }
            }
            
            
       
            timeStop = System.currentTimeMillis();
            delta = ((double) (timeStop-timeStart))/1000;
            System.out.println("Calculate Spectral Count : "+delta);
            timeStart = System.currentTimeMillis();
                
            
            // Calculate Specific Spectral Count
            
            // Prepare Specific Spectral count query
            spectralCountMap.clear();
            String specificSpectralCountQueryString = spectralCountQueryStringPart1 + " AND pi.proteinSetCount=1 GROUP BY ps_to_pm.id.proteinMatchId";
            
            Query specificSpectralCountQuery = entityManagerMSI.createQuery(specificSpectralCountQueryString);
            specificSpectralCountQuery.setParameter("proteinMatchIds", ProteinMatchIds); 
            specificSpectralCountQuery.setParameter("rsmId", rsmId);
            
            List <Object[]> specificSpectralCountRes = specificSpectralCountQuery.getResultList();
            Iterator<Object[]> specificSpectralCountResIt = specificSpectralCountRes.iterator();
            while (specificSpectralCountResIt.hasNext()) {
                Object[] cur = specificSpectralCountResIt.next();
                Integer proteinMatchId = (Integer) cur[0];
                Integer specificSpectralCount = ((Long) cur[1]).intValue();
                spectralCountMap.put(proteinMatchId, specificSpectralCount);
            }
            for (int i=0;i<nbProteinSets;i++) {
                ProteinSet proteinSetCur = proteinSetArray[i];
                ProteinSet.TransientProteinSetData proteinSetData = proteinSetCur.getTransientProteinSetData();

                Integer specificSpectralCount = spectralCountMap.get(proteinSetCur.getProteinMatchId());
                if (specificSpectralCount != null) {  // should not happen
                    proteinSetData.setSpecificSpectralCount( specificSpectralCount );
                }
            }
            
            
            timeStop = System.currentTimeMillis();
            delta = ((double) (timeStop-timeStart))/1000;
            System.out.println("Calculate Specific Spectral Count : "+delta);
            timeStart = System.currentTimeMillis();
            
            
            
            
            // SameSet count query
            /**
             SELECT ps, count(pm) 
             FROM   ProteinMatch pm, ProteinMatch typicalPm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps  
             WHERE  ps_to_pm.proteinSet.id IN (:proteinSetIds) AND
                    ps_to_pm.proteinSet.id = ps.id AND
                    ps_to_pm.proteinMatch.id=pm.id AND
                    ps_to_pm.resultSummary.id=:rsmId AND
                    ps.typicalProteinMatchId = typicalPm.id AND
                    pm.peptideCount=typicalPm.peptideCount
             GROUP BY ps
            */
            String sameSetCountQueryString = "SELECT ps, count(pm) FROM   ProteinMatch pm, ProteinMatch typicalPm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE  ps_to_pm.proteinSet.id IN (:proteinSetIds) AND ps_to_pm.proteinSet.id = ps.id AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND ps.typicalProteinMatchId = typicalPm.id AND pm.peptideCount=typicalPm.peptideCount GROUP BY ps";
            Query sameSetCountQuery = entityManagerMSI.createQuery(sameSetCountQueryString);
            
            // prepare the list of ProteinSet Ids
            ArrayList<Integer> proteinSetIds = new ArrayList<Integer>(proteinSetArray.length);
            
            int nb = proteinSetArray.length;
            for (int i=0;i<nb;i++) {
                proteinSetIds.add(i, proteinSetArray[i].getId());
            }
            
            sameSetCountQuery.setParameter("proteinSetIds", proteinSetIds); 
            sameSetCountQuery.setParameter("rsmId", rsmId);
            List <Object[]> sameSetCountRes = sameSetCountQuery.getResultList();
            Iterator<Object[]> sameSetCountResIt = sameSetCountRes.iterator();
            while (sameSetCountResIt.hasNext()) {
                Object[] cur = sameSetCountResIt.next();
                ProteinSet proteinSet = (ProteinSet) cur[0];
                int sameSetCount = ((Long) cur[1]).intValue();
                proteinSet.getTransientProteinSetData().setSameSetCount(sameSetCount);
            }
            
           // All proteins in Protein Group count query
           // -> used to know number of proteins in Sub set
            /**
             SELECT ps, count(pm) 
             FROM   ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps  
             WHERE  ps_to_pm.proteinSet.id IN (:proteinSetIds) AND
                    ps_to_pm.proteinSet.id = ps.id AND
                    ps_to_pm.proteinMatch.id=pm.id AND
                    ps_to_pm.resultSummary.id=:rsmId
             GROUP BY ps
            */
                         
            
            String allCountQueryString = "SELECT ps, count(pm) FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE  ps_to_pm.proteinSet.id IN (:proteinSetIds) AND ps_to_pm.proteinSet.id = ps.id AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId GROUP BY ps";
            Query allCountQuery = entityManagerMSI.createQuery(allCountQueryString);
            allCountQuery.setParameter("proteinSetIds", proteinSetIds); 
            allCountQuery.setParameter("rsmId", rsmId);
            List <Object[]> allCountRes = allCountQuery.getResultList();
            Iterator<Object[]> allCountResIt = allCountRes.iterator();
            while (allCountResIt.hasNext()) {
                Object[] cur = allCountResIt.next();
                ProteinSet proteinSet = (ProteinSet) cur[0];
                int allCount = ((Long) cur[1]).intValue();
                ProteinSet.TransientProteinSetData data = proteinSet.getTransientProteinSetData();
                data.setSubSetCount(allCount-data.getSameSetCount());
            }
            
            
            // Prepare SameSet count query
            /*String sameSetCountQueryString = "SELECT count(pm) FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm WHERE ps_to_pm.proteinSet.id=:proteinSetId AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND pm.peptideCount=:bestPeptideCount";
            TypedQuery<Long> sameSetCountQuery = entityManagerMSI.createQuery(sameSetCountQueryString, Long.class);
            
            // Prepare SameSet and Subset count query
            String setCountQueryString = "SELECT count(pm) FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm WHERE ps_to_pm.proteinSet.id=:proteinSetId AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId";
            TypedQuery<Long> setCountQuery = entityManagerMSI.createQuery(setCountQueryString, Long.class);
           
            // Calculate SameSet and SubSet sizes
            for (int i=0;i<nbProteinSets;i++) {
                ProteinSet proteinSetCur = proteinSetArray[i];
                ProteinSet.TransientProteinSetData proteinSetData = proteinSetCur.getTransientProteinSetData();
                ProteinMatch proteinMatch = proteinSetData.getTypicalProteinMatch();
                int peptitesCountInSameSet = proteinMatch.getPeptideCount();
                sameSetCountQuery.setParameter("proteinSetId", proteinSetCur.getId()); 
                sameSetCountQuery.setParameter("rsmId", rsmId);
                sameSetCountQuery.setParameter("bestPeptideCount", peptitesCountInSameSet);
                long sameSetCountDirect = sameSetCountQuery.getSingleResult();
                proteinSetCur.getTransientProteinSetData().setSameSetCount((int)sameSetCountDirect);
                
                setCountQuery.setParameter("proteinSetId", proteinSetCur.getId()); 
                setCountQuery.setParameter("rsmId", rsmId);
                long subSetCountDirect = setCountQuery.getSingleResult()-sameSetCountDirect;
                proteinSetCur.getTransientProteinSetData().setSubSetCount((int)subSetCountDirect);

            
            }*/
            
            timeStop = System.currentTimeMillis();
            delta = ((double) (timeStop-timeStart))/1000;
            System.out.println("Calculate SameSet and SubSet : "+delta);
            timeStart = System.currentTimeMillis();
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error("DatabaseProteinSetsAction failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    
}
