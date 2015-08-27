/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.m_logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.Query;

/**
 * load msQueries for a resultSet
 * @author MB243701
 */
public class DatabaseLoadMSQueriesTask extends AbstractDatabaseSlicerTask {

    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 1000;
    
    public static final int SUB_TASK_COUNT_MSQUERY = 1; // <<----- get in sync  
    public static final int SUB_TASK_MSQUERY = 0;
    
    private long m_projectId = -1;
    private ResultSet m_resultSet;
    private List<MsQuery> m_listMsQueries;
    private Map<Long, Integer> m_msqQueryMap;
    private MsiSearch m_msiSearch;
    private  Map<Long, Integer> m_nbPeptideMatchesByMsQueryIdMap;
    
    // data kept for sub tasks
    private List<Long> m_msQueriesIds = null;
    
    private int action;
    private static final int LOAD_MSQUERY = 0;
    
    
    public DatabaseLoadMSQueriesTask(AbstractDatabaseCallback callback) {
        super(callback);

    }

    public void initLoadMSQueries(long projectId, ResultSet rs, List<MsQuery> listMsQueries, Map<Long, Integer> nbPeptideMatchesByMsQueryIdMap) {
        init(SUB_TASK_COUNT_MSQUERY, new TaskInfo("Load MSQueries for resultSet "+(rs == null? "null":rs.getId()), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_resultSet = rs;
        m_listMsQueries = listMsQueries;
        m_nbPeptideMatchesByMsQueryIdMap = nbPeptideMatchesByMsQueryIdMap;
        action = LOAD_MSQUERY;
    }

    @Override
    public boolean fetchData() {
        if (action == LOAD_MSQUERY) {
            if (needToFetch()) {
                return fetchDataMSQueriesMainTask();
            } else {
                // fetch data of SubTasks
                return fetchMSQueriesDataSubTask();
            }
        }
        return true; // should not happen
    }
    
    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (action) {
            case LOAD_MSQUERY:
                m_resultSet = null;
                break;
        }
    }

    @Override
    public boolean needToFetch() {
        switch (action) {
            case LOAD_MSQUERY:
                return (m_listMsQueries == null || m_listMsQueries.isEmpty());
        }
        return false; // should not happen 
    }
    
    /**
     * Fetch first data to display msQueries and spectrum data
     *
     * @return
     */
    private boolean fetchDataMSQueriesMainTask() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            if (m_resultSet != null){
                Long msiSearchId = m_resultSet.getMsiSearch() == null ? -1: m_resultSet.getMsiSearch().getId();
                String query = "SELECT msq.id "
                        + "FROM fr.proline.core.orm.msi.MsQuery msq, fr.proline.core.orm.msi.Spectrum s, "
                        + "fr.proline.core.orm.msi.Peaklist pl, fr.proline.core.orm.msi.MsiSearch msi "
                        + "WHERE msi.id =:msiSearchId AND "
                        + "msi.peaklist.id = pl.id AND "
                        + "pl.id = s.peaklistId AND "
                        + "s.id = msq.spectrum.id AND "
                        + "msq.msiSearch.id =:msiSearchId ";
                TypedQuery<Long> msQueryQ = entityManagerMSI.createQuery(query, Long.class);
                msQueryQ.setParameter("msiSearchId", msiSearchId);
                m_msQueriesIds = msQueryQ.getResultList();
                m_msqQueryMap = new HashMap();
                int i=0;
                for (Long msqId : m_msQueriesIds) {
                    MsQuery msQuery = new MsQuery();
                    msQuery.setId(msqId);
                    m_listMsQueries.add(msQuery);
                    m_msqQueryMap.put(msqId, i);
                    m_nbPeptideMatchesByMsQueryIdMap.put(msqId, 0);
                    i++;
                }
                
                m_msiSearch = entityManagerMSI.find(MsiSearch.class, msiSearchId);
                
                // slice the task and get the first one
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, m_listMsQueries.size(), SLICE_SIZE);

                // execute the first slice now
                fetchMSQueriesData(subTask, entityManagerMSI);
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }
    
    /**
     * subTask to load msQueries
     *
     * @return
     */
    private boolean fetchMSQueriesDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            switch (slice.getSubTaskId()) {
                case SUB_TASK_MSQUERY:
                    fetchMSQueriesData(slice, entityManagerMSI);
                    break;
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
    
    private boolean fetchMSQueriesData(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfMsQueriesIds = subTask.getSubList(m_msQueriesIds);
        return fetchMSQueriesData(entityManagerMSI, sliceOfMsQueriesIds);
    }
    
    
    private boolean fetchMSQueriesData(EntityManager entityManagerMSI, List<Long> listMSQueriesId) {
        String queryMSQ = "SELECT msq "
                + "FROM fr.proline.core.orm.msi.MsQuery msq "
                + "WHERE msq.id IN (:listId) ";
        TypedQuery<MsQuery> queryMSQueries = entityManagerMSI.createQuery(queryMSQ, MsQuery.class);
        queryMSQueries.setParameter("listId", listMSQueriesId);
        
        List<MsQuery> resultList = new ArrayList();
        if (listMSQueriesId != null && !listMSQueriesId.isEmpty()) {
            resultList = queryMSQueries.getResultList();
        }
        
        String querySp = "SELECT msq.id, sp "
                + "FROM fr.proline.core.orm.msi.Spectrum sp, fr.proline.core.orm.msi.MsQuery msq "
                + "WHERE sp.id = msq.spectrum.id AND "
                + "msq.id IN (:listId) ";
        Query querySpectrum = entityManagerMSI.createQuery(querySp);
        querySpectrum.setParameter("listId", listMSQueriesId);
        Map<Long, Spectrum> spectrumMap = new HashMap<>();
        if (listMSQueriesId != null && !listMSQueriesId.isEmpty()) {
            List<Object[]> rsSp = querySpectrum.getResultList();
            for (Object[] o: rsSp){
                spectrumMap.put((Long)o[0], (Spectrum)o[1]);
            }
        }
        
        String queryCountPMS = "SELECT pm.msQuery.id, count(pm.id) "
                + "FROM fr.proline.core.orm.msi.PeptideMatch pm "
                + "WHERE pm.msQuery.id IN (:listId) "
                + "GROUP BY pm.msQuery.id";
        Query queryCountPM = entityManagerMSI.createQuery(queryCountPMS);
        queryCountPM.setParameter("listId", listMSQueriesId);
        if (listMSQueriesId != null && !listMSQueriesId.isEmpty()) {
            List<Object[]> rsCount = queryCountPM.getResultList();
            for (Object[] o: rsCount){
                Long id = (Long)o[0];
                Long n = (Long)o[1];
                Integer nb = n.intValue();
                m_nbPeptideMatchesByMsQueryIdMap.put(id, nb);
            }
        }
        
        for (MsQuery msq : resultList) {
            // update the list
            int index = m_msqQueryMap.get(msq.getId());
            msq.setSpectrum(spectrumMap.get(msq.getId()));
            msq.setMsiSearch(m_msiSearch);
            m_listMsQueries.set(index, msq);
        }
        
        return true;
    }
    
}
