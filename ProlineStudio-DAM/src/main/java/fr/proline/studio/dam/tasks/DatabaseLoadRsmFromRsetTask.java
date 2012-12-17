package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.repository.Database;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.*;
import fr.proline.studio.dam.data.ResultSummaryData;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load a Result Summary of a Result Set
 *
 * @author JM235353
 */
public class DatabaseLoadRsmFromRsetTask extends AbstractDatabaseTask {

    private ResultSet resultSet = null;
    private List<AbstractData> list = null;

    public DatabaseLoadRsmFromRsetTask(AbstractDatabaseCallback callback, ResultSet resultSet, List<AbstractData> list) {
        super(callback);
        this.resultSet = resultSet;
        this.list = list;

    }

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {


        Integer id = resultSet.getId();



        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(Database.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            TypedQuery<ResultSummary> rsmQuery = entityManagerMSI.createQuery("SELECT rs FROM fr.proline.core.orm.msi.ResultSummary rs WHERE rs.resultSet.id=:resultSetId", ResultSummary.class);   //find(ResultSummary.class, id);
            rsmQuery.setParameter("resultSetId", id);
            List<ResultSummary> rsmList = rsmQuery.getResultList();

            Iterator<ResultSummary> it = rsmList.iterator();
            while (it.hasNext()) {
                ResultSummary rsmCur = it.next();

                // Check if the ResultSummary has Children without loading them.
                TypedQuery<Long> isEmptyQuery = entityManagerMSI.createQuery("SELECT count(*) FROM ResultSummary rs WHERE rs.id=:resultSummaryId AND rs.children IS EMPTY", Long.class);
                isEmptyQuery.setParameter("resultSummaryId", rsmCur.getId());
                boolean hasChildren = (isEmptyQuery.getSingleResult() == 0);

                list.add(new ResultSummaryData(rsmCur, hasChildren));
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
