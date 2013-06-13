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
 * Used to Search a ProteinMatch of a Rset
 * 
 * @author JM235353
 */
public class DatabaseSearchPeptideMatchTask extends AbstractDatabaseTask {
  
    private long projectId = -1;
    private ResultSet rset = null;
    private String        searchString = null;
    private ArrayList<Long>     searchResult = null;
    
    public DatabaseSearchPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset, String searchString, ArrayList<Long> searchResult) {
        super(callback, Priority.HIGH_1, new TaskInfo("Search Peptide Match "+searchString, TASK_LIST_INFO));
        this.projectId = projectId;
        this.rset = rset;       
        this.searchString = searchString;
        this.searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search peptideMatches with the searched name
            TypedQuery<Long> searchQuery = entityManagerMSI.createQuery("SELECT pm.id FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id AND p.sequence LIKE :search ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", Long.class);
            searchQuery.setParameter("search", "%"+searchString+"%");
            searchQuery.setParameter("rsetId", rset.getId());
            List<Long> peptideMatchIdList = searchQuery.getResultList();

            searchResult.clear();
            searchResult.addAll(peptideMatchIdList);

            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        return true;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }
    
}
