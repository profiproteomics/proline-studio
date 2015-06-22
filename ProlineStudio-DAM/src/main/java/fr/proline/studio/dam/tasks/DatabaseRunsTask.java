
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * This class provides methods to load and to create Runs and Raw file from the UDS db 
 *
 * @author vd225637
 */
public class DatabaseRunsTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private Long m_rsmId = null;
    private Long m_rsetId = null;
    private Long m_datasetId = null;
    private RawFile m_rawfile = null;
    private Run m_run = null;
    
    private List<Long> m_runIds = null;
    private String[] m_resultPath = null;
    private String m_searchString = null;
    private ArrayList<RawFile> m_rawfileFounds = null;
    private Run[] m_runOut;    
    
    private int m_action;
    
    private final static int LOAD_RUN_FOR_RSM = 0;
    private final static int LOAD_PEAKLIST_PATH = 1;
    private final static int SEARCH_RAWFILE = 2;
    private final static int LOAD_RAWFILE = 3;
    private final static int REGISTER_IDENTIFICATION_DATASET_RUN = 4;
    
     
    public DatabaseRunsTask(AbstractDatabaseCallback callback){
        super(callback, null);
    }
    
    /**
     * Load Run Id for specified RSMs 
     * @param projectId
     * @param rsmId
     * @param runIds
     */
    public void initLoadRunIdForRsm(long projectId, Long rsmId, ArrayList<Long> runIds){
        setTaskInfo(new TaskInfo(" Load RunId for Identification Summary with id "+rsmId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_rsmId = rsmId;
        m_runIds = runIds;
        m_action = LOAD_RUN_FOR_RSM;
    }
    
    /**
     * Search Raw Files
     *
     * @param searchString
     * @param rawfileFounds
     */
    public void initSearchRawFile(String searchString, ArrayList<RawFile> rawfileFounds) {
        setTaskInfo(new TaskInfo(" Search Raw File " + searchString, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW, true /* hide this task to user */));
        m_searchString = searchString;
        m_rawfileFounds = rawfileFounds;
        m_action = SEARCH_RAWFILE;
    }
    
    public void initLoadRawFile(Long identificationDatasetId, ArrayList<RawFile> rawfileFounds, Run[] runOut) {
        setTaskInfo(new TaskInfo(" Load Raw File for Dataset " + identificationDatasetId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW, true /* hide this task to user */));
        m_datasetId = identificationDatasetId;
        m_rawfileFounds = rawfileFounds;
        m_runOut = runOut;
        m_action = LOAD_RAWFILE;
    }
    
    /**
     * Load PeakList Path for Rset
     *
     * @param projectId
     * @param rsetId
     * @param resultPath
     */
    public void initLoadPeakListPathForRset(long projectId, Long rsetId, String[] resultPath) {
        setTaskInfo(new TaskInfo(" Load PeakList Path for Search Result with id " + rsetId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW, true /* hide this task to user */));
        m_projectId = projectId;
        m_rsetId = rsetId;
        m_resultPath = resultPath;
        m_action = LOAD_PEAKLIST_PATH;
    }
    
     /**
     * Register map between IdentificationDataset & Run
     *
     * @param datasetId
     * @param rawfile
     * @param run
     */
    public void initRegisterIdentificationDatasetRun(long datasetId, RawFile rawfile, Run run) {
        setTaskInfo(new TaskInfo(" Register Run for Dataset with id "+datasetId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
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
            case LOAD_RUN_FOR_RSM: 
                return fetchRunForRsm();
            case LOAD_PEAKLIST_PATH:
                return fetchPeaklistPath();
            case SEARCH_RAWFILE:
                return searchRawFile();
            case LOAD_RAWFILE:
                return loadRawFile();
            case REGISTER_IDENTIFICATION_DATASET_RUN:
                return registerIdentificationDatasetRun();
        }
        
        return false; 
    }
    
    public boolean fetchRunForRsm() {
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            //Get Run and Raw File
            TypedQuery<IdentificationDataset> runIdQuery = entityManagerUDS.createQuery("SELECT idfDS FROM IdentificationDataset idfDS WHERE idfDS.project.id = :pjId and idfDS.resultSummaryId =:rsmId  ", IdentificationDataset.class);
            runIdQuery.setParameter("pjId", m_projectId);            
            runIdQuery.setParameter("rsmId", m_rsmId);
            List<IdentificationDataset> idfDs = runIdQuery.getResultList();

            if(idfDs != null && idfDs.size()>0 && idfDs.get(0).getRun() != null){
                m_runIds.add(idfDs.get(0).getRun().getId());
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

    
    public boolean fetchPeaklistPath() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();
            
            TypedQuery<String> peakListPathQuery = entityManagerMSI.createQuery("SELECT plist.path FROM Peaklist plist, MsiSearch msisearch, ResultSet rset WHERE rset.id = :rsetId AND rset.msiSearch=msisearch AND msisearch.peaklist=plist ", String.class);        
            peakListPathQuery.setParameter("rsetId", m_rsetId);
            List<String> paths = peakListPathQuery.getResultList();

            if (paths != null && paths.size()>0) {
                
                
                String path = paths.get(0);
                
                // remove .raw , or .raw-1.mgf
                int indexRaw = path.toLowerCase().indexOf(".raw");
                if (indexRaw != -1) {
                    path = path.substring(0,indexRaw);
                }
                // remove .mgf, ...
                int indexMgf = path.toLowerCase().indexOf(".mgf");
                if (indexMgf != -1) {
                    path = path.substring(0, indexMgf);
                }
                
                // remove all code before \ / or ~
                int index = path.lastIndexOf('/');
                index = Math.max(index, path.lastIndexOf('\\'));
                index = Math.max(index, path.lastIndexOf('~'));
                 if (index != -1) {
                    path = path.substring(index+1);
                }
                
                m_resultPath[0] = path;
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
    
    public boolean searchRawFile() {
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            
            // Search peptideMatches with the searched name
            TypedQuery<RawFile> searchQuery = entityManagerUDS.createQuery("SELECT rawfile FROM RawFile rawfile WHERE rawfile.rawFileName LIKE :search ORDER BY rawfile.rawFileName ASC", RawFile.class);
            
            String searchStringSql = m_searchString.replaceAll("\\*", "%").replaceAll("\\?","_");
            searchQuery.setParameter("search", searchStringSql);
            List<RawFile> rawFileList = searchQuery.getResultList();
            // force loading of runs
            for (RawFile r : rawFileList) {
                r.getRuns().size();
            }
            m_rawfileFounds.clear();
            m_rawfileFounds.addAll(rawFileList);

            entityManagerUDS.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            IdentificationDataset identificationDataset = entityManagerUDS.find(IdentificationDataset.class, m_datasetId);
            if (identificationDataset != null) {
                RawFile rawFile = identificationDataset.getRawFile();
                if (rawFile != null) {
                    m_rawfileFounds.add(rawFile);
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
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
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
