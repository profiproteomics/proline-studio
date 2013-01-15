package fr.proline.studio.dam.data;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadRsmFromRsmTask;
import java.util.List;

/**
 * Correspond to the ResultSummary in MSI DB
 *
 * @author JM235353
 */
public class ResultSummaryData /*extends AbstractData*/ {
/*
    private ResultSummary resultSummary = null;

    public ResultSummaryData(ResultSummary resultSummary, boolean hasChildren) {
        dataType = DataTypes.RESULT_SUMMARY;
        this.resultSummary = resultSummary;
        this.hasChildren = hasChildren;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadRsmFromRsmTask(callback, resultSummary, list));


    }

    @Override
    public String getName() {
        if (resultSummary != null) {
            return "Rsm" + resultSummary.getId();
        }
        return "";
    }

    public ResultSummary getResultSummary() {
        return resultSummary;
    }*/
}
