package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Spectrum from a MsQuery
 *
 * @author AK
 */
public class DatabaseVerifySpectrumFromResultSets extends AbstractDatabaseTask {

    private final List<Long> m_resultSetIDs;
    private final List<Long> m_failedResultSetIDs;
    private final Map<Long,List<Long>> m_emptySpectraPerResultSetIDs;
    private long m_projectID = -1;


    public DatabaseVerifySpectrumFromResultSets(AbstractDatabaseCallback callback, List<Long>  resultSetIDs, long projectID, List<Long> failedRSIDs, Map<Long,List<Long>> emptySpectraPerRSIds ) {
        super(callback, new TaskInfo("Verify Spectrum for ResultSet IDs " + resultSetIDs, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_resultSetIDs = resultSetIDs;
        m_projectID = projectID;
        m_failedResultSetIDs = failedRSIDs;
        m_emptySpectraPerResultSetIDs = emptySpectraPerRSIds;
    }
           
    @Override
    public boolean needToFetch() {
        return true;
    }

    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectID).createEntityManager();
        boolean foundErrSpectra = false;
        try {
            
            entityManagerMSI.getTransaction().begin();
            
            TypedQuery<Object[]> spectrumQuery = entityManagerMSI.createQuery("SELECT spc.id, rset.id FROM fr.proline.core.orm.msi.ResultSet AS rset, fr.proline.core.orm.msi.MsiSearch AS msi, fr.proline.core.orm.msi.Peaklist AS pl, Spectrum AS spc"
                + " WHERE rset.msiSearch = msi AND rset.id IN :rsIds AND msi.peaklist = pl AND spc.peaklistId = pl.id AND (spc.firstTime  is null OR spc.firstTime = 0) "
                + " AND (spc.firstCycle  is null OR spc.firstCycle = 0) AND (spc.firstScan  is null OR spc.firstScan = 0) ", Object[].class);
            spectrumQuery.setParameter("rsIds", m_resultSetIDs);
                
            List<Object[]> errSpectrumIDs = spectrumQuery.getResultList();
            if(errSpectrumIDs != null && !errSpectrumIDs.isEmpty()){
                foundErrSpectra = true;
                for(Object[] nextErrSp : errSpectrumIDs){
                    Long spectrumID = (Long)nextErrSp[0];
                    Long rsId = (Long)nextErrSp[1];
                    List<Long> spectraIds = m_emptySpectraPerResultSetIDs.getOrDefault(rsId, new ArrayList<>());                                       
                    spectraIds.add(spectrumID);
                    m_emptySpectraPerResultSetIDs.put(rsId, spectraIds);
                    if(!m_failedResultSetIDs.contains(rsId))
                        m_failedResultSetIDs.add(rsId);
                }
            }
            
            entityManagerMSI.getTransaction().commit();

        } catch (Exception e) {
            foundErrSpectra = true;            
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            if(m_failedResultSetIDs.isEmpty())
                m_failedResultSetIDs.addAll(m_resultSetIDs);
            return !foundErrSpectra;
        } finally {

            entityManagerMSI.close();

        }

        return !foundErrSpectra;
    }

}
