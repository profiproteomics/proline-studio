package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseRsetTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private ArrayList<ResultSet> m_resultSetArrayList = null;
    
    public DatabaseRsetTask(AbstractDatabaseCallback callback, long projectId, ArrayList<ResultSet> resultSetArrayList) {
        super(callback, new TaskInfo("Load All Search Results", TASK_LIST_INFO));
        m_projectId = projectId;
        m_resultSetArrayList = resultSetArrayList;
    }
    

    @Override
    public boolean needToFetch() {
        return true; // do not keep list of result set loaded : so we must re-read each time
    }
 
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();
            
            TypedQuery<ResultSet> resultSetQuery = entityManagerMSI.createQuery("SELECT rset FROM fr.proline.core.orm.msi.ResultSet rset WHERE rset.type=:decoyType ORDER BY rset.id", ResultSet.class);
            resultSetQuery.setParameter("decoyType", ResultSet.Type.SEARCH);
            List<ResultSet> resultSetList = resultSetQuery.getResultList();
            
            m_resultSetArrayList.addAll(resultSetList);
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        return true;
    }
    
}
