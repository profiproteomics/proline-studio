package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Spectrum from a MsQuery
 *
 * @author AK
 */
public class DatabaseVerifySpectrumFromResultSet extends AbstractDatabaseTask {

    private long m_resultSetID = -1;
    private long m_projectID = -1;
    private long m_spectrumID = -1;

    public DatabaseVerifySpectrumFromResultSet(AbstractDatabaseCallback callback, long resultSetID, long projectID) {
        super(callback, new TaskInfo("Verify Spectrum for ResultSet ID " + resultSetID, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_resultSetID = resultSetID;
        m_projectID = projectID;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectID).createEntityManager();

        try {
            
            entityManagerMSI.getTransaction().begin();

            TypedQuery<Long> spectrumQuery = entityManagerMSI.createQuery("SELECT spc.id FROM fr.proline.core.orm.msi.ResultSet AS rset, fr.proline.core.orm.msi.MsiSearch AS msi, fr.proline.core.orm.msi.Peaklist AS pl, Spectrum AS spc WHERE rset.msiSearch = msi AND rset.id = "+m_resultSetID+" AND msi.peaklist = pl AND spc.peaklistId = pl.id AND (spc.firstTime > 0 OR spc.firstCycle > 0 OR spc.firstScan > 0)", Long.class);
            spectrumQuery.setMaxResults(1);
            
            List<Long> spectrumIDs = spectrumQuery.getResultList();

            if (!spectrumIDs.isEmpty()) {
                m_spectrumID = spectrumIDs.get(0);
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

    public long getSpectrumID() {
        return m_spectrumID;
    }
}
