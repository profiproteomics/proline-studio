package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.Peaklist;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Spectrum from a MsQuery
 *
 * @author JM235353
 */
public class DatabaseLoadSinglePeaklist extends AbstractDatabaseTask {

    private long m_rsetID = -1;
    private long m_projectID = -1;
    private Peaklist[] m_peaklist = null;

    public DatabaseLoadSinglePeaklist(AbstractDatabaseCallback callback, long rsetID, long projectID, Peaklist[] peaklist) {
        super(callback, new TaskInfo("Load Peaklist for rset ID " + rsetID, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_rsetID = rsetID;
        m_projectID = projectID;
        m_peaklist = peaklist;
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

            TypedQuery<Peaklist> peaklistQuery = entityManagerMSI.createQuery("SELECT plist FROM Peaklist plist, MsiSearch msisearch, ResultSet rset WHERE rset.id = :rsetId AND rset.msiSearch=msisearch AND msisearch.peaklist=plist", Peaklist.class);

            peaklistQuery.setMaxResults(1);
            peaklistQuery.setParameter("rsetId", m_rsetID);

            List<Peaklist> peaklistList = peaklistQuery.getResultList();

            if (!peaklistList.isEmpty()) {
                m_peaklist[0] = peaklistList.get(0);
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

}
