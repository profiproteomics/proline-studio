package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.Enzyme;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.SearchSetting;
import fr.proline.core.orm.msi.SearchSettingsSeqDatabaseMap;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Properties for a Dataset
 * @author JM235353
 */
public class DatabaseRsetProperties extends AbstractDatabaseTask {
    
    private long m_projectId;
    private Dataset m_dataset = null;
    private ResultSet m_rset = null;
    
    public DatabaseRsetProperties(AbstractDatabaseCallback callback, long projectId, Dataset dataset) {
        super(callback, new TaskInfo("Load Properties for Search Result "+dataset.getName(), TASK_LIST_INFO));
        m_projectId = projectId;
        m_dataset = dataset;
    }
    
    public DatabaseRsetProperties(AbstractDatabaseCallback callback, long projectId, ResultSet rset, String name) {
        super(callback, new TaskInfo("Load Properties for Search Result "+name, TASK_LIST_INFO));
        m_projectId = projectId;
        m_rset = rset;
    }
    

    @Override
    public void abortTask() {
        // nothing to do for task which are not inherited from AbstractDatabaseSlicerTask 
    }
    
    @Override
    public boolean needToFetch() {
        if (m_rset == null) {
            m_rset = m_dataset.getTransientData().getResultSet();
        }
        return (m_rset.getTransientData().getPeptideMatchesCount() == null);
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();


            fetchData_count(entityManagerMSI, m_rset);
            
            ResultSet rsetDecoy = m_rset.getDecoyResultSet();
            if (rsetDecoy != null) {
                fetchData_count(entityManagerMSI, rsetDecoy);
            }
            
            // Load Enzymes
            SearchSetting searchSetting = m_rset.getMsiSearch().getSearchSetting();
            
            SearchSetting mergedSearchSetting = entityManagerMSI.merge(searchSetting);
            Set<Enzyme> enzymeSet = mergedSearchSetting.getEnzymes();
            Iterator<Enzyme> it = enzymeSet.iterator();
            while (it.hasNext()) { // to avoid lazy fetching problem
                it.next();
            }
            searchSetting.setEnzymes(enzymeSet);
            
            // Load SeqDatabase
            Set<SearchSettingsSeqDatabaseMap> setDatabaseMapSet = mergedSearchSetting.getSearchSettingsSeqDatabaseMaps();
            Iterator<SearchSettingsSeqDatabaseMap> itSeqDbMap = setDatabaseMapSet.iterator();
            while (itSeqDbMap.hasNext()) { // to avoid lazy fetching problem
                itSeqDbMap.next();
            }
            searchSetting.setSearchSettingsSeqDatabaseMaps(setDatabaseMapSet);
            
            
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        return true;
    }

    private void fetchData_count(EntityManager entityManagerMSI, ResultSet rset) throws Exception {
        
        Long rsetId = rset.getId();
        
        // Count Protein Match
        TypedQuery<Long> countProteinMatchQuery = entityManagerMSI.createQuery("SELECT count(pm) FROM fr.proline.core.orm.msi.ProteinMatch pm WHERE pm.resultSet.id=:rsetId", Long.class);
        countProteinMatchQuery.setParameter("rsetId", rsetId);
        Long proteinMatchNumber = countProteinMatchQuery.getSingleResult();

        rset.getTransientData().setProteinMatchesCount(Integer.valueOf(proteinMatchNumber.intValue()));


        // Count Peptide Match
        TypedQuery<Long> countPeptideMatchQuery = entityManagerMSI.createQuery("SELECT count(pm) FROM fr.proline.core.orm.msi.PeptideMatch pm WHERE pm.resultSet.id=:rsetId", Long.class);
        countPeptideMatchQuery.setParameter("rsetId", rsetId);
        Long peptideMatchNumber = countPeptideMatchQuery.getSingleResult();

        rset.getTransientData().setPeptideMatchesCount(Integer.valueOf(peptideMatchNumber.intValue()));

        // Count Ms Queries
        /*TypedQuery<Long> countMsQueriesQuery = entityManagerMSI.createQuery("SELECT count(DISTINCT msq) FROM fr.proline.core.orm.msi.MsQuery msq, fr.proline.core.orm.msi.PeptideMatch pm WHERE pm.resultSet.id=:rsetId AND msq=pm.msQuery", Long.class);
        */
        /*TypedQuery<Long> countMsQueriesQuery = entityManagerMSI.createQuery("SELECT count(msq) FROM fr.proline.core.orm.msi.MsQuery msq, fr.proline.core.orm.msi.ResultSet rset, fr.proline.core.orm.msi.MsiSearch msi_search WHERE rset.id=:rsetId AND rset.msiSearch=msi_search AND msq.msiSearch=msi_search", Long.class);
        
        countMsQueriesQuery.setParameter("rsetId", rsetId);
        Long msQueriesNumber = countMsQueriesQuery.getSingleResult();

        rset.getTransientData().setMSQueriesCount(Integer.valueOf(msQueriesNumber.intValue()));*/

    }
}
