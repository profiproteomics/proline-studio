package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.core.orm.uds.SpectrumTitleParsingRule;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseAdminTask extends AbstractDatabaseTask {

    private long m_peaklistSoftwareId = -1;
    private String m_name;
    private String m_version;
    private SpectrumTitleParsingRule m_spectrumTitleParsingRule = null;

    private int m_action;
    private final static int ADD_PEAKLIST_SOFTWARE = 0;
    private final static int MODIFY_PEAKLIST_SOFTWARE = 1;

    public DatabaseAdminTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }

    /**
     * Load PeakList Path for Rset
     *
     * @param projectId
     * @param rsetId
     * @param resultPath
     */
    public void initAddPeakListSoftware(String name, String version, SpectrumTitleParsingRule spectrumTitleParsingRule) {
        setTaskInfo(new TaskInfo(" Add PeakList Software " + name, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_name = name;
        m_version = version;
        m_spectrumTitleParsingRule = spectrumTitleParsingRule;
        m_action = ADD_PEAKLIST_SOFTWARE;
    }

    public void initModifyPeakListSoftware(long id, String name, String version) {
        setTaskInfo(new TaskInfo(" Modify PeakList Software " + name, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_peaklistSoftwareId = id;
        m_name = name;
        m_version = version;
        m_action = MODIFY_PEAKLIST_SOFTWARE;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case ADD_PEAKLIST_SOFTWARE:
                return addPeaklistSoftware();
            case MODIFY_PEAKLIST_SOFTWARE:
                return modifyPeaklistSoftware();
        }
        return false;
    }

    public boolean addPeaklistSoftware() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

        try {

            entityManagerUDS.getTransaction().begin();
            
            entityManagerUDS.persist(m_spectrumTitleParsingRule);
            
            PeaklistSoftware peaklistSoftware = new PeaklistSoftware();
            peaklistSoftware.setName(m_name);
            peaklistSoftware.setVersion(m_version);
            peaklistSoftware.setSpecTitleParsingRule(m_spectrumTitleParsingRule);
            
            entityManagerUDS.persist(peaklistSoftware);
            
            reloadPeakListSoftwares(entityManagerUDS);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {

            entityManagerUDS.close();

        }

        return true;
    }

    public boolean modifyPeaklistSoftware() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

        try {

            entityManagerUDS.getTransaction().begin();

            PeaklistSoftware peaklistSoftware = entityManagerUDS.find(PeaklistSoftware.class, m_peaklistSoftwareId);

            peaklistSoftware.setName(m_name);
            peaklistSoftware.setVersion(m_version);
            
            reloadPeakListSoftwares(entityManagerUDS);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {

            entityManagerUDS.close();

        }

        return true;
    }
    
    private void reloadPeakListSoftwares(EntityManager entityManagerUDS) {
        // Load All peaklist softwares

        TypedQuery<PeaklistSoftware> peaklistSoftwareQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.PeaklistSoftware p ORDER BY p.name ASC", PeaklistSoftware.class);
        List<PeaklistSoftware> peaklistSoftwareList = peaklistSoftwareQuery.getResultList();
        DatabaseDataManager.getDatabaseDataManager().setPeaklistSofwares(peaklistSoftwareList);

    }

}
