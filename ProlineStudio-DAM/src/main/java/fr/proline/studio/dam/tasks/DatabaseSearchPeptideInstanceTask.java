package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseSearchPeptideInstanceTask extends AbstractDatabaseTask {
   
    private Integer projectId = null;
    private ResultSummary rsm = null;
    private String        searchString = null;
    private ArrayList<Integer>     searchResult = null;
    
    public DatabaseSearchPeptideInstanceTask(AbstractDatabaseCallback callback, Integer projectId, ResultSummary rsm, String searchString, ArrayList<Integer> searchResult) {
        super(callback, Priority.HIGH_1, new TaskInfo("Search", "Search Peptide Instance "+searchString, TASK_LIST_INFO));
        this.projectId = projectId;
        this.rsm = rsm;       
        this.searchString = searchString;
        this.searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search peptideMatches with the searched name
            TypedQuery<Integer> searchQuery = entityManagerMSI.createQuery("SELECT pi.id FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pi.resultSummary.id=:rsmId AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id AND p.sequence LIKE :search ORDER BY pm.score DESC", Integer.class);
            searchQuery.setParameter("search", "%"+searchString+"%");
            searchQuery.setParameter("rsmId", rsm.getId());
            List<Integer> peptideInstanceIdList = searchQuery.getResultList();

            searchResult.clear();
            searchResult.addAll(peptideInstanceIdList);

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
