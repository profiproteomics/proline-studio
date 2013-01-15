package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ResultSummaryData;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Result Summaries children of a Result Summary
 *
 * @author JM235353
 */
public class DatabaseLoadRsmFromRsmTask /*extends AbstractDatabaseTask*/ {
/*
    private ResultSummary resultSummary = null;
    private List<AbstractData> list = null;

    public DatabaseLoadRsmFromRsmTask(AbstractDatabaseCallback callback, ResultSummary resultSummary, List<AbstractData> list) {
        super(callback);
        this.resultSummary = resultSummary;
        this.list = list;

    }

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DatabaseManager.getInstance().getMsiDbConnector( AccessDatabaseThread.getProjectIdTMP()).getEntityManagerFactory().createEntityManager();  //JPM.TODO : project id        
        try {

            entityManagerMSI.getTransaction().begin();

            // We are obliged to merge, to avoid lazy exception in resultSummary
            // because the previous entityManager has been closed
            ResultSummary resultSummaryMerged = entityManagerMSI.merge(resultSummary);

            Set<ResultSummary> resultSummaries = resultSummaryMerged.getChildren();
            Iterator<ResultSummary> it = resultSummaries.iterator();
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
    }*/
}
