package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Task to load Protein Sets for a Protein Match
 * @author JM235353
 */
public class DatabaseLoadProteinSetsFromProteinTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private ArrayList<ProteinMatch> m_proteinMatchArray = null;
    private ArrayList<Long> m_resultSetIdArray = null;
    private String m_proteinMatchName = null;
    
    public DatabaseLoadProteinSetsFromProteinTask(AbstractDatabaseCallback callback, long projectId, ProteinMatch proteinMatch) {
        super(callback, Priority.NORMAL_3, new TaskInfo("Load Protein Sets for Protein Match "+proteinMatch.getAccession(), TASK_LIST_INFO));
        m_projectId = projectId;
        m_proteinMatchArray = new ArrayList<>(1);
        m_proteinMatchArray.add(proteinMatch);        
    }
    
    public DatabaseLoadProteinSetsFromProteinTask(AbstractDatabaseCallback callback, long projectId, ArrayList<ProteinMatch> proteinMatchArray) {
        super(callback, Priority.NORMAL_3, new TaskInfo("Load Protein Sets for multiple Protein Matches", TASK_LIST_INFO));
        m_projectId = projectId;
        m_proteinMatchArray = proteinMatchArray;        
    }
    
    public DatabaseLoadProteinSetsFromProteinTask(AbstractDatabaseCallback callback, long projectId, ArrayList<ProteinMatch> proteinMatchArray, ArrayList<Long> resultSetIdArray, String proteinMatchName ) {
        super(callback, Priority.NORMAL_3, new TaskInfo("Load Protein Match from its name "+proteinMatchName, TASK_LIST_INFO));
        m_projectId = projectId;
        m_proteinMatchArray = proteinMatchArray;   
        m_resultSetIdArray = resultSetIdArray;
        m_proteinMatchName = proteinMatchName;
        
    }
    
    @Override
    public void abortTask() {
        // nothing to do for task which are not inherited from AbstractDatabaseSlicerTask 
    }
    
    @Override
    public boolean needToFetch() {
        int size = m_proteinMatchArray.size();
        for (int i=0;i<size;i++) {
            if (m_proteinMatchArray.get(i).getTransientData().getProteinSetArray() == null) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            int size = m_proteinMatchArray.size();
            for (int i=0;i<size;i++) {
                ProteinMatch proteinMatch = m_proteinMatchArray.get(i);
                if (proteinMatch == null) {
                    // we need to load this proteinMatch from its name and its resultSetId
                    Long resultSetId = m_resultSetIdArray.get(i);
                    proteinMatch = fetchAProteinMatch(entityManagerMSI, m_proteinMatchName, resultSetId); // JPM.TODO : was used for DataBoxProteinSetsCmp : no longer used : remove it or repare it
                    if (proteinMatch == null) {
                        continue;
                    } else {
                        m_proteinMatchArray.set(i, proteinMatch);
                    }
                }
                
                
                if (proteinMatch.getTransientData().getProteinSetArray() != null) {
                    // fetch not needed for this protein match
                    continue;
                }
                fetchDataForProteinMatch(entityManagerMSI, proteinMatch);
            }
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        // clean up protein match not found if needed
        while (m_proteinMatchArray.remove(null)) {}
        
        
        return true;
    }

    // JPM.TODO : was used for DataBoxProteinSetsCmp : no longer used : remove it or repare it
    private ProteinMatch fetchAProteinMatch(EntityManager entityManagerMSI, String proteinMatchName, Long resultSetId) {
        
        TypedQuery<ProteinMatch> rsmQuery = entityManagerMSI.createQuery("SELECT pm FROM ProteinMatch pm WHERE pm.resultSet.id=:resultSetId", ProteinMatch.class);
        rsmQuery.setParameter("resultSetId", resultSetId);

        return rsmQuery.getSingleResult();

    }
    private void fetchDataForProteinMatch(EntityManager entityManagerMSI, ProteinMatch proteinMatch) {



        // Load Protein Sets and their typical Protein Match in the same time
        Query proteinSetQuery = entityManagerMSI.createQuery("SELECT ps, typicalPm FROM ProteinMatch typicalPm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE ps_to_pm.proteinSet.id=ps.id AND ps_to_pm.proteinMatch.id=:proteinMatchId AND ps.typicalProteinMatchId=typicalPm.id AND ps.isValidated=true ORDER BY ps_to_pm.resultSummary.id ASC");
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
