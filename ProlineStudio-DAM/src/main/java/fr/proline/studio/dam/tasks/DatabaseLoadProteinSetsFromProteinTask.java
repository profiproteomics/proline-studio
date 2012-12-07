package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadProteinSetsFromProteinTask extends AbstractDatabaseTask {

    private ArrayList<ProteinMatch> proteinMatchArray = null;
    private ArrayList<Integer> resultSetIdArray = null;
    private String proteinMatchName = null;
    
    public DatabaseLoadProteinSetsFromProteinTask(AbstractDatabaseCallback callback, ProteinMatch proteinMatch) {
        super(callback, Priority.NORMAL_3);
        this.proteinMatchArray = new ArrayList<ProteinMatch>(1);
        this.proteinMatchArray.add(proteinMatch);        
    }
    
    public DatabaseLoadProteinSetsFromProteinTask(AbstractDatabaseCallback callback, ArrayList<ProteinMatch> proteinMatchArray) {
        super(callback, Priority.NORMAL_3);
        this.proteinMatchArray = proteinMatchArray;        
    }
    
    public DatabaseLoadProteinSetsFromProteinTask(AbstractDatabaseCallback callback, ArrayList<ProteinMatch> proteinMatchArray, ArrayList<Integer> resultSetIdArray, String proteinMatchName ) {
        super(callback, Priority.NORMAL_3);
        this.proteinMatchArray = proteinMatchArray;   
        this.resultSetIdArray = resultSetIdArray;
        this.proteinMatchName = proteinMatchName;
        
    }
    
    @Override
    public boolean needToFetch() {
        int size = proteinMatchArray.size();
        for (int i=0;i<size;i++) {
            if (proteinMatchArray.get(i).getTransientData().getProteinSetArray() == null) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            int size = proteinMatchArray.size();
            for (int i=0;i<size;i++) {
                ProteinMatch proteinMatch = proteinMatchArray.get(i);
                if (proteinMatch == null) {
                    // we need to load this proteinMatch from its name and its resultSetId
                    Integer resultSetId = resultSetIdArray.get(i);
                    proteinMatch = fetchAProteinMatch(entityManagerMSI, proteinMatchName, resultSetId);
                    if (proteinMatch == null) {
                        continue;
                    } else {
                        proteinMatchArray.set(i, proteinMatch);
                    }
                }
                
                
                if (proteinMatch.getTransientData().getProteinSetArray() != null) {
                    // fetch not needed for this protein match
                    continue;
                }
                fetchDataForProteinMatch(entityManagerMSI, proteinMatch);
            }
            
            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        // clean up protein match not found if needed
        while (proteinMatchArray.remove(null)) {}
        
        
        return true;
    }

    private ProteinMatch fetchAProteinMatch(EntityManager entityManagerMSI, String proteinMatchName, Integer resultSetId) {
        
        TypedQuery<ProteinMatch> rsmQuery = entityManagerMSI.createQuery("SELECT pm FROM ProteinMatch pm WHERE pm.resultSet.id=:resultSetId", ProteinMatch.class);
        rsmQuery.setParameter("resultSetId", resultSetId);

        return rsmQuery.getSingleResult();

    }
    private void fetchDataForProteinMatch(EntityManager entityManagerMSI, ProteinMatch proteinMatch) {



        // Load Protein Sets and their typical Protein Match in the same time
        Query proteinSetQuery = entityManagerMSI.createQuery("SELECT ps, typicalPm FROM ProteinMatch typicalPm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE ps_to_pm.proteinSet.id=ps.id AND ps_to_pm.proteinMatch.id=:proteinMatchId AND ps.typicalProteinMatchId=typicalPm.id ORDER BY ps_to_pm.resultSummary.id ASC");
        proteinSetQuery.setParameter("proteinMatchId", proteinMatch.getId());
        List l = proteinSetQuery.getResultList();

        ProteinSet[] proteinSetArray = new ProteinSet[l.size()];

        int index = 0;
        Iterator<Object[]> itProteinSetsQuery = l.iterator();
        while (itProteinSetsQuery.hasNext()) {
            Object[] resCur = itProteinSetsQuery.next();
            ProteinSet ps = (ProteinSet) resCur[0];
            ProteinMatch bestProteinMatch = (ProteinMatch) resCur[1];

            ProteinSet.TransientData data = ps.getTransientData();
            if (data.getTypicalProteinMatch() == null) {
                data.setTypicalProteinMatch(bestProteinMatch);
            }

            proteinSetArray[index++] = ps;
        }


        ProteinMatch.TransientData data = proteinMatch.getTransientData();
        data.setProteinSetArray(proteinSetArray);

        // Load Proteins for each ProteinSet
        for (int i = 0; i < proteinSetArray.length; i++) {
            DatabaseProteinsFromProteinSetTask.fetchProteins(entityManagerMSI, proteinSetArray[i]);
        }

        //JPM.TODO : load name of ResultSummary...



    }
    

    
    
}
