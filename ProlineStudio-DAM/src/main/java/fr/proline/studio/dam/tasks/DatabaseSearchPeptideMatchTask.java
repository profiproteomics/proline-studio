package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
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
  
    private ResultSet rset = null;
    private String        searchString = null;
    private ArrayList<Integer>     searchResult = null;
    
    public DatabaseSearchPeptideMatchTask(AbstractDatabaseCallback callback, ResultSet rset, String searchString, ArrayList<Integer> searchResult) {
        super(callback, Priority.HIGH_1);
        this.rset = rset;       
        this.searchString = searchString;
        this.searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search the first ProteinSet which has a Best Protein Match with the searched name
            TypedQuery<Integer> searchQuery = entityManagerMSI.createQuery("SELECT pm.id FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id AND p.sequence LIKE :search ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", Integer.class);
            searchQuery.setParameter("search", "%"+searchString+"%");
            searchQuery.setParameter("rsetId", rset.getId());
            List<Integer> proteinSetIdList = searchQuery.getResultList();

            searchResult.clear();
            searchResult.addAll(proteinSetIdList);

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
