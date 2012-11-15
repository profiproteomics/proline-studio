package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.IdentificationFraction;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.*;
import fr.proline.studio.dam.data.ResultSetData;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load a Result Set of an Identification Fraction
 *
 * @author JM235353
 */
public class DatabaseLoadResultSetTask extends AbstractDatabaseTask {

    private IdentificationFraction identificationFraction = null;
    private List<AbstractData> list = null;

    public DatabaseLoadResultSetTask(AbstractDatabaseCallback callback, IdentificationFraction identificationFraction, List<AbstractData> list) {
        super(callback);
        this.identificationFraction = identificationFraction;
        this.list = list;

    }

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {

        Integer id = identificationFraction.getResultSetId();
        if (id == null) {
            // should never happen
            logger.error("IdentificationFraction : no ResultSetId : " + identificationFraction.getId());
            return false;
        }


        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            ResultSet rset = entityManagerMSI.find(ResultSet.class, id);
            if (rset == null) {
                // should not happen
                logger.error("IdentificationFraction : ResultSet not found with Id :" + id);
            } else {

                // Check if the ResultSet has ResultSummary Children.
                TypedQuery<Long> hasChildrenQuery = entityManagerMSI.createQuery("SELECT count(rset.id) FROM ResultSummary rsm, ResultSet rset WHERE rset.id=:resultSetId AND rsm.resultSet.id=rset.id", Long.class);
                hasChildrenQuery.setParameter("resultSetId", rset.getId());
                boolean hasChildren = (hasChildrenQuery.getSingleResult() == 1);

                list.add(new ResultSetData(rset, hasChildren));
            }

            entityManagerMSI.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }
}
