
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.dto.DDataset;
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
    private List<Long> m_runIds = null;
    private String[] m_resultPath = null;
    private String m_searchString = null;
    private ArrayList<RawFile> m_rawfileFounds = null;
        
    private int m_action;
    
    private final static int LOAD_RUN_FOR_RSM = 0;
    private final static int LOAD_PEAKLIST_PATH = 1;
    private final static int SEARCH_RAWFILE = 2;
     
    public DatabaseRunsTask(AbstractDatabaseCallback callback){
        super(callback, null);
    }
    
    /**
     * Load Run Id for specified RSMs
     * @return 
     */
    public void initLoadRunIdForRsm(long projectId, Long rsmId, ArrayList<Long> runIds){
        setTaskInfo(new TaskInfo(" Load RunId for Identification Summary with id "+rsmId,false, TASK_LIST_INFO));
        m_projectId = projectId;
        m_rsmId = rsmId;
        m_runIds = runIds;
        m_action = LOAD_RUN_FOR_RSM;
    }
    
    /**
     * Search Raw Files
     *
     * @return
     */
    public void initSearchRawFile(String searchString, ArrayList<RawFile> rawfileFounds) {
        setTaskInfo(new TaskInfo(" Search Raw File " + searchString, false, TASK_LIST_INFO, true /* hide this task to user */));
        m_searchString = searchString;
        m_rawfileFounds = rawfileFounds;
        m_action = SEARCH_RAWFILE;
    }
    
        /**
     * Load PeakList Path for Rset
     *
     * @return
     */
    public void initLoadPeakListPathForRset(long projectId, Long rsetId, String[] resultPath) {
        setTaskInfo(new TaskInfo(" Load PeakList Path for Search Result with id " + rsetId, false, TASK_LIST_INFO, true /* hide this task to user */));
        m_projectId = projectId;
        m_rsetId = rsetId;
        m_resultPath = resultPath;
        m_action = LOAD_PEAKLIST_PATH;
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

            if(idfDs != null && idfDs.size()>0){
                m_runIds.add(idfDs.get(0).getRun().getId());
            }
            
            
            entityManagerUDS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerUDS.getTransaction().rollback();
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
            entityManagerMSI.getTransaction().rollback();
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

            m_rawfileFounds.clear();
            m_rawfileFounds.addAll(rawFileList);

            entityManagerUDS.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case LOAD_RUN_FOR_RSM:
            case LOAD_PEAKLIST_PATH:
            case SEARCH_RAWFILE:
                return true;
        }
        
        return true; // should never be called
    }
    
    
    
}
