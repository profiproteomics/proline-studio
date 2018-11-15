package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * This class provides methods to load and to create Runs and Raw file from the
 * UDS db
 *
 * @author vd225637
 */
public class DatabaseRunsTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private List<Long> m_rsmIds = null;
    private Long m_datasetId = null;
    private RawFile m_rawfile = null;
    private Run m_run = null;

    
    private Map<Long,Long> m_runIdsByRsmIds = null;
    private String m_searchString = null;
    private HashMap<String, RawFile> m_rawfileFounds = null;
    private Run[] m_runOut;


    private int m_action;

    private final static int LOAD_RUNS_FOR_RSMS = 0;
    private final static int SEARCH_RAWFILE = 1;
    private final static int LOAD_RAWFILE = 2;
    private final static int REGISTER_IDENTIFICATION_DATASET_RUN = 3;

   
    public DatabaseRunsTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }

    /**
     * Load Run Id for specified RSMs
     *
     * @param projectId
     * @param rsmIds
     * @param runIds
     */
    public void initLoadRunIdsForRsms(long projectId, ArrayList<Long> rsmIds, HashMap<Long,Long> runIds) {
        setTaskInfo(new TaskInfo(" Load RunId for Identification Summary with ids " + rsmIds, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_rsmIds = rsmIds;
        m_runIdsByRsmIds = runIds;
        m_action = LOAD_RUNS_FOR_RSMS;
    }

    /**
     * Search Raw Files
     *
     * @param searchString
     * @param rawfileFounds
     */
    public void initSearchRawFile(String searchString, HashMap<String, RawFile> rawfileFounds) {
        setTaskInfo(new TaskInfo(" Search Raw File " + searchString, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW, true /* hide this task to user */));
        m_searchString = searchString;
        m_rawfileFounds = rawfileFounds;
        m_action = SEARCH_RAWFILE;
    }

    public void initLoadRawFile(Long identificationDatasetId, HashMap<String, RawFile> rawfileFounds, Run[] runOut) {
        setTaskInfo(new TaskInfo(" Load Raw File for Dataset " + identificationDatasetId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW, true /* hide this task to user */));
        m_datasetId = identificationDatasetId;
        m_rawfileFounds = rawfileFounds;
        m_runOut = runOut;
        m_action = LOAD_RAWFILE;
    }

    /**
     * Register map between IdentificationDataset & Run
     *
     * @param datasetId
     * @param rawfile
     * @param run
     */
    public void initRegisterIdentificationDatasetRun(long datasetId, RawFile rawfile, Run run) {
        setTaskInfo(new TaskInfo(" Register Run for Dataset with id " + datasetId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_datasetId = datasetId;
        if (rawfile == null) {
            rawfile = run.getRawFile();
        }
        m_rawfile = rawfile;
        m_run = run;

        m_action = REGISTER_IDENTIFICATION_DATASET_RUN;
    }

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_RUNS_FOR_RSMS:
                return fetchRunsForRsms();
            case SEARCH_RAWFILE:
                return searchRawFile();
            case LOAD_RAWFILE:
                return loadRawFile();
            case REGISTER_IDENTIFICATION_DATASET_RUN:
                return registerIdentificationDatasetRun();
        }

        return false;
    }

    public boolean fetchRunsForRsms() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            //Get Run and Raw File
            TypedQuery<IdentificationDataset> runIdQuery = entityManagerUDS.createQuery("SELECT idfDS FROM IdentificationDataset idfDS WHERE idfDS.project.id = :pjId and idfDS.resultSummaryId IN (:rsmIds)  ", IdentificationDataset.class);
            runIdQuery.setParameter("pjId", m_projectId);
            runIdQuery.setParameter("rsmIds", m_rsmIds);
            List<IdentificationDataset> idfDs = runIdQuery.getResultList();

            if (idfDs != null) {
                for(IdentificationDataset identDS : idfDs) {
                    if(identDS.getRun() != null)
                        m_runIdsByRsmIds.put(identDS.getResultSummaryId(), identDS.getRun().getId());
                    else
                        m_runIdsByRsmIds.put(identDS.getResultSummaryId(), -1L);
                }
            }

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

    public boolean searchRawFile() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            // Search rawFile with the searched identifier
            TypedQuery<RawFile> searchQuery = entityManagerUDS.createQuery("SELECT searchRawfile  FROM RawFile searchRawfile WHERE searchRawfile.identifier LIKE :search ORDER BY searchRawfile.identifier ASC", RawFile.class);

            String searchStringSql = m_searchString.replaceAll("\\*", "%").replaceAll("\\?", "_");
            searchQuery.setParameter("search", searchStringSql);

            m_rawfileFounds.clear();

            List<RawFile> rawFileList = searchQuery.getResultList();

            // force loading of runs
            for (RawFile r : rawFileList) {
                r.getRuns().size();
                m_rawfileFounds.put(r.getRawFileName(), r);
            }

            entityManagerUDS.getTransaction().commit();
        } catch (RuntimeException e) {
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

    private boolean loadRawFile() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            IdentificationDataset identificationDataset = entityManagerUDS.find(IdentificationDataset.class, m_datasetId);
            if (identificationDataset != null) {
                RawFile rawFile = identificationDataset.getRawFile();
                if (rawFile != null) {
                    m_rawfileFounds.put(rawFile.getRawFileName(), rawFile);
                    m_runOut[0] = identificationDataset.getRun();
                }
            }
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
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

    public boolean registerIdentificationDatasetRun() {
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            IdentificationDataset idf = entityManagerUDS.find(IdentificationDataset.class, m_datasetId);
            RawFile mergedRaw = entityManagerUDS.find(RawFile.class, m_rawfile.getIdentifier());
            Run mergedRun = entityManagerUDS.find(Run.class, m_run.getId());
            idf.setRawFile(mergedRaw);
            idf.setRun(mergedRun);
            entityManagerUDS.merge(idf);
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

    @Override
    public boolean needToFetch() {
        return true; // should never be called
    }

}
