package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.Peaklist;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Tasks to get and set MSI Peaklist associated data.
 *
 * @author JM235353
 */
public class DatabasePeaklistTask extends AbstractDatabaseTask {

    private long m_rsetID = -1;
    private long m_projectID = -1;
    private Peaklist[] m_peaklist = null;
    private String m_rawFileIdentifier = null;

    private int m_action;
    private final static int LOAD_PEAKLIST_FOR_RS = 0;
    private final static int UPDATE_PEAKLIST = 1;

    public DatabasePeaklistTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }

    public void initUpdatePeaklistIdentifier(long projectId, Long rsetId, String peaklistIdentifier) {
        setTaskInfo(new TaskInfo(" Updating Raw File Identifier ", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW, true /* hide this task to user */));
        m_projectID = projectId;
        m_rsetID = rsetId;
        m_rawFileIdentifier = peaklistIdentifier;
        m_action = UPDATE_PEAKLIST;
    }

    public void initLoadPeaklistForRS(long rsetID, long projectID, Peaklist[] peaklist) {
        setTaskInfo(new TaskInfo(" Load Peaklist for rset ID " + rsetID, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW, true /* hide this task to user */));
        m_rsetID = rsetID;
        m_projectID = projectID;
        m_peaklist = peaklist;
        m_action = LOAD_PEAKLIST_FOR_RS;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_PEAKLIST_FOR_RS:
                return loadPeaklistForRS();
            case UPDATE_PEAKLIST:
                return updatePeaklistRawFileIdentifier();
        }
        return false;
    }

    public boolean updatePeaklistRawFileIdentifier() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectID).createEntityManager();       
        try {

            entityManagerMSI.getTransaction().begin();

            Query rawFileUpdateQuery = entityManagerMSI.createQuery("UPDATE Peaklist as plist set plist.rawFileId = :identifier WHERE plist.id  IN (SELECT plist FROM Peaklist plist, MsiSearch search, ResultSet rset WHERE rset.id = :rsetId AND rset.msiSearch=search AND search.peaklist=plist)");
             
            rawFileUpdateQuery.setParameter("rsetId", m_rsetID);
            rawFileUpdateQuery.setParameter("identifier", m_rawFileIdentifier);

            rawFileUpdateQuery.executeUpdate();
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

    public boolean loadPeaklistForRS() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectID).createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();
            // SELECT plist FROM Peaklist plist, MsiSearch msisearch, ResultSet rset 
            // WHERE rset.id = :rsetId AND rset.msiSearch=msisearch AND msisearch.peaklist=plist
            TypedQuery<Peaklist> peaklistQuery =entityManagerMSI.createQuery("SELECT plist FROM Peaklist plist, MsiSearch search, ResultSet rset WHERE rset.id = :rsetId AND rset.msiSearch=search AND search.peaklist=plist", Peaklist.class);

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
